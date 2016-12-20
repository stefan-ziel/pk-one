/* $Id$ */

package ch.claninfo.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class JSON2DOM extends AbstractJSONParser {

	/** Namespace */
	public static final String NS = "http://www.claninfo.ch/2016/pkOne-Meldungen"; //$NON-NLS-1$
	private static final String ARRAY_NAME_ATTR = "array-name"; //$NON-NLS-1$
	private static DocumentBuilder builder;

	Document doc;
	Element curr;
	String name;

	private JSON2DOM(Reader pIn, String pName) throws IOException, ParserConfigurationException {
		super(pIn);
		if (builder == null) {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		doc = builder.newDocument();
		name = pName;
	}

	/**
	 * @param pIn input stream containing JSON Object
	 * @param pName Name of the Message
	 * @return Dom Document
	 * @throws JSONParseException invalid json
	 * @throws IOException
	 */
	public static Document parse(Reader pIn, String pName) throws JSONParseException, IOException {
		try {
			JSON2DOM p = new JSON2DOM(pIn, pName);
			p.parse();
			return p.doc;
		}
		catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void endArray() throws JSONParseException {
		curr.removeAttribute(ARRAY_NAME_ATTR);
	}

	@Override
	protected void endObject() throws JSONParseException {
		Node parent = curr.getParentNode();
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			curr = (Element) parent;
			name = curr.getAttribute(ARRAY_NAME_ATTR);
		} else {
			curr = null;
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
		curr.setAttribute(ARRAY_NAME_ATTR, name);
	}

	@Override
	protected void startObject() throws JSONParseException {
		try {
			Element obj = doc.createElementNS(NS, name);
			if (curr == null) {
				doc.appendChild(obj);
			} else {
				curr.appendChild(obj);
			}
			curr = obj;
		}
		catch (DOMException e) {
			throw new InvalidStructureException(curr == null ? doc : curr, name, line, col, e);
		}
	}

	@Override
	protected void value(Object pObject) throws JSONParseException {
		if (pObject != null) {
			try {
			String value;
			if (pObject instanceof Date) {
				value = STD_DATE_FORMAT.format(pObject);
			} else {
				value = pObject.toString();
			}

			if (curr.hasAttribute(name)) {
				curr.setAttribute(name, curr.getAttribute(name) + ' ' + value);
			} else if ("Content".equals(name)) { //$NON-NLS-1$
				curr.appendChild(doc.createTextNode(value));
			} else {
				curr.setAttribute(name, value);
			}
			}
			catch (DOMException e) {
				throw new InvalidStructureException(curr == null ? doc : curr, name, line, col, e);
			}
		}
	}

	static class InvalidStructureException extends JSONParseException {

		public InvalidStructureException(Node pParent, String pName, int pLine, int pColumn, DOMException pCause) {
			super(pCause.getMessage() + " appending '" + pName + "' to '" + pParent.getNodeName() + '\'', pLine, pColumn, pCause); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
