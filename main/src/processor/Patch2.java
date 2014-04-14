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
	private Point bezCurveInterp(Point[] curve, double u) {
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

		return p;
	}

	private Point bezPatchInterp(double u, double v) {
		Point[] vcurve = new Point[4];

		// Build control points for a Bezier curve in v
		for (int i = 0; i < 4; i++) {
			vcurve[i] = bezCurveInterp(controls[i], u);
		}

		return bezCurveInterp(vcurve, v);
	}
	
	
	/**
	 * Uniformally tessellates this patch into quads.
	 * 
	 * @param step
	 *            The step size to use, between 0 and 1.0
	 * @return The list of quads (each represented by a Point[])
	 */
	public List<Quad> uniformTessellation(double step) {
		ArrayList<Quad> quads = new ArrayList<Quad>();

		for (double i = 0; i + step < 1 + 0.0001; i += step) {
			for (double j = 0; j + step < 1 + 0.0001; j += step) {
				Point[] p = new Point[4];
				p[0] = bezPatchInterp(i, j);
				p[1] = bezPatchInterp(i + step, j);
				p[2] = bezPatchInterp(i + step, j + step);
				p[3] = bezPatchInterp(i, j + step);
				quads.add(new Quad(p));
			}
		}

		return quads;
	}

}
