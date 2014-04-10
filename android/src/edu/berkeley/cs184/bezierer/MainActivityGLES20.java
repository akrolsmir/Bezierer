package edu.berkeley.cs184.bezierer;

import javax.media.opengl.GL;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivityGLES20 extends Activity {
	
	MyGLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity.
		mGLView = new MyGLSurfaceView(this);
		setContentView(mGLView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);
			
			// Create an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);

			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(new MyGLRenderer());
		}
	}

	public class MyGLRenderer implements GLSurfaceView.Renderer {

		public void onSurfaceCreated(GL10 unused, EGLConfig config) {
			// Set the background frame color
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		}

		public void onDrawFrame(GL10 unused) {
			// Redraw background color
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			
			GLES20.gl
			
	        GLES20.glBegin(GL.GL_TRIANGLES);
	        gl.glColor3f(1, 0, 0);
	        gl.glVertex2d(-c, -c);
	        gl.glColor3f(0, 1, 0);
	        gl.glVertex2d(0, c);
	        gl.glColor3f(0, 0, 1);
	        gl.glVertex2d(s, -s);
	        gl.glEnd();
		}

		public void onSurfaceChanged(GL10 unused, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
		}
	}
}
