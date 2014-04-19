package processor;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	
	private float[] select_rgba_spec = {0.0f, 0.0f, 0.0f};
	private float[] select_rgba_diff = {0.0f, 0.0f, 0.0f};
	private float[] select_rgba_amb = {1.0f, 1.0f, 0.0f};
	
	private float[] select_filled_rgba_spec = {1.0f, 1.0f, 1.0f};
	private float[] select_filled_rgba_diff = {1.0f, 1.0f, 0.0f};
	private float[] select_filled_rgba_amb = {0.2f, 0.2f, 0.0f};
	
	private Hashtable<Point, Integer> vertices = new Hashtable<Point, Integer>();
	private Hashtable<Point, Integer> normals = new Hashtable<Point, Integer>();
	private ArrayList<Point> orderedVerts = new ArrayList<Point>();
	private ArrayList<Point> orderedNorms = new ArrayList<Point>();
	
	private double maxC;

	private static String fileName;
	private static String outputFile;
	
	private enum Mode {
		FILLED, WIREFRAME, HIDDEN_LINE
	}
	
	private enum TessMode {
		UNIFORM, ADAPTIVE
	}
	
	private enum WriteMode {
		READ, WRITE
	}
	
	private enum Curvature {
		ON, OFF
	}

	private static boolean smooth = true;
	
	private static Mode mode = Mode.FILLED;
	private static TessMode tess = TessMode.UNIFORM;
	private static WriteMode write = WriteMode.READ;
	private static Curvature curve = Curvature.OFF;
	
	private List<Polygon> quads = new ArrayList<>();
	static double modifier;
	
	private static ArrayList<Float[]> rotations = new ArrayList<Float[]>();
	private static ArrayList<Float[]> translations = new ArrayList<Float[]>();
	private static List<List<Polygon>> polyList = new ArrayList<List<Polygon>>();
	
	private static int current = 0;

	@Override
	public void display(GLAutoDrawable gLDrawable) {
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();
		for(int i = 0; i < polyList.size(); i++){
			
			gl.glTranslatef(translations.get(i)[0], translations.get(i)[1], translations.get(i)[2]);
	
			// rotate about the three axes
			gl.glRotatef(rotations.get(i)[0], 1.0f, 0.0f, 0.0f);
			gl.glRotatef(rotations.get(i)[1], 0.0f, 1.0f, 0.0f);
			gl.glRotatef(rotations.get(i)[2], 0.0f, 0.0f, 1.0f);
			
			gl.glShadeModel(smooth ? GL2.GL_SMOOTH : GL2.GL_FLAT);
			
			if(curve == Curvature.OFF){
				gl.glEnable(GL2.GL_LIGHTING);
				if(i == current){
					gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, select_rgba_amb, 0);
			        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, select_rgba_diff, 0);
			        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, select_rgba_spec, 0);
				} else {
					gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, rgba_amb, 0);
			        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, rgba_diff, 0);
			        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, rgba_spec, 0);
				}
			} else {
				gl.glDisable(GL2.GL_LIGHTING);
			}
	
			switch (mode) {
			case WIREFRAME:
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
				drawQuads(gl, i);
				break;
			case HIDDEN_LINE:
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
				drawQuads(gl, i);
	
				// Hidden-line removal through polygon offset
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
				gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
				gl.glDisable(GL2.GL_LIGHTING);
				gl.glPolygonOffset(1.0f, 1.0f);
				gl.glColor3f(0.0f, 0.0f, 0.0f); // Background color
				drawQuads(gl, i);
				gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
				gl.glEnable(GL2.GL_LIGHTING);
				break;
			case FILLED:
				if(curve == Curvature.OFF){
					if(i == current){
						gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, select_filled_rgba_amb, 0);
				        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, select_filled_rgba_diff, 0);
				        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, select_filled_rgba_spec, 0);
				        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 16.0f);
					} else {
						gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, filled_rgba_amb, 0);
				        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, filled_rgba_diff, 0);
				        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, filled_rgba_spec, 0);
				        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 16.0f);
					}
				}
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
				drawQuads(gl, i);
				break;
			}
			
			
			//undo all transforms
			
			// rotate about the three axes
			gl.glRotatef(-rotations.get(i)[2], 0.0f, 0.0f, 1.0f);
			gl.glRotatef(-rotations.get(i)[1], 0.0f, 1.0f, 0.0f);
			gl.glRotatef(-rotations.get(i)[0], 1.0f, 0.0f, 0.0f);
			
			gl.glTranslatef(-translations.get(i)[0], -translations.get(i)[1], -translations.get(i)[2]);
			
			
		}

		// increasing rotation for the next iteration
		// rotateT += 0.2f;
	}
	
	private double findMaxCurvature(){
		double maxCurve = 0.0;
		for(List<Polygon> lst : polyList){
			for(Polygon p : lst){
				Vertex[] points = p.getPoints();
				for(int i = 0; i < p.getNum(); i++){
					if(points[i].curvature > maxCurve){
						maxCurve = points[i].curvature;
					}
				}
			}
		}
		return maxCurve;
	}
	
	private void drawQuads(GL2 gl, int num) {
		// Draw all quads
		for (Polygon quad : polyList.get(num)) {
			gl.glBegin(GL2.GL_POLYGON);
			Vertex[] points = quad.getPoints();
			for (int i = 0; i < quad.getNum(); i++) {
				float[] normal = {(float) points[i].n.getX(), (float) points[i].n.getY(), (float) points[i].n.getZ()};
				if(curve == Curvature.ON){
					gl.glColor3d(points[i].curvature/maxC, 0.0, (maxC-points[i].curvature)/maxC);
				}
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
	
	private void initFile(String file_name){
		System.out.println("Parsing...");
		ArrayList<Polygon> temp = new ArrayList<Polygon>();
		if(file_name.split("\\.")[1].compareTo("bez") == 0){
			List<Patch> patches = Parser.readBez(file_name);
			System.out.println("Tessellating...");
			for (Patch patch : patches) {
				switch(tess){
				case UNIFORM: 
					temp.addAll(patch.uniformTessellation(modifier));
					break;
				case ADAPTIVE:
					temp.addAll(patch.adaptiveTessellation(modifier, 0.0, 1.0, 0.0, 1.0));
					break;
				}
			}
		} else {
			temp.addAll(Parser.readObj(fileName));
		}
		polyList.add(temp);
		System.out.println("Done.");
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
		if(fileName.split("\\.")[1].compareTo("scene") == 0){
			//do something
			try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
				String line;
				while((line = reader.readLine()) != null){
					if(line.matches("\\s*")){
					} else {
						String[] in = line.trim().split("\\s+");
						String buf = in[0];
						if (buf.equals("translate")){
							Float[] temp = {Float.parseFloat(in[1]), Float.parseFloat(in[2]), Float.parseFloat(in[3])};
							translations.add(temp);
						} else if (buf.equals("rotate")){
							Float[] temp = {Float.parseFloat(in[1]), Float.parseFloat(in[2]), Float.parseFloat(in[3])};
							rotations.add(temp);
						} else{
							initFile(buf);
						}
					}
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		} else {
			Float[] trans = {(float) 0.0, (float) 0.0, (float) -10.0};
			translations.add(trans);
			Float[] rots = {(float) 0.0, (float) 0.0, (float) 0.0};
			rotations.add(rots);
			initFile(fileName);
		}
		
		maxC = findMaxCurvature()*.1;
		
		if(write == WriteMode.WRITE){
			System.out.println("Writing...");
			int v = 0;
			int n = 0;
			for(Polygon quad : quads){
				Vertex[] polyVertices = quad.getPoints();
				for(int i = 0; i < polyVertices.length; i++){
					if(!vertices.containsKey(polyVertices[i].p)){
						v++;
						vertices.put(polyVertices[i].p, v);
						orderedVerts.add(polyVertices[i].p);
					}
				}
				for(int i = 0; i < polyVertices.length; i++){
					if(!normals.containsKey(polyVertices[i].n)){
						n++;
						normals.put(polyVertices[i].n, n);
						orderedNorms.add(polyVertices[i].n);
					}
				}
			}
			try{
				File file = new File(outputFile);
				if(!file.exists()){
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				for(Point p : orderedVerts){
					bw.write("v " + p.getX() + " " + p.getY() + " " + p.getZ() + "\n");
				}
				for(Point p : orderedNorms){
					bw.write("vn " + p.getX() + " " + p.getY() + " " + p.getZ() + "\n");
				}
				for(Polygon q : quads){
					bw.write("f ");
					Vertex[] points = q.getPoints();
					for(int i = 0; i < q.getNum(); i++){
						bw.write(vertices.get(points[i].p).toString() + "//" + normals.get(points[i].n).toString() + " ");
					}
					bw.write("\n");
				}
				bw.close();
				System.out.println("Done");
			} catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}
			System.exit(1);
		}
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
		
		fileName = args[0];
		modifier = Double.parseDouble(args[1]);
		for(int i = 2; i < args.length; i++){
			if(args[i].equals("-a")){
				tess = TessMode.ADAPTIVE;
			}
			if(args[i].equals("-o")){
				write = WriteMode.WRITE;
				outputFile = args[i+1];
				i++;
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
						translations.get(current)[1] += move;
						break;
					case KeyEvent.VK_DOWN:
						translations.get(current)[1] -= move;
						break;
					case KeyEvent.VK_LEFT:
						translations.get(current)[0] -= move;
						break;
					case KeyEvent.VK_RIGHT:
						translations.get(current)[0] += move;
						break;
					}
				} else {
					switch (e.getKeyCode()) {
					// Rotate object
					case KeyEvent.VK_UP:
						rotations.get(current)[0] += turn;
						break;
					case KeyEvent.VK_DOWN:
						rotations.get(current)[0] -= turn;
						break;
					case KeyEvent.VK_LEFT:
						rotations.get(current)[1] += turn;
						break;
					case KeyEvent.VK_RIGHT:
						rotations.get(current)[1] -= turn;
						break;
						
					// Zoom in or out
					case KeyEvent.VK_EQUALS:
						translations.get(current)[2] += move;
						break;
					case KeyEvent.VK_MINUS:
						translations.get(current)[2] -= move;
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
					
					// Toggle objects
					case KeyEvent.VK_V:
						current = (current + 1) % polyList.size();
						break;
					
					//Gaussian curvature
					case KeyEvent.VK_C:
						curve = curve == Curvature.ON ? Curvature.OFF : Curvature.ON;
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