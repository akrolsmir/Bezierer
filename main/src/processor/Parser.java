package processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Parser {
	
	private static final Charset charset = StandardCharsets.UTF_8;
	private static int lineNum = 1;
		
	/**
	 * File management code taken from http://docs.oracle.com/javase/tutorial/essential/io/file.html
	 * @throws IOException
	 */
	public static ArrayList<Patch> read(Path file){
		ArrayList<Patch> patches = new ArrayList<Patch>();
		lineNum = 1;
		try(BufferedReader reader = Files.newBufferedReader(file, charset)){
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
		}
		return patches;
	}
	
	public static Patch read_patch(BufferedReader reader) 
			throws IOException, ArrayIndexOutOfBoundsException{
		Patch newPatch = new Patch();
		String line;
		for(int i = 0; i < 4; i++){
			lineNum++;
			while((line = reader.readLine()).matches("\\s*")){
				lineNum++;
			}
			String[] in = line.trim().split("\\s+");
			ArrayList<Point> curve = new ArrayList<Point>();
			for(int j = 0; j < 12; j+=3){
				curve.add(new Point(
						Double.parseDouble(in[j]), Double.parseDouble(in[j+1]), Double.parseDouble(in[j+2])));
			}
			newPatch.addCurve(curve);
			
		}
		return newPatch;
	}
	
}
