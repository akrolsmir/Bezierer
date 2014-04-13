package processor;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.FileSystems;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.*;
import com.jogamp.opengl.util.Animator;


public class Renderer implements GLEventListener {

	private float rotateT = 0.0f;

	@Override
	public void display(GLAutoDrawable gLDrawable) {
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -5.0f);

		// rotate about the three axes
		gl.glRotatef(rotateT, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotateT, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotateT, 0.0f, 0.0f, 1.0f);

		// Draw all quads
		for (Point[] quad : quads) {
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor3f(0.0f, 1.0f, 0.0f); // set the color of the quad
			for (int i = 0; i < 4; i++) {
				gl.glVertex3d(quad[i].getX(), quad[i].getY(), quad[i].getZ());
			}
			gl.glEnd();
		}

		// increasing rotation for the next iteration
		rotateT += 0.2f;
	}

	// private ;
	private List<Point[]> quads = new ArrayList<>();
	double step = 0.2;

	@Override
	public void init(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

		// Parse all patches, then tessellate into quads
		System.out.println("Parsing...");
		List<Patch> patches = Parser.read(FileSystems.getDefault().getPath("teapot.bez"));
		System.out.println("Tessellating...");
		for (Patch patch : patches) {
			quads.addAll(patch.uniformTessellation(step));
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
		final GLCanvas canvas = new GLCanvas();
		final Frame frame = new Frame("Jogl Quad drawing");
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
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}
}