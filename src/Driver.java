import java.io.*;
import java.util.*;

public class Driver {
	private static FileReader inFile;
	
	static {
		try {
			inFile = new FileReader("./zin/Example.html");
		} catch (FileNotFoundException e) {}
	}
	
	
	
	public static void main(String[] args) {
		List<Tag> t = null;
		TagQuery q = null;
		
		q = new TagQuery(inFile);
		
		try {
			t = q.query("div");
		} catch (TagQuery.InvalidQueryException e) {
			e.printStackTrace();
		}
		
		for(Tag tmp : t) {
			System.out.println(tmp.toString());
			// System.out.print("[TAG]: " + tmp.getName() + "\n" + tmp.getClasses() + tmp.getIDs() + tmp.getProps());
		}
		
	}
	
}
