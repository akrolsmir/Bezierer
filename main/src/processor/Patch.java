package processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing a Bezier patch
 * 
 */
public class Patch {

	//protected ArrayList<ArrayList<Point>> controls;
	protected Point3D[][] controls = new Point3D[4][4];

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
	public void addCurve(Point3D[] curve) {
		controls[index] = curve;
		index++;
//		controls.add(new ArrayList<Point>(points));
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

	// given the control points of a bezier curve
	// and a parametric value, return the curve
	// point and derivative
	private static Point3D bezCurveInterp(Point3D[] curve, double u) {
		
		// System.out.println(Arrays.toString(curve));
		
		// first, split each of the three segments
		// to form two new ones AB and BC
		Point3D A = curve[0].multiply(1 - u).add(curve[1].multiply(u));
		Point3D B = curve[1].multiply(1 - u).add(curve[2].multiply(u));
		Point3D C = curve[2].multiply(1 - u).add(curve[3].multiply(u));

		// now, split AB and BC to form a new segment DE
		Point3D D = A.multiply(1 - u).add(B.multiply(u));
		Point3D E = B.multiply(1 - u).add(C.multiply(u));

		// finally, pick the right point on DE,
		// this is the point on the curve
		Point3D p = D.multiply(1 - u).add(E.multiply(u));
		
		return p;

//		// compute derivative also
//		Point3D dPdu = E.subtract(D).multiply(3);
//
//		// dPdu;
	}
	
	public static Point3D bezPatchInterp(Point3D[][] patch, double u, double v) {
		Point3D[] vcurve = new Point3D[4];
		
		// build control points for a Bezier curve in v
		for(int i = 0; i < 4; i++) {
			vcurve[i] = bezCurveInterp(patch[i], u);
			//ucurve[i] = bezCurveInterp(col(patch, i), u);
		}
		
		return bezCurveInterp(vcurve, v);
	}
	
	public List<Point3D[]> uniformTessellation( double step) {
		Point3D[][] patch = controls;
		ArrayList<Point3D[]> quads = new ArrayList<Point3D[]>();
		
		for(double i = 0; i + step < 1 + 0.0001; i += step) {
			for(double j = 0; j + step < 1 + 0.0001; j += step) {
				Point3D[] quad = new Point3D[4];
				quad[0] = bezPatchInterp(patch, i, j);
				quad[1] = bezPatchInterp(patch, i + step, j);
				quad[2] = bezPatchInterp(patch, i + step, j + step);
				quad[3] = bezPatchInterp(patch, i, j + step);
				quads.add(quad);
			}
		}
		
		return quads;
	}

}
