package processor;

public class Quad {
	public Vertex[] points = new Vertex[4];
	
	public Quad(Vertex[] p){
		for(int i = 0; i < 4; i++){
			points[i] = p[i];
		}
	}
}
