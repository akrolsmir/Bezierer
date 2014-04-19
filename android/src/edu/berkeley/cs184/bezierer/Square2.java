package edu.berkeley.cs184.bezierer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import processor.Quad;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class Square2
{
	/** Used for debug logs. */
	private static final String TAG = "LessonTwoRenderer";
	
//	/**
//	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
//	 * of being located at the center of the universe) to world space.
//	 */
//	private float[] mModelMatrix = new float[16];
//
//	/**
//	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
//	 * it positions things relative to our eye.
//	 */
//	private float[] mViewMatrix = new float[16];
//
//	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
//	private float[] mProjectionMatrix = new float[16];
//	
//	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
//	private float[] mMVPMatrix = new float[16];
	
	/** 
	 * Stores a copy of the model matrix specifically for the light position.
	 */
	private float[] mLightModelMatrix = new float[16];	
	
	/** Store our model data in a float buffer. */
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;
	
	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;
	
	/** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;
	
	/** This will be used to pass in the light position. */
	private int mLightPosHandle;
	
	/** This will be used to pass in model position information. */
	private int mPositionHandle;
	
	/** This will be used to pass in model color information. */
	private int mColorHandle;
	
	/** This will be used to pass in model normal information. */
	private int mNormalHandle;

	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;	
	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;	
	
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	
	
	/** Size of the normal data in elements. */
	private final int mNormalDataSize = 3;
	
	/** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
	 *  we multiply this by our transformation matrices. */
	private final float[] mLightPosInModelSpace = new float[] {3.0f, 3.0f, 3.0f, 1.0f};
	
	/** Used to hold the current position of the light in world space (after transformation via model matrix). */
	private final float[] mLightPosInWorldSpace = new float[4];
	
	/** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
	private final float[] mLightPosInEyeSpace = new float[4];
	
	/** This is a handle to our per-vertex cube shading program. */
	private int mPerVertexProgramHandle;
		
	/** This is a handle to our light point program. */
	private int mPointProgramHandle;	
						
	/**
	 * Initialize the model data.
	 */
	public Square2(Quad quad)
	{	
		// Define points for a cube.		
		
    	final float[] squareCoords = {
    			(float) quad.points[0].p.getX(), (float) quad.points[0].p.getY(), (float) quad.points[0].p.getZ(),
    			(float) quad.points[1].p.getX(), (float) quad.points[1].p.getY(), (float) quad.points[1].p.getZ(),
    			(float) quad.points[2].p.getX(), (float) quad.points[2].p.getY(), (float) quad.points[2].p.getZ(),
    			(float) quad.points[3].p.getX(), (float) quad.points[3].p.getY(), (float) quad.points[3].p.getZ()
    	};
    	
    	final float[] squareNormals = { 
    			(float) quad.points[0].n.getX(), (float) quad.points[0].n.getY(), (float) quad.points[0].n.getZ(),
    			(float) quad.points[1].n.getX(), (float) quad.points[1].n.getY(), (float) quad.points[1].n.getZ(),
    			(float) quad.points[2].n.getX(), (float) quad.points[2].n.getY(), (float) quad.points[2].n.getZ(),
    			(float) quad.points[3].n.getX(), (float) quad.points[3].n.getY(), (float) quad.points[3].n.getZ()
    	};
		
		// R, G, B, A
		final float[] cubeColorData =
		{				
				// Green
				0.0f, 1.0f, 0.0f, 1.0f,				
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,				

		};
		
		// Initialize the buffers.
		mCubePositions = ByteBuffer.allocateDirect(squareCoords.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubePositions.put(squareCoords).position(0);		
		
		mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeColors.put(cubeColorData).position(0);
		
		mCubeNormals = ByteBuffer.allocateDirect(squareNormals.length * mBytesPerFloat)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mCubeNormals.put(squareNormals).position(0);
		
		final String vertexShader = getVertexShader();   		
 		final String fragmentShader = getFragmentShader();			
		
		final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		
		mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position",  "a_Color", "a_Normal"});
		
      // Define a simple shader program for our point.
      final String pointVertexShader =
      	"uniform mat4 u_MVPMatrix;      \n"		
        +	"attribute vec4 a_Position;     \n"		
        + "void main()                    \n"
        + "{                              \n"
        + "   gl_Position = u_MVPMatrix   \n"
        + "               * a_Position;   \n"
        + "   gl_PointSize = 5.0;         \n"
        + "}                              \n";
      
      final String pointFragmentShader = 
      	"precision mediump float;       \n"					          
        + "void main()                    \n"
        + "{                              \n"
        + "   gl_FragColor = vec4(1.0,    \n" 
        + "   1.0, 1.0, 1.0);             \n"
        + "}                              \n";
      
      final int pointVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
      final int pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
      mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle, 
      		new String[] {"a_Position"});   
      
      // initialize byte buffer for the draw list
      ByteBuffer dlb = ByteBuffer.allocateDirect(
              // (# of coordinate values * 2 bytes per short)
              drawOrder.length * 2);
      dlb.order(ByteOrder.nativeOrder());
      drawListBuffer = dlb.asShortBuffer();
      drawListBuffer.put(drawOrder);
      drawListBuffer.position(0);
	}
	
	protected String getVertexShader()
	{
		// TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
		final String vertexShader =
			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
		  + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.	
		  + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
			
		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
		  + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
		  
		  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
		  
		  + "void main()                    \n" 	// The entry point for our vertex shader.
		  + "{                              \n"		
		// Transform the vertex into eye space.
		  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
		// Transform the normal's orientation into eye space.
		  + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
		// Will be used for attenuation.
		  + "   float distance = length(u_LightPos - modelViewVertex);             \n"
		// Get a lighting direction vector from the light to the vertex.
		  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
		// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
		// pointing in the same direction then it will get max illumination.
		  + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n" 	  		  													  
		// Attenuate the light based on distance.
		 // + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
		// Multiply the color by the illumination level. It will be interpolated across the triangle.
		  + "   v_Color = a_Color * diffuse;                                       \n" 	 
		// gl_Position is a special variable used to store the final position.
		// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.		
		  + "   gl_Position = u_MVPMatrix * a_Position;                            \n"     
		  + "}                                                                     \n"; 
		
		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
													// precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
		  											// triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}                              \n";
		
		return fragmentShader;
	}
	
	/**
	 * Draws this square
	 */			
	public void draw(float[] mvpMatrix, float[] vMatrix)
	{		
		// Set our per-vertex lighting program.
        GLES20.glUseProgram(mPerVertexProgramHandle);
        
        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix"); 
        mLightPosHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal"); 
        
        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
//        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -10.0f);      
//        Matrix.rotateM(mLightModelMatrix, 0, 60, 0.0f, 1.0f, 0.0f);
//        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.0f);
               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, vMatrix, 0, mLightPosInWorldSpace, 0);     
		
		// Pass in the position information
		mCubePositions.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, mCubePositions);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		0, mCubeColors);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
        // Pass in the normal information
        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false, 
        		0, mCubeNormals);
        
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        
//		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
//        // (which currently contains model * view).
//        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, vMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        
        // Pass in the light position in eye space.        
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        
        // Draw the cube.
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glDrawElements(
        		GLES20.GL_TRIANGLES, drawOrder.length,
        		GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        
        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);        
        drawLight(mvpMatrix);
	}	
	
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    ShortBuffer drawListBuffer;
	
	/**
	 * Draws a point representing the position of the light.
	 */
	private void drawLight(float[] mvpMatrix)
	{
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");
        
		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);  
		
		// Pass in the transformation matrix.
//		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
//		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mvpMatrix, 0);
		
		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}
	
	/** 
	 * Helper function to compile a shader.
	 * 
	 * @param shaderType The shader type.
	 * @param shaderSource The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	private int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}
		
		return shaderHandle;
	}	
	
	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
	{
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			if (attributes != null)
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
		
		return programHandle;
	}
}
