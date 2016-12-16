/* $Id$ */

package ch.claninfo.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * 
 */
public class JSON2DOMTest {

	/**
	 * Test method for
	 * {@link ch.claninfo.json.JSON2DOM#parse(java.io.Reader, java.lang.String, org.xml.sax.ContentHandler)}.
	 * 
	 * @throws IOException
	 * @throws JSONParseException
	 * @throws SAXException
	 */
	@Test
	public void testParseReaderStringContentHandler() throws JSONParseException, IOException, SAXException {
		InputStream is = getClass().getResourceAsStream("EKSimExecuteResponse.json"); //$NON-NLS-1$
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(getClass().getResource("/ch/claninfo/pkone/pkOne-Meldungen.xsd")); //$NON-NLS-1$
		Validator validator = schema.newValidator();
		validator.validate(new DOMSource(JSON2DOM.parse(new InputStreamReader(is), "EKSimExecuteResponse"))); //$NON-NLS-1$
	}

}
