
public class Driver {
	
	private static final String html = "<body>\n<div class=\"hello\">\n</div>";
	
	public static void main(String[] args) {
		Driver d = new Driver();
		
		d.query("<*>");
	}
	
	public String query(String query) {
		String q = query.substring(0, query.indexOf('>') + 1).trim();
		String s = query.substring(query.indexOf('>') + 1).trim();
		
		System.out.println(q + "\n" + s);
		return "";
	}
}
