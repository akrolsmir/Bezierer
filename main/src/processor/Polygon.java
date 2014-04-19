package processor;

public abstract class Polygon {
	public Vertex[] points;
	public int numPoints;
	
	public abstract Vertex[] getPoints();
	
	public abstract int getNum();
	
	public abstract void setPoints(Vertex[] p);
}
