/* $Id$ */

package ch.claninfo.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 */
public class JSON2SAX extends AbstractJSONParser {

	/** Namespace */
	public static final String NS = "http://www.claninfo.ch/2016/pkOne-Meldungen"; //$NON-NLS-1$

	ContentHandler out;
	Stack<String> nameStack = new Stack<>();
	String name;

	private JSON2SAX(Reader pIn, String pName, ContentHandler pOut) throws IOException {
		super(pIn);
		out = pOut;
		name = pName;
	}

	/**
	 * @param pIn input stream containing JSON Object
	 * @param pName Name of the Message
	 * @param pOut Content handler to write events
	 * @throws JSONParseException invalid json
	 * @throws IOException
	 */
	public static void parse(Reader pIn, String pName, ContentHandler pOut) throws JSONParseException, IOException {
		JSON2SAX p = new JSON2SAX(pIn, pName, pOut);
		try {
			pOut.startDocument();
			p.parse();
			pOut.endDocument();
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void endArray() throws JSONParseException {
		// NOP
	}

	@Override
	protected void endObject() throws JSONParseException {
		try {
			name = nameStack.pop();
			out.endElement(NS, name, name);
		}
		catch (SAXException e) {
			reportError(e);
		}
	}

	@Override
	protected void name(String pName) throws JSONParseException {
		name = pName;
	}

	/**
	 * @see ch.claninfo.server.json.AbstractJSONParser#startArray()
	 */
	@Override
	protected void startArray() throws JSONParseException {
		// NOP
	}

	@Override
	protected void startObject() throws JSONParseException {
		try {
			nameStack.push(name);
			AttributesImpl attrs = new AttributesImpl();
			out.startElement(NS, name, name, attrs);
		}
		catch (SAXException e) {
			reportError(e);
		}
	}

	@Override
	protected void value(Object pObject) throws JSONParseException {
		if (pObject != null) {
			try {
				AttributesImpl attrs = new AttributesImpl();
				out.startElement(NS, name, name, attrs);
				String s = pObject.toString();
				out.characters(s.toCharArray(), 0, s.length());
				out.endElement(NS, name, name);
			}
			catch (SAXException e) {
				reportError(e);
			}
		}
	}
}
