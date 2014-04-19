package processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Bezier patch
 * 
 */
public class Patch {

	protected Point[][] controls = new Point[4][4];

	/**
	 * Default constructor for a trivial patch (no control points)
	 */
//	public Patch() {
//		controls = new ArrayList<ArrayList<Point>>(0);
//	}

	/**
	 * Constructs a patch which is the Bezier curve defined by points
	 * 
	 * @param points
	 *            the control points of a Bezier curve
	 */
//	public Patch(ArrayList<Point> points) {
//		controls = new ArrayList<ArrayList<Point>>(1);
//		controls.set(0, new ArrayList<Point>(points));
//	}
	
	int index = 0;

	/**
	 * Adds a Bezier curve to the patch
	 * 
	 * @param points
	 *            the control points of a Bezier curve
	 */
	public void addCurve(Point[] curve) {
		controls[index] = curve;
		index++;
	}

//	public boolean equals(Object o) {
//		if (o instanceof Patch) {
//			ArrayList<ArrayList<Point>> oControls = ((Patch) o).controls;
//			if (oControls.size() == controls.size()) {
//				for (int i = 0; i < controls.size(); i++) {
//					if (oControls.get(i).size() == controls.get(i).size()) {
//						for (int j = 0; j < controls.get(i).size(); j++) {
//							if (!controls.get(i).get(j)
//									.equals(oControls.get(i).get(j))) {
//								return false;
//							}
//						}
//					} else {
//						return false;
//					}
//				}
//			} else {
//				return false;
//			}
//			return true;
//		}
//		return false;
//	}
//
//	public Point pointAt(double u, double v) {
//		// TODO try matrix multiply here
//		return new Point();
//	}

	// Helper class for bezPatchInterp
	// Given the control points of a bezier curve
	// and a parametric value, return the curve point
	private Vertex bezCurveInterp(Point[] curve, double u) {
		// First, split each of the three segments
		// to form two new ones AB and BC
		Point A = curve[0].multiply(1 - u).add(curve[1].multiply(u));
		Point B = curve[1].multiply(1 - u).add(curve[2].multiply(u));
		Point C = curve[2].multiply(1 - u).add(curve[3].multiply(u));

		// Now, split AB and BC to form a new segment DE
		Point D = A.multiply(1 - u).add(B.multiply(u));
		Point E = B.multiply(1 - u).add(C.multiply(u));

		// Finally, pick the right point on DE,
		// this is the point on the curve
		Point p = D.multiply(1 - u).add(E.multiply(u));
		
		Point n = (E.subtract(D)).multiply(3);

		return new Vertex(p,n, u, u);
	}

	private Vertex bezPatchInterp(double u, double v) {
		Point[] vcurve = new Point[4];
		Point[] ucurve = new Point[4];
		Point p, n;
		Point vPoint, uPoint;
		for (int i = 0; i < 4; i++) {
			ucurve[i] = bezCurveInterp(controls[i], v).p;
		}
		for (int i = 0; i < 4; i++) {
			Point[] vPoints = new Point[4];
			for(int j = 0; j < 4; j++){
				vPoints[j] = controls[j][i];
			}
			vcurve[i] = bezCurveInterp(vPoints, u).p;
		}
		p = bezCurveInterp(ucurve, u).p;
		
		//broken, doesnt properly handle degenerate normals
		do{
			vPoint = bezCurveInterp(vcurve, v).n;
			uPoint = bezCurveInterp(ucurve, u).n;
			n = uPoint.crossProduct(vPoint).multiply(-1);
			if(n.distance(Point.ZERO) < .0001){
				if(u > 0.5){
					u =  -.00001 + u;
					v = -.00001 + v;
				} else {
					u =  .00001 + u;
					v = .00001 + v;
				}
				for (int i = 0; i < 4; i++) {
					ucurve[i] = bezCurveInterp(controls[i], v).p;
				}
				for (int i = 0; i < 4; i++) {
					Point[] vPoints = new Point[4];
					for(int j = 0; j < 4; j++){
						vPoints[j] = controls[j][i];
					}
					vcurve[i] = bezCurveInterp(vPoints, u).p;
				}
			}
		} while(n.distance(Point.ZERO) < .0001);

	
		return new Vertex(p, n.normalize(), u, v);
	}
	
	
	/**
	 * Uniformly tessellates this patch into quads.
	 * 
	 * @param step
	 *            The step size to use, between 0 and 1.0
	 * @return The list of quads
	 */
	public List<Quad> uniformTessellation(double step) {
		ArrayList<Quad> quads = new ArrayList<Quad>();

		for (double i = 0; i + step < 1 + 0.0001; i += step) {
			for (double j = 0; j + step < 1 + 0.0001; j += step) {
				Vertex[] p = new Vertex[4];
				p[0] = bezPatchInterp(i, j);
				p[1] = bezPatchInterp(i + step, j);
				p[2] = bezPatchInterp(i + step, j + step);
				p[3] = bezPatchInterp(i, j + step);
				quads.add(new Quad(p));
			}
		}

		return quads;
	}
	
	/*
	 * EVERYTHING BELOW THIS NEEDS SO MUCH CODE REVIEW
	 * UNLESS IT MAGICALLY WORKS + LAZY
	 */
	
	public List<Triangle> adaptiveTessellation(double error, double Umin, double Umax, double Vmin, double Vmax){
		ArrayList<Triangle> tris = new ArrayList<Triangle>();
		Vertex[] p = new Vertex[4];
		p[0] = bezPatchInterp(Umin, Vmin);
		p[1] = bezPatchInterp(Umax, Vmin);
		p[2] = bezPatchInterp(Umax, Vmax);
		p[3] = bezPatchInterp(Umin, Vmax);
		
		Vertex[] pTemp = new Vertex[3];
		pTemp[0] = p[0];
		pTemp[1] = p[1];
		pTemp[2] = p[2];
		tris.addAll(splitTriangle(pTemp, error));
		pTemp[0] = p[0];
		pTemp[1] = p[2];
		pTemp[2] = p[3];
		tris.addAll(splitTriangle(pTemp, error));
		
		return tris;
	}
	
	private List<Triangle> splitTriangle(Vertex[] p, double error){
		ArrayList<Triangle> tris = new ArrayList<Triangle>();
		boolean e1 = testEdge(error, p[0], p[1]);
		boolean e2 = testEdge(error, p[0], p[2]);
		boolean e3 = testEdge(error, p[1], p[2]);
		if(e1 && e2 && e3){
			tris.add(new Triangle(p));
			return tris;
		}
		else {
			
			//calc midpoints in uv
			Vertex p01 = bezPatchInterp((p[0].u + p[1].u)/2, (p[0].v + p[1].v)/2);
			Vertex p02 = bezPatchInterp((p[0].u + p[2].u)/2, (p[0].v + p[2].v)/2);
			Vertex p12 = bezPatchInterp((p[1].u + p[2].u)/2, (p[1].v + p[2].v)/2);
			Vertex[] tri = new Vertex[3];
			
			//such symmetries!
			if(!e1 && !e2 && !e3){
				tri[0] = p[0];
				tri[1] = p01;
				tri[2] = p02;
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p[1];
				tri[1] = p12;
				tri[2] = p01;
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p[2];
				tri[1] = p02;
				tri[2] = p12;
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p01;
				tri[1] = p12;
				tri[2] = p02;
				tris.addAll(splitTriangle(tri, error));
			} else if (e1 && !e2 && !e3){
				tri[0] = p[0];
				tri[1] = p[1];
				tri[2] = p02;
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p[1];
				tri[1] = p12;
				tri[2] = p02;
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p[2];
				tri[1] = p02;
				tri[2] = p12;
				tris.addAll(splitTriangle(tri, error));
			} else if (!e1 && e2 && !e3){
				Vertex temp = p[1];
				p[1] = p[0];
				p[0] = temp;
				tris.addAll(splitTriangle(p, error));
			} else if (!e1 && !e2 && e3){
				Vertex temp = p[2];
				p[2] = p[0];
				p[0] = temp;
				tris.addAll(splitTriangle(p, error));
			} else if (!e1 && e2 && e3){
				tri[0] = p[0];
				tri[1] = p01;
				tri[2] = p[2];
				tris.addAll(splitTriangle(tri, error));
				tri[0] = p01;
				tri[1] = p[1];
				tri[2] = p[2];
				tris.addAll(splitTriangle(tri, error));
			} else if (e1 && !e2 && e3){
				Vertex temp = p[1];
				p[1] = p[0];
				p[0] = temp;
				tris.addAll(splitTriangle(p, error));
			} else if (e1 && e2 && !e3){
				Vertex temp = p[2];
				p[2] = p[0];
				p[0] = temp;
				tris.addAll(splitTriangle(p, error));
			}
		}
		return tris;
	}
	
	private boolean testEdge(double error, Vertex x1, Vertex x2){
		//some of this is redundant but w.e
		return bezPatchInterp((x1.u+x2.u)/2, (x1.v+x2.v)/2).p.subtract(x1.p.midpoint(x2.p)).magnitude() < error;
	}

}
