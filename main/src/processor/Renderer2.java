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
	
	private int[] mode_ghetto_enum = {GL2.GL_LINE, GL2.GL_FILL};
	private int[] shadeMode_ghetto_enum = {GLLightingFunc.GL_FLAT, GLLightingFunc.GL_SMOOTH};
	private static int mode = 1;
	private static int shadeMode = 1;

	@Override
	public void display(GLAutoDrawable gLDrawable) {
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, mode_ghetto_enum[mode]);
		gl.glShadeModel(shadeMode_ghetto_enum[shadeMode]);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -5.0f);

		// rotate about the three axes
		gl.glRotatef(rotateT, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotateT, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotateT, 0.0f, 0.0f, 1.0f);

		// Draw all quads
		for (Quad quad : quads) {
			gl.glBegin(GL2.GL_QUADS);
			//gl.glColor3f(1.0f, 0.0f, 0.0f); // set the color of the quad
			for (int i = 0; i < 4; i++) {
				List<Point> curr_norms = norms.get(quad.points[i]);
				Point avg = new Point(0.0,0.0,0.0);
				int len = curr_norms.size();
				for(int j = 0; j < len; j++){
					avg = avg.add(curr_norms.get(j));
				}
				avg.normalize();
				float[] normal = {(float) avg.getX(), (float) avg.getY(), (float) avg.getZ()};
				gl.glNormal3fv(normal, 0);
				gl.glVertex3d(quad.points[i].getX(), quad.points[i].getY(), quad.points[i].getZ());
			}
			gl.glEnd();
		}

		// increasing rotation for the next iteration
		rotateT += 0.05f;
	}

	// private ;
	private List<Quad> quads = new ArrayList<>();
	private Hashtable<Point, List<Point>> norms = new Hashtable<Point, List<Point>>();
	double step = 0.25;
	
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
		
		float[] rgba_spec = {1.0f, 1.0f, 1.0f};
		float[] rgba_diff = {1.0f, 0.0f, 0.0f};
		float[] rgba_amb = {0.3f, 0.0f, 0.0f};
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, rgba_amb, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, rgba_diff, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, rgba_spec, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 16.0f);

		// Parse all patches, then tessellate into quads
		System.out.println("Parsing...");
		List<Patch> patches = Parser.read(FileSystems.getDefault().getPath("teapot.bez"));
		System.out.println("Tessellating...");
		for (Patch patch : patches) {
			quads.addAll(patch.uniformTessellation(step));
		}
		for(Quad quad : quads){
			for(int i = 0; i < 4; i++){
				if(norms.containsKey(quad.points[i])){
					norms.get(quad.points[i]).add(quad.normals[i]);
				} else {
					ArrayList<Point> ns = new ArrayList<Point>();
					ns.add(quad.normals[i]);
					norms.put(quad.points[i], ns);
				}
			}
		}
		System.out.println("Generating normals...");
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
		
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				System.out.print("Pressed ");
				switch(e.getKeyCode()){
					case KeyEvent.VK_W:
						System.out.println("w");
						mode = (1 - mode);
						break;
					case KeyEvent.VK_S:
						System.out.println("s");
						shadeMode = (1 - shadeMode);
						break;
				}
			}
		});
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}
}