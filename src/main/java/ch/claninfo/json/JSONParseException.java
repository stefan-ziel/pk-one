/* $Id$ */

package ch.claninfo.json;

public class JSONParseException extends Exception {

	public JSONParseException(int pLine, int pColumn, Throwable pCause) {
		super("Error at (" + pLine + ',' + pColumn + "): " + pCause.getMessage(), pCause);
	}

	public JSONParseException(String pText, int pLine, int pColumn) {
		super("Error at (" + pLine + ',' + pColumn + "): " + pText); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public JSONParseException(String pText, int pLine, int pColumn, Throwable pCause) {
		super("Error at (" + pLine + ',' + pColumn + "): " + pText, pCause); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
