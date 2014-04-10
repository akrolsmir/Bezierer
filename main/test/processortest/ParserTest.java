package processortest;

import static org.junit.Assert.*;

import java.nio.file.FileSystems;
import java.util.ArrayList;

import org.junit.Test;

import processor.Parser;
import processor.Patch;
import processor.Point;

public class ParserTest {

	@Test
	public void testArch() {
		ArrayList<Patch> cmp = new ArrayList<Patch>();
		Patch patch;
		ArrayList<Point> curve;
		
		//first patch
		patch = new Patch();
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.00, 0.00));
		curve.add(new Point(0.33, 0.00, 0.00));
		curve.add(new Point(0.66, 0.00, 0.00));
		curve.add(new Point(1.00, 0.00, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.33, 0.00));
		curve.add(new Point(0.33, 0.33, 0.00));
		curve.add(new Point(0.66, 0.33, 0.00));
		curve.add(new Point(1.00, 0.33, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.66, 0.00));
		curve.add(new Point(0.33, 0.66, 0.00));
		curve.add(new Point(0.66, 0.66, 0.00));
		curve.add(new Point(1.00, 0.66, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 1.00, 0.00));
		curve.add(new Point(0.33, 1.00, 0.00));
		curve.add(new Point(0.66, 1.00, 0.00));
		curve.add(new Point(1.00, 1.00, 0.00));
		patch.addCurve(curve);
		cmp.add(patch);
		
		//second patch
		patch = new Patch();
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.00, 0.00));
		curve.add(new Point(0.33, 0.00, 2.00));
		curve.add(new Point(0.66, 0.00, 2.00));
		curve.add(new Point(1.00, 0.00, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.33, 0.00));
		curve.add(new Point(0.33, 0.33, 2.00));
		curve.add(new Point(0.66, 0.33, 2.00));
		curve.add(new Point(1.00, 0.33, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 0.66, 0.00));
		curve.add(new Point(0.33, 0.66, 2.00));
		curve.add(new Point(0.66, 0.66, 2.00));
		curve.add(new Point(1.00, 0.66, 0.00));
		patch.addCurve(curve);
		curve = new ArrayList<Point>(); 
		curve.add(new Point(0.00, 1.00, 0.00));
		curve.add(new Point(0.33, 1.00, 2.00));
		curve.add(new Point(0.66, 1.00, 2.00));
		curve.add(new Point(1.00, 1.00, 0.00));
		patch.addCurve(curve);
		cmp.add(patch);
		
		ArrayList<Patch> cmp2 = Parser.read(FileSystems.getDefault().getPath("arch.bez"));
		assertEquals(cmp.get(0), cmp2.get(0));
		assertEquals(cmp.get(1), cmp2.get(1));
	}

}
