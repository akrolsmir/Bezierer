package processor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
	
	private static int lineNum = 1;
		
	/**
	 * Use for Android parsing
	 */
	public static ArrayList<Patch> read(BufferedReader reader){
		ArrayList<Patch> patches = new ArrayList<Patch>();
		lineNum = 1;
		try {
			int numPatches = Integer.parseInt(reader.readLine());
			while(numPatches-- > 0){
				patches.add(read_patch(reader));
			}
		} catch (IOException | ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Invalid read.");
			System.exit(1);
		} catch (NumberFormatException e){
			e.printStackTrace();
			System.err.println("Line " + lineNum + ": Expected double.");
			System.exit(1);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return patches;
	}
	
	
	/**
	 * Use for JOGL parsing
	 * 
	 * @param filename
	 * @return
	 */
	public static ArrayList<Patch> read(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			return Parser.read(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static Patch read_patch(BufferedReader reader) 
			throws IOException, ArrayIndexOutOfBoundsException{
		Patch newPatch = new Patch();
		String line;
		for(int i = 0; i < 4; i++){
			lineNum++;
			while((line = reader.readLine()).matches("\\s*")){
				lineNum++;
			}
			String[] in = line.trim().split("\\s+");
			newPatch.addCurve(new Point[]{
					new Point(Double.parseDouble(in[0]), Double.parseDouble(in[1]), Double.parseDouble(in[2])),
					new Point(Double.parseDouble(in[3]), Double.parseDouble(in[4]), Double.parseDouble(in[5])),
					new Point(Double.parseDouble(in[6]), Double.parseDouble(in[7]), Double.parseDouble(in[8])),
					new Point(Double.parseDouble(in[9]), Double.parseDouble(in[10]), Double.parseDouble(in[11]))
			});
			
		}
		return newPatch;
	}
	
}
