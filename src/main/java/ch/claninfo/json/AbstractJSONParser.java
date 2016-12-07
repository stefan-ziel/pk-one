/* $Id$ */

package ch.claninfo.json;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * parsing JSON objects
 */
public abstract class AbstractJSONParser {

	public static final DateFormat STD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS"); //$NON-NLS-1$

	int line;
	int col;
	Reader in;
	char next;
	boolean eof;

	/**
	 * @param pIn
	 * @throws IOException
	 */
	protected AbstractJSONParser(Reader pIn) throws IOException {
		super();
		in = pIn;
		line = 1;
		col = 1;
		next();
	}

	protected abstract void endArray() throws JSONParseException;

	protected abstract void endObject() throws JSONParseException;

	protected abstract void name(String pName) throws JSONParseException;

	protected void parse() throws JSONParseException, IOException {
		while (true) {
			char c = next();
			if (c == '{') {
				parseObject();
				return;
			} else if (!Character.isWhitespace(c)) {
				throw new InvalidCharException("{", c, line, col); //$NON-NLS-1$
			}
		}
	}

	protected void reportError(Throwable pError) throws JSONParseException {
		throw new JSONParseException(line, col, pError);
	}

	protected abstract void startArray() throws JSONParseException;

	protected abstract void startObject() throws JSONParseException;

	protected abstract void value(Object pObject) throws JSONParseException;

	void checkNext(char pExpected) throws JSONParseException, IOException {
		char c = getNext();
		if (c != pExpected) {
			throw new InvalidCharException(Character.toString(pExpected), c, line, col);
		}
	}

	private char getNext() throws IOException, JSONParseException {
		if (eof) {
			throw new EOFException(line, col);
		}
		return next();
	}

	private char next() throws IOException {
		char current = next;
		int r = in.read();
		if (r == -1) {
			next = 0;
			eof = true;
		} else {
			next = (char) r;
			eof = false;
			if (next == '\n') {
				line++;
				col = 1;
			} else {
				col++;
			}
		}
		return current;
	}

	private void parseArray() throws JSONParseException, IOException {
		startArray();
		while (true) {
			if (next == ']') {
				getNext();
				endArray();
				return;
			}
			if (Character.isWhitespace(next) || next == ',') {
				getNext();
			} else {
				parseValue();
			}
		}
	}

	private void parseFalse() throws JSONParseException, IOException {
		checkNext('a');
		checkNext('l');
		checkNext('s');
		checkNext('e');
		value(Boolean.FALSE);
	}

	private void parseNameValuePair(char pDelim) throws JSONParseException, IOException {
		name(parseString(pDelim));
		while (true) {
			char c = getNext();
			if (c == ':') {
				parseValue();
				return;
			}
			if (!Character.isWhitespace(c)) {
				throw new InvalidCharException(":", c, line, col); //$NON-NLS-1$
			}
		}
	}

	private void parseNull() throws JSONParseException, IOException {
		checkNext('u');
		checkNext('l');
		checkNext('l');
		value(null);
	}

	private void parseNumber(char pFirst) throws JSONParseException, IOException {
		StringBuffer res = new StringBuffer();
		res.append(pFirst);
		boolean isReal = pFirst == '.';
		int digits = Character.isDigit(pFirst) ? 1 : 0;
		boolean hasExponent = false;
		while (true) {
			if (Character.isDigit(next)) {
				res.append(getNext());
				digits++;
			} else if (next == '+' || next == '-') {
				res.append(getNext());
			} else if (next == 'e' || next == 'e') {
				res.append(getNext());
				isReal = true;
				hasExponent = true;
			} else if (next == '.') {
				res.append(getNext());
				isReal = true;
			} else {
				if (isReal) {
					if (hasExponent) {
						if (digits <= 9) {
							value(Float.valueOf(res.toString()));
						} else {
							value(Double.valueOf(res.toString()));
						}
					} else if (digits <= 9) {
						value(Float.valueOf(res.toString()));
					} else if (digits <= 15) {
						value(Double.valueOf(res.toString()));
					} else {
						value(new BigDecimal(res.toString()));
					}
				} else if (digits <= 2) {
					value(Byte.valueOf(res.toString()));
				} else if (digits <= 4) {
					value(Short.valueOf(res.toString()));
				} else if (digits <= 9) {
					value(Integer.valueOf(res.toString()));
				} else if (digits <= 17) {
					value(Long.valueOf(res.toString()));
				} else {
					value(new BigInteger(res.toString()));
				}
				return;
			}
		}
	}

	private void parseObject() throws JSONParseException, IOException {
		startObject();
		while (true) {
			char c = getNext();
			switch (c) {
				case '}':
					endObject();
					return;
				case '\'':
				case '"':
					parseNameValuePair(c);
					break;
				case ',':
					// continue
					break;
				default:
					if (!Character.isWhitespace(c)) {
						throw new InvalidCharException("}'\",", c, line, col); //$NON-NLS-1$
					}
			}
		}
	}

	private String parseString(char pDelim) throws JSONParseException, IOException {
		StringBuffer res = new StringBuffer();
		boolean escaped = false;
		while (true) {
			char c = getNext();
			if (escaped) {
				res.append(c);
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (c == pDelim) {
				return res.toString();
			} else {
				res.append(c);
			}
		}
	}

	private void parseStringOrDate(char pDelim) throws JSONParseException, IOException {
		String res = parseString(pDelim);
		// 2015-10-01T00:00:00.0
		// 0123456789012345678901
		if (res.length() >= 20 && res.charAt(4) == '-' && res.charAt(7) == '-' && res.charAt(10) == 'T' && res.charAt(13) == ':' && res.charAt(16) == ':' && res.charAt(19) == '.') {
			try {
				value(STD_DATE_FORMAT.parse(res));
				return;
			}
			catch (ParseException e) {
				// war einen Versuch wert aber was solls
			}
		}
		value(res);
	}

	private Boolean parseTrue() throws JSONParseException, IOException {
		checkNext('r');
		checkNext('u');
		checkNext('e');
		return Boolean.TRUE;
	}

	private void parseValue() throws JSONParseException, IOException {
		while (true) {
			char c = getNext();
			switch (c) {
				case '{':
					parseObject();
					return;
				case '[':
					parseArray();
					return;
				case '\'':
				case '"':
					parseStringOrDate(c);
					return;
				case 't':
					parseTrue();
					return;
				case 'f':
					parseFalse();
					return;
				case 'n':
					parseNull();
					return;
				case '-':
				case '+':
				case '.':
					parseNumber(c);
					return;
				default:
					if (Character.isDigit(c)) {
						parseNumber(c);
						return;
					}
					if (!Character.isWhitespace(c)) {
						throw new InvalidCharException("}'\",", c, line, col); //$NON-NLS-1$
					}
			}
		}
	}

	static class EOFException extends JSONParseException {

		public EOFException(int pLine, int pColumn) {
			super("Unexpected END of input.", pLine, pColumn); //$NON-NLS-1$
		}

	}

	static class InvalidCharException extends JSONParseException {

		public InvalidCharException(String pExpected, char pActual, int pLine, int pColumn) {
			super("Unexpected char '" + pActual + " expected one of the following: " + pExpected, pLine, pColumn); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
}
