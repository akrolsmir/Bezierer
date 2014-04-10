package processor;

/**
 * A point in 3d space
 *
 */
public class Point {
	
	protected double[] coords;
	
	/**
	 * Constructs a point at the origin
	 */
	public Point(){
		this(0.0,0.0,0.0);
	}
	
	/**
	 * Constructs a point at (x,y,z)
	 * @param x x-coordinate of the point
	 * @param y y-coordinate of the point
	 * @param z z-coordinate of the point
	 */
	public Point(double x, double y, double z){
		coords = new double[3];
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	/**
	 * Moves the point to (x,y,z)
	 * @param x new x-coordinate
	 * @param y new y-coordinate
	 * @param z new z-coordinate
	 */
	public void setCoordinates(double x, double y, double z){
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	/**
	 * Returns an array of the coordinates
	 * @return coordinates
	 */
	public double[] getCoordinates(){
		double[] result = {coords[0], coords[1], coords[2]};
		return result;
	}
	
	public boolean equals(Object o){
		if(o instanceof Point){
			double[] oCoords = ((Point) o).coords;
			return oCoords[0] == coords[0] && oCoords[1] == coords[1] && oCoords[2] == coords[2];
		}
		return false;
	}
	
}
