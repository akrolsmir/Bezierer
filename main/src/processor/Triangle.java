package processor;

public class Triangle extends Polygon{
	public Vertex[] points = new Vertex[3];
	public int numPoints = 3;
	
	public Triangle(Vertex[] p){
		for(int i = 0; i < 3; i++){
			points[i] = p[i];
		}
	}
	
	public Vertex[] getPoints(){
		return points;
	}
	
	public int getNum(){
		return numPoints;
	}
}
