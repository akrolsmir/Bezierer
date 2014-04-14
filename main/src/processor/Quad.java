package processor;

public class Quad {
	public Point[] points = new Point[4];
	public Point[] normals = new Point[4];
	
	public Quad(Point[] p){
		for(int i = 0; i < 4; i++){
			points[i] = p[i];
		}
		for(int i = 0; i < 4; i++){
			normals[i] = (points[(i+1) % 4].subtract(points[i])).crossProduct(points[(i+3) % 4].subtract(points[i]));
		}
	}
}
