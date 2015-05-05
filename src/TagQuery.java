import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**************************************************************************
 * The {@code TagQuery} class.
 * 
 * @author Jonathan A. Henly
 **/
public class TagQuery {
	// TODO: Remove the line of debugging code following this
	private static PrintWriter log;
	

	// TODO: Remove the static block of debugging code following this
	static {
		try {
			log = new PrintWriter("./zout/out_QUERY");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * 
	 * @author Jonathan A. Henly
	 **/
	public static class InvalidQueryException extends Exception {
		private static final long serialVersionUID = -6002360782575126129L;
		
		public InvalidQueryException(String query, int index) {
			super("Parsing of the query: \"" + query + "\" failed at character index: " + index + "\n");
		}
	}
	
	private static enum QueryType {
		CLASS, ID, TAG, TAG_CLASS, TAG_ID
	}
	
	private Tag root;
	private Stack<Tag> result;
	private Stack<Tag> sub;
	private Stack<String> instructions;
	
	public TagQuery() {
		this.root = null;
		this.instructions = null;
		this.sub = null;
		this.result = new Stack<Tag>();
	}
	
	public TagQuery(FileReader file) {
		this();
		init(file);
	}
	
	public TagQuery(URL url) throws IOException {
		this();
		init(url.openStream());
	}
	
	public TagQuery(InputStream stream) {
		this();
		init(stream);
	}
	
	private void init(FileReader file) {
		StringBuilder sb = new StringBuilder();
		Scanner inFile = new Scanner(file);
		
		while(inFile.hasNextLine()) {
			sb.append(inFile.nextLine());
		}
		
		inFile.close();
		this.root = Tag.parseTags(sb);
	}
	
	private void init(InputStream stream) {
		StringBuilder sb = new StringBuilder();
		
		Scanner inFile = new Scanner(stream);
		while(inFile.hasNextLine()) {
			sb.append(inFile.nextLine());
		}
		
		inFile.close();
		this.root = Tag.parseTags(sb);
	}
	
	/**************************************************************************
	 * 
	 * 
	 * 
	 * @param file - 
	 * @return a reference to this {@code TagQuery} object.
	 **/
	public TagQuery loadSource(FileReader file) {
		init(file);
		return this;
	}
	
	/**************************************************************************
	 * 
	 * 
	 * 
	 * @param url - 
	 * @return a reference to this {@code TagQuery} object.
	 * @throws IOException
	 **/
	public TagQuery loadSource(URL url) throws IOException {
		init(url.openStream());
		return this;
	}
	
	/**************************************************************************
	 * 
	 * 
	 * 
	 * @param stream - 
	 * @return a reference to this {@code TagQuery} object.
	 * @throws IOException
	 **/
	public TagQuery loadSource(InputStream stream) {
		init(stream);
		return this;
	}
	
	/**************************************************************************
	 * 
	 * 
	 * 
	 * @param query - 
	 * @return a reference to this object.
	 * @throws InvalidQueryException 
	 **/
	public List<Tag> query(String query) throws InvalidQueryException {
		this.instructions = this.buildQuery(query);
		this.runInstructions();
		
		return this.getResult();
	}
	
	private Stack<String> buildQuery(String query) throws InvalidQueryException {
		Stack<String> tasks = new Stack<String>();
		String task = "";
		char[] command;
		boolean space = false;
		boolean comma = false;
		boolean dot = false;
		boolean hash = false;
		
		query = query.trim();
		command = query.toCharArray();
		for(int i = 0; i < query.length(); i++) {
			switch(command[i]) {
			case ' ':
				if(space || comma) {
					continue;
				}
				space = true;
				dot = false;
				hash = false;
				
				tasks.push(task);
				tasks.push(" ");
				task = "";
				break;
			
			case ',':
				if(comma || (i == query.length() - 1)) {
					throw new InvalidQueryException(query, i);
				}
				comma = true;
				space = false;
				dot = false;
				hash = false;
				
				tasks.push(task);
				tasks.push(",");
				task = "";
				break;
				
			case '.':
				if(dot) {
					throw new InvalidQueryException(query, i);
				}
				dot = true;
				
				task += command[i];
				break;
				
			case '#':
				if(hash) {
					throw new InvalidQueryException(query, i);
				}
				hash = true;
				
				task += command[i];
				break;
			
			default :
				space = false;
				comma = false;
				dot = false;
				hash = false;
				
				task += command[i];
				break;
			}
		}
		
		if(!task.isEmpty()) {
			tasks.push(task);
		}
		
		return tasks;
	}
	
	private void runInstructions() {
		String instruction = "";
		int height = 0;
		
		this.sub = new Stack<Tag>();
		while(this.instructions.size() > 0) {
			instruction = this.instructions.pop();
			if(" ".equals(instruction)) {
				height += 1;
			} else if(",".equals(instruction)) {
				height = 0;
				this.result.addAll(this.sub);
				this.sub = new Stack<Tag>();
			} else {
				if(height > 0) {
					this.runQuery(instruction, height);
				} else {
					this.runInitQuery(instruction);
				}
			}
		}
		this.result.addAll(this.sub);
	}
	
	private void runQuery(String q, int height) {
		if(q.indexOf('.') == q.indexOf('#')) {
			this.find(height, q, QueryType.TAG);
		} else if(q.indexOf('#') >= 0) {
			if(q.indexOf('#') == 0) {
				q = q.substring(1);
				this.find(height, q, QueryType.ID);
			} else {
				this.find(height, q, QueryType.TAG_ID);
			}
		} else if(q.indexOf('.') >= 0) {
			if(q.indexOf('.') == 0) {
				q = q.substring(1);
				this.find(height, q, QueryType.CLASS);
			} else {
				this.find(height, q, QueryType.TAG_CLASS);
			}
		}
	}
	
	private void runInitQuery(String q) {
		if(q.indexOf('.') == q.indexOf('#')) {
			this.findInit(this.root, q, QueryType.TAG);
		} else if(q.indexOf('#') >= 0) {
			if(q.indexOf('#') == 0) {
				q = q.substring(1);
				this.findInit(this.root, q, QueryType.ID);
			} else {
				this.findInit(this.root, q, QueryType.TAG_ID);
			}
		} else if(q.indexOf('.') >= 0) {
			if(q.indexOf('.') == 0) {
				q = q.substring(1);
				this.findInit(this.root, q, QueryType.CLASS);
			} else {
				this.findInit(this.root, q, QueryType.TAG_CLASS);
			}
		}
	}
	
	private void find(int height, String q, QueryType qt) {
		Stack<Tag> tmp = new Stack<Tag>();
		Tag t = null;
		Tag p = null;
		
		nullParent:
		while(!this.sub.isEmpty()) {
			t = this.sub.pop();
			p = t;
			for(int pCount = 0; pCount < height; pCount++) {
				if(p == null) {
					continue nullParent;
				}
				p = p.getParent();
			}
			
			switch(qt) {
			case TAG:
				if(q.equals(p.getName())) {
					tmp.push(t);
				}
				break;
			case CLASS:
				if(p.hasClass(q)) {
					tmp.push(t);
				}
				break;
			case ID:
				if(t.hasID(q)) {
					tmp.push(t);
				}
				break;
			case TAG_CLASS:
				if(p.getName().equals(q.substring(0, q.indexOf('.')))) {
					if(p.hasClass(q.substring(q.indexOf('.') + 1))) {
						tmp.push(t);
					}
				}
				break;
			case TAG_ID:
				if(p.getName().equals(q.substring(0, q.indexOf('#')))) {
					if(p.hasClass(q.substring(q.indexOf('#') + 1))) {
						tmp.push(t);
					}
				}
				break;
			}
		}
		
		this.sub = tmp;
	}
	
	private void findInit(Tag t, String q, QueryType qt) {
		if(t.hasChildren()) {
			List<Tag> c = t.getChildren();
			for(Tag child : c) {
				if(!child.isStartTag()) {
					continue;
				}
				this.findInit(child, q, qt);
			}
		}
		
		switch(qt) {
		case TAG:
			if(q.equals(t.getName())) {
				this.sub.push(t);
			}
			break;
		case CLASS:
			if(t.hasClass(q)) {
				this.sub.push(t);
			}
			break;
		case ID:
			if(t.hasID(q)) {
				this.sub.push(t);
			}
			break;
		case TAG_CLASS:
			if(t.getName().equals(q.substring(0, q.indexOf('.')))) {
				if(t.hasClass(q.substring(q.indexOf('.') + 1))) {
					this.sub.push(t);
				}
			}
			break;
		case TAG_ID:
			if(t.getName().equals(q.substring(0, q.indexOf('#')))) {
				if(t.hasClass(q.substring(q.indexOf('#') + 1))) {
					this.sub.push(t);
				}
			}
			break;
		}
	}
	
	public List<Tag> getResult() {
		return this.result;
	}
	
	public void reset() {
		this.result = new Stack<Tag>();
	}
}
