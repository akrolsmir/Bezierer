package processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Parser {
	
	private static final Charset charset = StandardCharsets.UTF_8;
	private static int lineNum = 1;
	
		
	/**
	 * File management code taken from http://docs.oracle.com/javase/tutorial/essential/io/file.html
	 * @throws IOException
	 */
	public static ArrayList<Patch> readBez(String fileName){
		Path file = FileSystems.getDefault().getPath(fileName);
		ArrayList<Patch> patches = new ArrayList<Patch>();
		lineNum = 1;
		try(BufferedReader reader = Files.newBufferedReader(file, charset)){
			int numPatches = Integer.parseInt(reader.readLine());
			while(numPatches-- > 0){
				patches.add(read_patch(reader));
			}
		} catch (IOException | ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Invalid read.");
			System.exit(1);
		} catch (NumberFormatException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Expected double.");
			System.exit(1);
		}
		return patches;
	}
	
	public static ArrayList<Polygon> readObj(String fileName){
		Path file = FileSystems.getDefault().getPath(fileName);
		ArrayList<Point> vertices = new ArrayList<Point>();
		ArrayList<Point> normals = new ArrayList<Point>();
		ArrayList<Polygon> polys = new ArrayList<Polygon>();
		Hashtable<Point, ArrayList<Point>> hash = new Hashtable<Point, ArrayList<Point>>();
		Hashtable<Point, Point> avgs = new Hashtable<Point, Point>();
		boolean isNorm = false;
		lineNum = 1;
		try(BufferedReader reader = Files.newBufferedReader(file, charset)){
			String line;
			while((line = reader.readLine()) != null){
				if(line.matches("\\s*")){
					lineNum++;
				} else {
					lineNum++;
					String[] in = line.trim().split("\\s+");
					String buf = in[0];
					if (buf.equals("v")){
						vertices.add(new Point(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3])));
					}
					else if (buf == "vt"){
						/*ignore*/
						continue;
					}
					else if (buf.equals("vn")){
						normals.add(new Point(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3])));
					}
					else if (buf.equals("f")){
						int numVerts = in.length - 1;
						Vertex[] faceVerts = new Vertex[numVerts];
						for (int i = 1; i < in.length; i++){
							String[] verts = in[i].split("/");
							if(verts.length <= 2){
								faceVerts[i-1] = new Vertex(vertices.get(Integer.parseInt(verts[0])-1), new Point(1.0,0.0,0.0), 0.0,0.0);
							} else if (verts.length == 3){
								isNorm = true;
								faceVerts[i-1] = new Vertex(vertices.get(Integer.parseInt(verts[0])-1), normals.get(Integer.parseInt(verts[2])-1), 0.0,0.0);
							}
						}
						
						if(numVerts == 3){
							for(int i = 0; i < 3; i++){
								Point normal = (faceVerts[(i+1)%3].p.subtract(faceVerts[i].p)).crossProduct(faceVerts[(i+2)%3].p.subtract(faceVerts[i].p)).normalize();
								if(hash.containsKey(faceVerts[i].p)){
									hash.get(faceVerts[i].p).add(normal);
								} else {
									ArrayList<Point> temp = new ArrayList<Point>();
									temp.add(normal);
									hash.put(faceVerts[i].p, temp);
								}
							}
							polys.add(new Triangle(faceVerts));
						} else {
							for(int i = 0; i < 4; i++){
								Point normal = (faceVerts[(i+1)%4].p.subtract(faceVerts[i].p)).crossProduct(faceVerts[(i+3)%4].p.subtract(faceVerts[i].p)).normalize();
								if(hash.containsKey(faceVerts[i].p)){
									hash.get(faceVerts[i].p).add(normal);
								} else {
									ArrayList<Point> temp = new ArrayList<Point>();
									temp.add(normal);
									hash.put(faceVerts[i].p, temp);
								}
							}
							polys.add(new Quad(faceVerts));
						}
					}
				}
			}
		} catch (IOException | ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Invalid read.");
			System.exit(1);
		} catch (NumberFormatException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Expected double.");
			System.exit(1);
		}
		if(!isNorm){
			for (Enumeration<Point> e = hash.keys(); e.hasMoreElements();){
				Point key = e.nextElement();
				ArrayList<Point> temp = hash.get(key);
				Point avg = new Point(0.0,0.0,0.0);
				for(Point p : temp){
					avg = avg.add(p);
				}
				avg = avg.normalize();
				avgs.put(key, avg);
			}
			for(Polygon poly : polys){
				Vertex[] points = poly.getPoints();
				for(int i = 0; i < poly.getNum(); i++){
					points[i].n = avgs.get(points[i].p);
				}
				poly.setPoints(points);
			}
		}
		return polys;
	}
	
	public static Patch read_patch(BufferedReader reader) 
			throws IOException, ArrayIndexOutOfBoundsException{
		Patch newPatch = new Patch();
		String line;
		for(int i = 0; i < 4; i++){
			lineNum++;
			while((line = reader.readLine()).matches("\\s*")){
				lineNum++;
			}
			String[] in = line.trim().split("\\s+");
			newPatch.addCurve(new Point[]{
					new Point(Double.parseDouble(in[0]), Double.parseDouble(in[1]), Double.parseDouble(in[2])),
					new Point(Double.parseDouble(in[3]), Double.parseDouble(in[4]), Double.parseDouble(in[5])),
					new Point(Double.parseDouble(in[6]), Double.parseDouble(in[7]), Double.parseDouble(in[8])),
					new Point(Double.parseDouble(in[9]), Double.parseDouble(in[10]), Double.parseDouble(in[11]))
			});
			
		}
		return newPatch;
	}
	
}
