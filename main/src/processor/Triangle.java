package processor;

public class Triangle {
	public Vertex[] points = new Vertex[3];
	
	public Triangle(Vertex[] p){
		for(int i = 0; i < 3; i++){
			points[i] = p[i];
		}
	}
}
