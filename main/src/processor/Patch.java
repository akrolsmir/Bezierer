package processor;

import java.util.ArrayList;

/**
 * Class representing a Bezier patch
 *
 */
public class Patch {
	
	protected ArrayList<ArrayList<Point>> controls;
	
	/**
	 * Default constructor for a trivial patch (no control points)
	 */
	public Patch(){
		controls = new ArrayList<ArrayList<Point>>(0);
	}
	
	/**
	 * Constructs a patch which is the Bezier curve defined by points
	 * @param points the control points of a Bezier curve
	 */
	public Patch(ArrayList<Point> points){
		controls = new ArrayList<ArrayList<Point>>(1);
		controls.set(0, new ArrayList<Point>(points));
	}
	
	/**
	 * Adds a Bezier curve to the patch
	 * @param points the control points of a Bezier curve
	 */
	public void addCurve(ArrayList<Point> points){
		controls.add(new ArrayList<Point>(points));
	}
	
	public boolean equals(Object o){
		if(o instanceof Patch){
			ArrayList<ArrayList<Point>> oControls = ((Patch) o).controls;
			if(oControls.size() == controls.size()){
				for(int i = 0; i < controls.size(); i++){
					if(oControls.get(i).size() == controls.get(i).size()){
						for(int j = 0; j < controls.get(i).size(); j++){
							if(!controls.get(i).get(j).equals(oControls.get(i).get(j))){
								return false;
							}
						}
					} else {
						return false;
					}
				}
			} else {
				return false;
			}
			return true;
		}
		return false;
	}
}
