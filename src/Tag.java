import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

/**************************************************************************
 * The {@code Tag} class.
 * 
 * @author Jonathan A. Henly
 **/
public class Tag {
	// TODO: Remove the line of debugging code following this
	private static PrintWriter log;

	// TODO: Remove the static block of debugging code following this
	static {
		try {
			log = new PrintWriter("./zout/out_TAG");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Tag parent;
	private List<Tag> children;
	private String tag;
	private HashSet<String> classes;
	private HashSet<String> ids;
	private HashMap<String, String> properties;
	private String contents;
	private boolean start;
	private int index;
	private int numChildren;

	/**************************************************************************
	 * Constructors
	 **/
	/**************************************************************************
	 * Default Constructor
	 **/
	private Tag() {
		parent = null;
		children = null;
		tag = "";
		classes = null;
		ids = null;
		properties = null;
		contents = "";
		start = false;
		index = Integer.MIN_VALUE;
		numChildren = 0;
	} // private Tag()

	/**************************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            - this tag's identifier. (i.e. {@code img}, {@code a},
	 *            {@code table}, etc.)
	 **/
	public Tag(String name) {
		this();
		this.tag = name;
	} // public Tag(String name)

	/**************************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            - this tag's identifier. (i.e. {@code img}, {@code a},
	 *            {@code table}, etc.)
	 * @param state
	 *            - true if this is an opening tag, false if this is a closing
	 *            tag.
	 **/
	public Tag(String name, boolean state) {
		this(name);
		this.start = state;
	} // public Tag(String name, boolean state)

	/**************************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            - this tag's identifier. (i.e. {@code img}, {@code a},
	 *            {@code table}, etc.)
	 * @param state
	 *            - true if this is an opening tag, false if this is a closing
	 *            tag.
	 * @param place
	 *            - the position of this tag relative to the other tags.
	 * @param data
	 *            - the data used to parse this tag.
	 **/
	public Tag(String name, boolean state, int place, String data) {
		this(name, state);
		this.index = place;
		this.parseTag(data);

		// TODO: Remove the 2 lines debugging code following this
		// log.write("[TAG]: " + this.tag + "\n" + this.getClasses()
		// 		+ this.getIDs() + this.getProps() + "\n");
		// log.flush();

	} // public Tag(String name, boolean state, int place, String data)

	/**************************************************************************
	 * Accessors and Mutators
	 **/
	/**************************************************************************
	 * Gets this {@code Tag} object's identifier.
	 * 
	 * @return this tag's identifier. (i.e. {@code img}, {@code a},
	 *         {@code table}, etc.)
	 **/
	public String getName() {
		return tag;
	} // public String getTag()

	/**************************************************************************
	 * Gets the number of first-level nested tags.
	 * <p>
	 * For example:
	 * <p>
	 * {@code <div class="one">}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <ul class="two">}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <li></li>}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <li></li>}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code </ul>}<br>
	 * {@code </div>}
	 * <p>
	 * The {@code <div>} tag with {@code class="one"} would only have one
	 * first-level child, the {@code <ul>} tag. While the {@code <ul>} tag with
	 * {@code class="two"} would have two first-level children, the two
	 * {@code <li>} tags.
	 * 
	 * 
	 * @return an `int` representing this {@code Tag} object's number of
	 *         first-level children.
	 **/
	public int getNumChildren() {
		if (children == null) {
			return 0;
		}

		return numChildren;
	} // public int getNumChildren()

	/**************************************************************************
	 * 
	 * 
	 * @param num
	 *            -
	 **/
	private void setNumChildren(int num) {
		numChildren = num;
	} // private void setNumChildren(int num)

	/**************************************************************************
	 * 
	 * 
	 * @return
	 **/
	protected int getIndex() {
		return index;
	} // protected int getIndex()

	/**************************************************************************
	 * Returns {@code true} if, and only if, this is a start tag.
	 * <p>
	 * If confused by the meaning of <em>start tag</em> then refer to the
	 * following table:
	 * <p>
	 * <style> th, td { margin: auto auto auto auto; text-align: center; width:
	 * 180px; } th { height: 20px; } td { height: 60px; } </style>
	 * <table summary="" border="1" style="margin: auto auto auto auto">
	 * <th>Start Tag</th>
	 * <th>End Tag</th>
	 * <tr>
	 * <td>{@code <a>}<br>
	 * {@code <div>}<br>
	 * {@code <img src="foo" />}</td>
	 * <td>{@code </a>}<br>
	 * {@code </div>}</td>
	 * </tr>
	 * </table>
	 * <p>
	 * <strong>Note:</strong> In the table, the backslash('/') character in the
	 * tag {@code <img src="foo" />} is not required in HTML, it's optional
	 * syntactic sugar.
	 * 
	 * @return {@code true} if this is a start tag and {@code false} if this is
	 *         an end tag.
	 **/
	public boolean isStartTag() {
		return this.start;
	} // public boolean isOpen()

	/**************************************************************************
	 * Sets this {@code Tag} object's parent tag.
	 * 
	 * @param parent
	 *            - a {@code Tag} object representing this {@code Tag} object's
	 *            parent tag.
	 **/
	protected void setParent(Tag parent) {
		this.parent = parent;
	} // public void setParent(Tag parent)

	/**************************************************************************
	 * Gets this {@code Tag} object's parent tag.
	 * <p>
	 * For example:<br>
	 * {@code <div>}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <ul>}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@code <li></li>}<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code </ul>}<br>
	 * {@code </div>}
	 * <p>
	 * In this example, the {@code <div>} tag would have a {@code null} parent.
	 * The {@code <ul>} tag's parent would be the {@code <div>} tag, and finally
	 * the {@code <li>} tag's parent would be the {@code <ul>} tag.
	 * 
	 * 
	 * @return a {@code Tag} object representing this {@code Tag} object's
	 *         parent tag, or {@code null} if this {@code Tag} object does not
	 *         have a parent tag.
	 **/
	public Tag getParent() {
		return parent;
	} // public Tag getParent()

	/**************************************************************************
	 * 
	 * 
	 * @param child
	 *            -
	 **/
	protected void addChild(Tag child) {
		if (children == null) {
			children = new LinkedList<Tag>();
		}

		children.add(child);
	} // public void addChild(Tag child)

	/**************************************************************************
	 * 
	 * 
	 * @param child
	 *            -
	 **/
	public List<Tag> getChildren() {
		return this.children;
	} // public void addChild(Tag child)

	/**************************************************************************
	 * 
	 * 
	 * @return
	 **/
	public boolean hasChildren() {
		return (children != null);
	} // public boolean hasChildren()

	/**************************************************************************
	 * 
	 * 
	 * @param which
	 *            -
	 * @return
	 **/
	public boolean hasClass(String which) {
		return classes == null ? false : classes.contains(which);
	} // public boolean hasClass(String which)

	/**************************************************************************
	 * 
	 * 
	 * @param which
	 *            -
	 * @return
	 **/
	public boolean hasID(String which) {
		return ids == null ? false : ids.contains(which);
	} // public boolean hasId(String which)

	/**************************************************************************
	 * 
	 * 
	 * @param which
	 *            - the property to inquire about. For example {@code href},
	 *            {@code src}, {@code style}, etc.
	 * @return {@code true} if this {@code Tag} object has the requested
	 *         property, otherwise {@code false}.
	 **/
	public boolean hasProperty(String which) {
		if (which.equals("class")) {
			return classes != null;
		} else if (which.equals("id")) {
			return ids != null;
		}

		return properties.containsKey(which);
	}

	/**************************************************************************
	 * 
	 * @param which
	 *            -
	 * @return
	 **/
	public String getProperty(String which) {
		if (properties == null) {
			return "";
		}

		return properties.get(which);
	} // public String getProperty(String which)

	/**************************************************************************
	 * TODO: Remove this debugging method.
	 * 
	 * @return
	 **/
	public String getClasses() {
		Iterator<String> it;
		String s = "";

		if (!(this.classes == null)) {
			it = this.classes.iterator();

			s += String.format("[Class]:%n");
			while (it.hasNext()) {
				s += String.format("%9s%s%n", " ", it.next());
			}
		}

		return s;
	} // public String getClasses()

	/**************************************************************************
	 * TODO: Remove this debugging method.
	 * 
	 * @return
	 **/
	public String getIDs() {
		Iterator<String> it;
		String s = "";

		if (!(this.ids == null)) {
			it = this.ids.iterator();

			s += String.format("[IDs]:%n");
			while (it.hasNext()) {
				s += String.format("%9s%s%n", " ", it.next());
			}
		}

		return s;
	} // public String getIDs()

	/**************************************************************************
	 * TODO: Remove this debugging method.
	 * 
	 * @return
	 **/
	public String getProps() {
		Iterator<String> it;
		String s = "";

		if (!(this.properties == null)) {
			it = this.properties.keySet().iterator();

			s += String.format("[Props]:%n");
			while (it.hasNext()) {
				String key = it.next();
				String val = this.properties.get(key);
				s += String.format("%9s[%s]=\"%s\"%n", " ", key, val);
			}
		}

		return s;
	} // public String getProps()

	/**************************************************************************
	 * Methods
	 **/
	/**************************************************************************
	 * 
	 * 
	 * @param data
	 *            -
	 **/
	private void parseTag(String data) {
		if (data.lastIndexOf('/') > data.lastIndexOf('"')) {
			data = data.substring(0, data.indexOf('/'));
		}

		this.parseProperties(data.trim());
	} // private void parseTag(String data)

	/**************************************************************************
	 * 
	 * 
	 * @param property
	 *            -
	 **/
	private void parseProperties(String property) {
		String key = "";
		String val = "";
		char cur = ' ';
		boolean quotes = false;

		for (int i = 0; i < property.length(); i++) {
			cur = property.charAt(i);
			switch (cur) {
			case ' ':
				if (!quotes) {
					continue;
				}
				val += cur;
				break;
			case '=':
				if (!quotes) {
					continue;
				}
				val += cur;
				break;
			case '"':
				if (quotes) {
					if (key.equals("class")) {
						this.parseClasses(val);
					} else if (key.equals("id")) {
						this.parseIds(val);
					} else {
						if (this.properties == null) {
							this.properties = new HashMap<String, String>();
						}
						this.properties.put(key.trim(), val.trim());
					}

					key = "";
					val = "";
					quotes = false;
				} else {
					quotes = true;
				}
				break;
			default:
				if (quotes) {
					val += cur;
				} else {
					key += cur;
				}
			}

		}

	} // private void addProperty(String property)

	/**************************************************************************
	 * 
	 * 
	 * @param classStr
	 *            -
	 **/
	private void parseClasses(String classStr) {
		this.classes = new HashSet<String>();
		if (classStr.trim().isEmpty()) {
			this.classes.add("");
		} else {
			classStr = classStr + " ";
			do {
				String tmp = classStr.substring(0, classStr.indexOf(' '));
				this.classes.add(tmp.trim());
				classStr = classStr.substring(classStr.indexOf(' ') + 1);
			} while (!classStr.trim().isEmpty());
		}
	} // private void parseClasses(String classStr)

	/**************************************************************************
	 * 
	 * 
	 * @param idStr
	 *            -
	 **/
	private void parseIds(String idStr) {
		this.ids = new HashSet<String>();
		if (idStr.trim().isEmpty()) {
			this.ids.add("");
		} else {
			idStr = idStr + " ";
			do {
				String tmp = idStr.substring(0, idStr.indexOf(' '));
				this.ids.add(tmp.trim());
				idStr = idStr.substring(idStr.indexOf(' ') + 1);
			} while (!idStr.trim().isEmpty());
		}
	} // private void parseIds(String idStr)

	public String toString() {
		Iterator<String> it;
		StringBuilder sb = new StringBuilder("");
		
		sb.append('<');
		if (!this.isStartTag()) {
			sb.append('/');
		}
		sb.append(tag);
		
		if(classes != null) {
			sb.append(" class=\"");
			it = classes.iterator();
			while(it.hasNext()) {
				sb.append(it.next());
				if(it.hasNext()) {
					sb.append(' ');
				}
			}
			
			sb.append('"');
		}
		
		if(ids != null) {
			sb.append(" id=\"");
			it = ids.iterator();
			while(it.hasNext()) {
				sb.append(it.next());
				if(it.hasNext()) {
					sb.append(' ');
				}
			}
			
			sb.append('"');
		}
		
		if(properties != null) {
			sb.append(' ');
			Iterator<Entry<String, String>> pit = properties.entrySet().iterator();
			while(pit.hasNext()) {
				sb.append(pit.next().getKey()).append('=').append('"').append(pit.next().getValue()).append('"');
				if(pit.hasNext()) {
					sb.append(' ');
				}
			}
		}
		
		sb.append('>');

		return sb.toString();
	}

	/**************************************************************************
	 * 
	 * 
	 * @param data
	 *            -
	 * @return
	 **/
	protected static Tag parseTags(StringBuilder data) {
		Stack<Tag> tags = new Stack<Tag>();
		Tag root;
		Tag tmp;
		String name = "";
		String body = "";
		boolean oStart = false;
		boolean cStart = false;
		boolean isClosing = false;
		boolean nameEnd = false;
		boolean quotes = false;
		boolean selfClosing = false;
		boolean tagDone = false;
		int index = -1;
		int rootNumChildren = 0;

		root = new Tag();
		tags.add(root);

		for (int i = 0; i < data.length() - 1; i++) {
			char current = data.charAt(i);

			if (tagDone) {
				tagDone = false;

				if (selfClosing) {
					tmp = new Tag(name, false, index, body);
				} else {
					tmp = new Tag(name, true, index, body);
				}

				if (selfClosing) {
					tmp.setParent(tags.peek());
					tmp.getParent().addChild(tmp);
					rootNumChildren += 1;
				} else if (isClosing) {
					tmp.setParent(tags.pop().getParent());
					tmp.getParent().addChild(tmp);
				} else {
					tmp.setParent(tags.peek());
					tmp.getParent().addChild(tmp);
					tags.push(tmp);
					rootNumChildren += 1;
				}

				name = "";
				body = "";
			}

			switch (current) {
			case '!':
				if (quotes) {
					break;
				}
				if (oStart || cStart) {
					oStart = false;
					cStart = false;
				}
				break;
			case '<':
				if (quotes) {
					break;
				}
				index++;
				oStart = true;
				nameEnd = false;
				isClosing = false;
				selfClosing = false;
				continue;

			case '>':
				if (quotes || (!oStart && !cStart)) {
					break;
				}
				oStart = false;
				cStart = false;
				nameEnd = true;
				tagDone = true;
				continue;

			case '/':
				if (quotes || (!oStart && !cStart)) {
					break;
				}
				if (nameEnd) {
					selfClosing = true;
				} else if (oStart) {
					oStart = false;
					cStart = true;
				}

				isClosing = true;

				continue;

			case '"':
				quotes = !quotes;
				break;

			case ' ':
				if (oStart || cStart) {
					nameEnd = true;
				}
				break;
			}

			if (oStart || cStart) {
				if (!nameEnd) {
					name += current;
				} else {
					body += current;
				}
			}

		}

		root.setNumChildren(rootNumChildren);

		return root;
	} // public static Tag parseTags(String data)

}
