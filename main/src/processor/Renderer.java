package processor;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.*;

import com.jogamp.opengl.util.Animator;


public class Renderer implements GLEventListener {

	private float[] rgba_spec = {0.0f, 0.0f, 0.0f};
	private float[] rgba_diff = {0.0f, 0.0f, 0.0f};
	private float[] rgba_amb = {0.0f, 1.0f, 0.0f};
	
	private float[] filled_rgba_spec = {1.0f, 1.0f, 1.0f};
	private float[] filled_rgba_diff = {1.0f, 0.0f, 0.0f};
	private float[] filled_rgba_amb = {0.2f, 0.0f, 0.0f};
	
	private static String fileName;
	
	private enum Mode {
		FILLED, WIREFRAME, HIDDEN_LINE
	}
	
	private enum TessMode {
		UNIFORM, ADAPTIVE
	}

	private static boolean smooth = true;
	
	private static Mode mode = Mode.FILLED;
	private static TessMode tess = TessMode.UNIFORM;
	
	private List<Polygon> quads = new ArrayList<>();
	static double modifier;
	
	private static float rotateX, rotateY, rotateZ;
	private static float translateX, translateY, translateZ = -10;

	@Override
	public void display(GLAutoDrawable gLDrawable) {
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();
		gl.glTranslatef(translateX, translateY, translateZ);

		// rotate about the three axes
		gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f);
		
		gl.glShadeModel(smooth ? GL2.GL_SMOOTH : GL2.GL_FLAT);
		
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, rgba_amb, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, rgba_diff, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, rgba_spec, 0);

		switch (mode) {
		case WIREFRAME:
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			drawQuads(gl);
			break;
		case HIDDEN_LINE:
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			drawQuads(gl);

			// Hidden-line removal through polygon offset
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glPolygonOffset(1.0f, 1.0f);
			gl.glColor3f(0.0f, 0.0f, 0.0f); // Background color
			drawQuads(gl);
			gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
			gl.glEnable(GL2.GL_LIGHTING);
			break;
		case FILLED:
	        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, filled_rgba_amb, 0);
	        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, filled_rgba_diff, 0);
	        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, filled_rgba_spec, 0);
	        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 16.0f);
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			drawQuads(gl);
			break;
		}

		// increasing rotation for the next iteration
		// rotateT += 0.2f;
	}
	
	private void drawQuads(GL2 gl) {
		// Draw all quads
		for (Polygon quad : quads) {
			gl.glBegin(GL2.GL_POLYGON);
			for (int i = 0; i < quad.getNum(); i++) {
				Vertex[] points = quad.getPoints();
				float[] normal = {(float) points[i].n.getX(), (float) points[i].n.getY(), (float) points[i].n.getZ()};
				gl.glNormal3fv(normal, 0);
				gl.glVertex3d(points[i].p.getX(), points[i].p.getY(), points[i].p.getZ());
			}
			gl.glEnd();
		}
	}
	
	private void initLight(GL2 gl){
		float[] lightPos = { 2000,2000,2000, 1 };
		float[] noAmbient = { 0.2f, 0.2f, 0.2f, 1f };
		float[] specular = { 1.0f, 1.0f, 1.0f, 1f };

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, noAmbient, 0);
		//gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, specular, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specular, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
	}


	@Override
	public void init(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		initLight(gl);
		//gl.glShadeModel(GLLightingFunc.GL_FLAT);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		// Parse all patches, then tessellate into quads
		System.out.println("Parsing...");
		if(fileName.split("\\.")[1].equals("bez")){
			List<Patch> patches = Parser.readBez(fileName);
			System.out.println("Tessellating...");
			for (Patch patch : patches) {
				switch(tess){
				case UNIFORM: 
					quads.addAll(patch.uniformTessellation(modifier));
					break;
				case ADAPTIVE:
					quads.addAll(patch.adaptiveTessellation(modifier, 0.0, 1.0, 0.0, 1.0));
					break;
				}
			}
		} else {
			quads.addAll(Parser.readObj(fileName));
			System.out.println(quads.size());
		}
		System.out.println("Done.");
	}

	@Override
	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width,
			int height) {
		GL2 gl = gLDrawable.getGL().getGL2();
		final float aspect = (float) width / (float) height;
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		final float fh = 0.5f;
		final float fw = fh * aspect;
		gl.glFrustumf(-fw, fw, -fh, fh, 1.0f, 1000.0f);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void dispose(GLAutoDrawable gLDrawable) {
	}

	public static void main(String[] args) {
		
		args = new String[]{"teapot.bez", "0.1"};
		
		fileName = args[0];
		modifier = Double.parseDouble(args[1]);
		for(int i = 2; i < args.length; i++){
			if(args[i].equals("-a")){
				tess = TessMode.ADAPTIVE;
			}
		}
		final GLCanvas canvas = new GLCanvas();
		final Frame frame = new Frame("BZR!");
		final Animator animator = new Animator(canvas);
		canvas.addGLEventListener(new Renderer());
		frame.add(canvas);
		frame.setSize(640, 480);
		frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				frame.dispose();
				System.exit(0);
			}
		});
		
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				float turn = 2f, move = .1f;

				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
					switch (e.getKeyCode()) {
					// Translate object in x/y plane
					case KeyEvent.VK_UP:
						translateY += move;
						break;
					case KeyEvent.VK_DOWN:
						translateY -= move;
						break;
					case KeyEvent.VK_LEFT:
						translateX -= move;
						break;
					case KeyEvent.VK_RIGHT:
						translateX += move;
						break;
					}
				} else {
					switch (e.getKeyCode()) {
					// Rotate object
					case KeyEvent.VK_UP:
						rotateX += turn;
						break;
					case KeyEvent.VK_DOWN:
						rotateX -= turn;
						break;
					case KeyEvent.VK_LEFT:
						rotateY += turn;
						break;
					case KeyEvent.VK_RIGHT:
						rotateY -= turn;
						break;
						
					// Zoom in or out
					case KeyEvent.VK_EQUALS:
						translateZ += move;
						break;
					case KeyEvent.VK_MINUS:
						translateZ -= move;
						break;

					// Toggle different modes
					case KeyEvent.VK_S:
						smooth = !smooth;
						break;
					case KeyEvent.VK_W:
						mode = mode == Mode.WIREFRAME ? Mode.FILLED : Mode.WIREFRAME;
						break;
					case KeyEvent.VK_H:
						mode = mode == Mode.HIDDEN_LINE ? Mode.FILLED : Mode.HIDDEN_LINE;
						break;
					}
				}
			}
		});
		
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}
}