/*******************************************************************************
 * Copyright (c) 2015 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This stream filter is used to replace opening and closing brackets with XML entities inside attribute values in the
 * xmldata section of a HTML Integrity result file. The reason for the existence of this (indeed rather strange-looking)
 * code is pretty much the same as the reason for the very similar code part in
 * de.gebit.integrity.runner.callbacks.xml.XmlWriterTestCallback.onExecutionFinish(TestModel, SuiteSummaryResult), which
 * performs this translation already during writing of the HTML file: Since the XSLT transformator is configured to
 * output HTML, it apparently outputs brackets as characters in the inline XML part. This part however should remain
 * conformant to strict XML, as it is intended to be parsed by XML parsers like the one used in the Jenkins Plugin. So
 * to get this desired output we need to replace these special characters with entities again to fix this particular
 * problem.
 * 
 * @author Rene Schneider - initial API and implementation
 *
 */
public class FilteringHTMLInputStream extends FilterInputStream {

	/**
	 * This tag opens the XML data.
	 */
	private final char[] triggerOpenTagName = new char[] { 'x', 'm', 'l', 'd', 'a', 't', 'a' };

	/**
	 * This tag closes the XML data.
	 */
	private final char[] triggerCloseTagName = new char[] { '/', 'x', 'm', 'l', 'd', 'a', 't', 'a' };

	/**
	 * Tag start char.
	 */
	private static final char TRIGGER_TAG_START = '<';

	/**
	 * Tag end char.
	 */
	private static final char TRIGGER_TAG_END = '>';

	/**
	 * Attribute data start char.
	 */
	private static final char TRIGGER_ATTRIBUTE = '"';

	/**
	 * Whether we are inside the XML part.
	 */
	private boolean insideXmlPart;

	/**
	 * Whether we are inside an attribute value.
	 */
	private boolean insideAttribute;

	/**
	 * Whether we are past the XML part.
	 */
	private boolean pastXmlPart;

	/**
	 * The current position inside a tag.
	 */
	private int tagPosition;

	/**
	 * Used to replace single characters with multi-character strings.
	 */
	private String replacement;

	/**
	 * Constructs a {@link FilteringHTMLInputStream}.
	 * 
	 * @param aStream
	 *            the stream to filter
	 */
	public FilteringHTMLInputStream(InputStream aStream) {
		super(aStream);
	}

	@Override
	public int read() throws IOException {
		if (replacement != null) {
			char tempChar = replacement.charAt(0);
			replacement = replacement.substring(1);
			if (replacement.length() == 0) {
				replacement = null;
			}

			return tempChar;
		} else {
			int tempInt = super.read();

			if (tempInt == -1) {
				return -1;
			}
			char tempChar = (char) tempInt;

			if (!pastXmlPart) {
				if (!insideAttribute) {
					if (tempChar == TRIGGER_TAG_START) {
						tagPosition = 0;
					} else if (tempChar == TRIGGER_TAG_END) {
						tagPosition = -1;
					} else if (tagPosition >= 0) {
						if (insideXmlPart && tempChar == TRIGGER_ATTRIBUTE) {
							insideAttribute = true;
						} else {
							tagPosition++;
							if (insideXmlPart) {
								if (tagPosition < triggerCloseTagName.length - 1) {
									if (tempChar != triggerCloseTagName[tagPosition]) {
										tagPosition = 0;
									}
								} else if (tagPosition == triggerCloseTagName.length - 1) {
									insideXmlPart = false;
									pastXmlPart = true;
									tagPosition = 0;
								}
							} else {
								if (tagPosition < triggerOpenTagName.length - 1) {
									if (tempChar != triggerOpenTagName[tagPosition]) {
										tagPosition = 0;
									}
								} else if (tagPosition == triggerOpenTagName.length - 1) {
									insideXmlPart = true;
									pastXmlPart = false;
									tagPosition = 0;
								}
							}
						}
					}
				} else {
					if (insideXmlPart) {
						if (tempChar == TRIGGER_ATTRIBUTE) {
							insideAttribute = false;
						} else {
							if (tempChar == '<') {
								replacement = "&lt;";
								return read();
							} else if (tempChar == '>') {
								replacement = "&gt;";
								return read();
							}
						}
					}
				}
			}

			return tempChar;
		}
	}

	@Override
	public int read(byte[] aBuffer, int anOffset, int aLength) throws IOException {
		int tempPos = 0;

		while (tempPos < aLength) {
			int tempByte = read();

			if (tempByte == -1) {
				return tempPos;
			} else {
				aBuffer[tempPos + anOffset] = (byte) tempByte;
				tempPos++;
			}
		}

		return tempPos;
	}

}
