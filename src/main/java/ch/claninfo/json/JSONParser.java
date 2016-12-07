/* $Id$ */

package ch.claninfo.json;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Creates a structure of nested maps and lists to represent a JSON object
 */
public class JSONParser extends AbstractJSONParser {

	Map<String, Object> root;
	Stack<Object> objects = new Stack<Object>();
	String currentName;

	private JSONParser(Reader pIn) throws IOException {
		super(pIn);
	}

	/**
	 * @param pIn input stream containing JSON Object
	 * @return a JSON Object as Map of Key Value pairs
	 * @throws JSONParseException invalid json
	 * @throws IOException
	 */
	public static Map<String, Object> parse(Reader pIn) throws JSONParseException, IOException {
		JSONParser p = new JSONParser(pIn);
		p.parse();
		return p.root;
	}

	@Override
	protected void endArray() {
		objects.pop();
	}

	@Override
	protected void endObject() {
		if (!objects.empty()) {
			objects.pop();
		}
	}

	@Override
	protected void name(String pName) {
		currentName = pName;
	}

	/**
	 * @see ch.claninfo.common.json.server.json.AbstractJSONParser#startArray()
	 */
	@Override
	protected void startArray() {
		List<Object> array = new LinkedList<Object>();
		value(array);
		objects.push(array);
	}

	@Override
	protected void startObject() {
		Map<String, Object> object = new LinkedHashMap<String, Object>();
		value(object);
		objects.push(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void value(Object pObject) {
		if (objects.isEmpty()) {
			root = (Map<String, Object>) pObject;
		} else {
			Object topOfStack = objects.peek();
			if (topOfStack instanceof List) {
				((List<Object>) topOfStack).add(pObject);
			} else {
				((Map<String, Object>) topOfStack).put(currentName, pObject);
			}
		}
	}
}
