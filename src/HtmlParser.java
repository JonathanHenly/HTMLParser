
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {
	private LinkedList<HtmlTag> tagList = null;
	private HashSet<String> validTags = null;
	private HashSet<String> invalidText = null;

	public class HtmlTag {
		private HashMap<String, String> attributes = null;
		private String tag = null;
		private String content = null;

		private HtmlTag() {
			this.attributes = new HashMap<String, String>();
			this.tag = "";
			this.content = "";
		}

		public HtmlTag(String tag) {
			this();
			this.tag = tag;
		}

		public String getTag() {
			return this.tag;
		}

		public String getContent() {
			return this.content;
		}

		private void setContent(String content) {
			this.content = content;
		}

		private void addAttribute(String attr, String value) {
			this.attributes.put(attr, value);
		}

		public String getAttribute(String attr) {
			if (this.attributes.containsKey(attr)) {
				return this.attributes.get(attr);
			} else {
				return "";
			}
		}
		
		public String getFileName(String attr) {
			String s = "";
			
			if (this.attributes.containsKey(attr)) {
				s = this.attributes.get(attr);
				s = s.substring(s.lastIndexOf('/') + 1);
				return s;
			} else {
				return s;
			}
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();

			s.append("<").append(this.getTag()).append(" ");
			for (String attr : this.attributes.keySet()) {
				s.append(attr).append("=\"").append(this.attributes.get(attr))
						.append("\" ");
			}
			s.append(">");

			return s.toString();
		}

	}

	
	public HtmlParser() {
		this.tagList = new LinkedList<HtmlTag>();
		this.validTags = new HashSet<String>();
		this.invalidText = new HashSet<String>();
	}
	
	public String query(String query) {
		String q = query.substring(0, query.indexOf('>'));
		// String s = query.substring()
		
		return "";
	}

	public HtmlParser setTags(String tags) {
		this.parseSetters(tags, this.validTags);
		return this;
	}

	public HtmlParser setAvoidText(String text) {
		this.parseSetters(text, this.invalidText);
		return this;
	}
	
	public LinkedList<HtmlTag> getParsedTags() {
		return this.tagList;
	}

	private boolean parseSetters(String data, HashSet<String> set) {
		char delimeter = ' ';

		if (data.indexOf(',') != -1) {
			delimeter = ',';
		}

		data = delimeter + data.trim();
		while (!data.isEmpty()) {
			int lastIndex = data.lastIndexOf(delimeter);
			String tmp = data.substring(lastIndex + 1);
			set.add(tmp);
			data = data.substring(0, lastIndex);
		}

		return true;
	}

	public void parse(InputStream instream)
			throws UnsupportedEncodingException, IOException {
		int read = -1;
		byte[] buffer = new byte[5 * 1024];
		byte[] readData;
		StringBuilder htmlData = new StringBuilder();
		String readDataText;

		while ((read = instream.read(buffer)) > -1) {
			readData = new byte[read];
			System.arraycopy(buffer, 0, readData, 0, read);
			readDataText = new String(readData, "UTF-8");
			htmlData.append(readDataText);
		}
		
		parse(htmlData.toString());
	}

	public void parse(String html) {
		Scanner htmlData = new Scanner(html);
		String line = "";

		while (htmlData.hasNextLine()) {
			line = htmlData.nextLine().trim();
			if (line.isEmpty()) {
				continue;
			}
			this.parseLine(line);
		}

		htmlData.close();
	}

	private void parseLine(String line) {
		HtmlTag tag = null;
		Matcher m = Pattern.compile("<([^<])+").matcher(line);

		while (m.find()) {
			tag = parseTag(m.group());
			if (tag != null) {
				this.tagList.add(tag);
			}
		}

	}

	private HtmlTag parseTag(String strTag) {
		HtmlTag tag = null;
		
		// TODO Fix this mangled code up...
		if (strTag.charAt(0) == '<' && strTag.charAt(1) != '/' && strTag.charAt(1) != '!' && strTag.indexOf(' ') > 0) {
			String tmp = strTag.substring(1, strTag.indexOf(' '));
			if (isValidTag(tmp)) {
				tag = new HtmlTag(tmp);
				strTag = strTag.substring(strTag.indexOf(' ')).trim();
				
				while (strTag.charAt(0) != '>') {
					String key = "";
					String value = "";
					int index = strTag.indexOf('=');
					
					// TODO Fix this mangled code up...
					if(index < 0) {
						break;
					}
					key = strTag.substring(0, index).trim();
					strTag = strTag.substring(strTag.indexOf('"') + 1);
					value = strTag.substring(0, strTag.indexOf('"'));
					strTag = strTag.substring(strTag.indexOf('"') + 1);
					
					if(isAvoidText(key) || isAvoidText(value)) {
						return null;
					}
					
					tag.addAttribute(key, value);
				}
				strTag = strTag.substring(strTag.indexOf('>') + 1).trim();
				tag.setContent(strTag);
			}
		}

		return tag;
	}

	private boolean isValidTag(String tag) {
		return this.validTags.contains(tag);
	}

	private boolean isAvoidText(String text) {
		return this.invalidText.contains(text);
	}

}
