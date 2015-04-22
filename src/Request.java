import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Representation of a RequestConnection.
 * 
 * @author Jonathan A. Henly
 **/
public final class Request {
	private final URL url;
	private final HttpURLConnection con;
	private final List<String> cookies;
	private final HashMap<String, String> props;
	private final HtmlParser htmlParser;

	private Request(final URL newURL, final HttpURLConnection newCon,
			final List<String> newCookies, final HtmlParser newHtmlParser) {
		System.setProperty("http.keepAlive", "true");

		this.url = newURL;
		this.con = newCon;
		this.cookies = newCookies;
		this.props = new HashMap<String, String>();
		this.htmlParser = newHtmlParser;
	}
	
	public String[] getURLs() {
		LinkedList<HtmlParser.HtmlTag> list = this.htmlParser.getParsedTags();
		ListIterator<HtmlParser.HtmlTag> iter = list.listIterator();
		String[] urls = new String[list.size()];
		int index = 0;
		
		while (iter.hasNext()) {
			HtmlParser.HtmlTag tag = iter.next();
			String src = tag.getAttribute("src");
			
			urls[index] = src;
			index += 1;
		}
		
		return urls;
	}

	public void test(String directory) {
		LinkedList<HtmlParser.HtmlTag> list = this.htmlParser.getParsedTags();
		ListIterator<HtmlParser.HtmlTag> iter = list.listIterator();
		Calendar today = Calendar.getInstance(Locale.US);
		String date = (today.get(Calendar.MONTH) + 1) + "-"
				    + today.get(Calendar.DAY_OF_MONTH) + "-"
				    + today.get(Calendar.YEAR);
		String path = directory + date + "/";
		File dir = new File(path);
		int dirCount = 1;

		if (!dir.exists()) {
			dir.mkdir();
			dir = new File(path + dirCount + "/");
			dir.mkdir();
		} else {
			while(dir.exists()) {
				dir = new File(path + dirCount + "/");
				dirCount += 1;
			}
			dir.mkdir();
		}

		while (iter.hasNext()) {
			HtmlParser.HtmlTag tag = iter.next();
			String src = tag.getAttribute("src").replaceAll("_t", "_o");
			String filename = tag.getFileName("src").replaceAll("_t", "");
			String content = tag.getAttribute("title");
			
			System.out.println(src + "\n" + content);
			
			try {
				URL u = new URL(src);
				BufferedInputStream in = new BufferedInputStream(u
						.openConnection().getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(dir.getAbsolutePath() + "/" + filename));

				// keep saving into file until end of data
				int i;
				while ((i = in.read()) != -1) {
					out.write(i);
				}
				out.flush();

				// close all the streams
				out.close();
				in.close();
			} catch (MalformedURLException e) {
				System.out.println(src + " is not an image file.");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}

	/**
	 * Gets the URL's address of this <code>Request</code>.
	 * 
	 * @return a <code>String</code> representation of the URL's address
	 */
	public String getAddress() {
		return this.url.getPath();
	}

	/**
	 * Returns an unmodifiable <code>List&ltString&gt</code> of the cookies.
	 * 
	 * @return a <code>List&ltString&gt</code> containing the cookies
	 **/
	public List<String> getCookies() {
		return (this.cookies != null) ? this.cookies : new LinkedList<String>();
	}

	/**
	 * Adds a general request property specified by a key-value pair. This
	 * method will not overwrite existing values associated with the same key.
	 * 
	 * @param key
	 *            - the keyword by which the request is known (e.g., "Accept").
	 * @param value
	 *            - the value associated with it.
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public Request addProperty(String key, String value) {
		if (this.props.containsKey(key)) {
			String check = this.props.get(key);
			for (String tmp : check.split(",")) {
				if (value.equals(tmp)) {
					return this;
				}
			}

			this.props.put(key, check + "," + value);
		} else {
			this.props.put(key, value);
		}

		return this;
	}

	/**
	 * Overwrites a general request property specified by a key-value pair. If
	 * the key-value pair does not exist then this method will add a general
	 * request property specified by a key-value pair.
	 * 
	 * @param key
	 *            - the keyword by which the request is known (e.g., "Accept").
	 * @param value
	 *            - the value associated with it.
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public Request setProperty(String key, String value) {
		this.props.put(key, value);

		return this;
	}

	/**
	 * Removes a general request property specified by a key-value pair.
	 * <p>
	 * If the specified key has more than one value associated with it then this
	 * method will only remove the specified value. Leaving the remaining values
	 * associated with said key.
	 * 
	 * @param key
	 *            - the keyword by which the request is known (e.g., "Accept")
	 * @param value
	 *            - the value,to remove, associated with it
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public Request removeProperty(String key, String value) {
		if (this.props.containsKey(key)) {
			String check[] = this.props.get(key).split(",");
			if (check.length <= 1) {
				this.props.remove(key);
			} else {
				String result = "";
				for (int i = 0; i < check.length; i++) {
					if (!value.equals(check[i])) {
						result += check[i];
					}
					if (i < check.length - 1) {
						result += ',';
					}
				}
				this.props.put(key, result);
			}
		}

		return this;
	}

	/**
	 * Removes a general request property specified by a key.
	 * <p>
	 * This method will remove a general request property specified by a key,
	 * even if the key has multiple comma separated values.
	 * 
	 * @param key
	 *            - the keyword by which the request is known (e.g., "Accept")
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public Request removeProperty(String key) {
		if (this.props.containsKey(key)) {
			this.props.remove(key);
		}

		return this;
	}

	/**
	 * Removes every general request property.
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public Request clearProperties() {
		this.props.clear();
		return this;
	}

	/**
	 * Removes every general request property.
	 * 
	 * @return this <code>Request</code> instance
	 **/
	public void makeRequest() {

	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode());
	}

	/**
	 * Representation of a RequestConnection.
	 * 
	 * @author Jonathan A. Henly
	 **/
	public static class RequestBuilder {
		private final URL nestedURL;
		private final HttpURLConnection nestedCon;
		private final HtmlParser nestedHtmlParser;
		private List<String> nestedCookies;

		public RequestBuilder(final String address) throws IOException {
			this.nestedURL = new URL(address);
			this.nestedCon = (HttpURLConnection) this.nestedURL
					.openConnection();
			this.nestedHtmlParser = new HtmlParser();
			this.nestedCookies = null;
		}

		public RequestBuilder withCookies() {
			// set the default cookie manager
			CookieHandler.setDefault(new CookieManager(null,
					CookiePolicy.ACCEPT_ALL));
			this.nestedCookies = this.nestedCon.getHeaderFields().get(
					"Set-Cookie");

			try {
				this.nestedHtmlParser.setTags("img");
				this.nestedHtmlParser.setAvoidText("/gfx/logon_new.jpg");
				this.nestedHtmlParser.parse(this.nestedCon.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return this;
		}

		public Request build() {
			return new Request(this.nestedURL, this.nestedCon,
					this.nestedCookies, this.nestedHtmlParser);
		}
	}
}