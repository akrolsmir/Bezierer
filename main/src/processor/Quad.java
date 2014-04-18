package processor;

public class Quad extends Polygon{
	public Vertex[] points = new Vertex[4];
	public int numPoints = 4;
	
	public Quad(Vertex[] p){
		for(int i = 0; i < 4; i++){
			points[i] = p[i];
		}
	}
	
	public Vertex[] getPoints(){
		return points;
	}
	
	public void setPoints(Vertex[] p){
		for(int i = 0; i < 4; i++){
			points[i] = p[i];
		}
	}
	
	public int getNum(){
		return numPoints;
	}
}
