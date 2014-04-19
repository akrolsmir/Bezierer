package processor;

public class Vertex{
	
	//such access control
	public Point p, n;
	public double u,v;
	public double curvature;
	//public Patch patch;
	
	public Vertex(Point p, Point n, double u, double v){
		this.p = p;
		this.n = n;
		this.u = u;
		this.v = v;
	}
	
}
