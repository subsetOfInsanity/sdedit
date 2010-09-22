// Copyright (c) 2006 - 2008, Markus Strauch.
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
// THE POSSIBILITY OF SUCH DAMAGE.

package net.sf.sdedit.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Markus Strauch
 */

public class DocUtil {
	private static DocumentBuilder documentBuilder;

	private static Transformer transformer;

	private static XPathFactory xPathFactory = XPathFactory.newInstance();

	private DocUtil() {
		/* empty */
	}

	static {
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			documentBuilder = factory.newDocumentBuilder();
			transformer = TransformerFactory.newInstance().newTransformer();
			factory.setIgnoringElementContentWhitespace(true);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String evalXPathAsString(Document document, String expression)
			throws XMLException {
		XPath xpath = xPathFactory.newXPath();
		try {
			return xpath.evaluate("/extension/description", document);
		} catch (XPathExpressionException xee) {
			throw new XMLException("Could not evaluate XPath: " + expression,
					xee);
		}
	}

	public static String evaluateCDATA(Document document, String xPath)
			throws XMLException {
		StringBuffer buffer = new StringBuffer();
		Element elem = (Element) evalXPathAsNode(document, xPath);
		if (elem != null) {
			NodeList children = elem.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i) instanceof CDATASection) {
					buffer.append(((CDATASection) children.item(i))
							.getTextContent());
				}
			}
		}
		return buffer.toString();
	}

	public static Node evalXPathAsNode(Document document, String expression)
			throws XMLException {
		XPath xpath = xPathFactory.newXPath();
		try {
			Node result = (Node) xpath.evaluate(expression, document,
					XPathConstants.NODE);
			return result;
		} catch (XPathExpressionException xee) {
			throw new XMLException("Could not evaluate XPath: " + expression,
					xee);
		}
	}

	public static NodeList evalXPathAsNodeList(Document document,
			String expression) throws XMLException {
		XPath xpath = xPathFactory.newXPath();
		try {
			return (NodeList) xpath.evaluate(expression, document,
					XPathConstants.NODESET);
		} catch (XPathExpressionException xee) {
			throw new XMLException("Could not evaluate XPath: " + expression,
					xee);
		}
	}

	/**
	 * Returns an empty Document.
	 * 
	 * @return an empty Document
	 */
	public static Document newDocument() {
		return documentBuilder.newDocument();
	}

	/**
	 * Writes the XML representation of a Document object.
	 * 
	 * @param document
	 *            the Document instance
	 * @param encoding
	 *            the encoding to be used
	 * @param out
	 *            the output stream to be used
	 * @throws XMLException
	 * @throws IOException
	 */

	public static void writeDocument(Document document, String encoding,
			OutputStream out) throws IOException, XMLException {
		OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		Source source = new DOMSource(document);
		Result result = new StreamResult(writer);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new XMLException("writeDocument failed", e);
		}
	}

	/**
	 * Creates a Document instance from a Reader reading an XML input stream.
	 * 
	 * @param in
	 *            an XML input stream
	 * @return the Document object created from the XML code
	 * @throws IOException
	 *             if the Reader cannot read
	 * @throws XMLException
	 */
	public static Document readDocument(InputStream in, String encoding)
			throws IOException, XMLException {
		InputStreamReader reader = new InputStreamReader(in, encoding);
		InputSource source = new InputSource(new BufferedReader(reader));
		Document document;
		try {
			document = documentBuilder.parse(source);
		} catch (SAXException e) {
			throw new XMLException("readDocument failed", e);
		}
		return document;
	}

	/**
	 * An <tt>XMLException</tt> is thrown when an XML document is not
	 * well-formed or not valid.
	 */
	public static class XMLException extends Exception {

		/**
		 * Constructor.
		 * 
		 * @param msg
		 *            a descriptive message
		 */
		// constructors are declared private because XMLExceptions are only
		// instantiated in DocUtil
		private XMLException(String msg) {
			super(msg);
		}

		/**
		 * Constructor.
		 * 
		 * @param msg
		 *            a descriptive message
		 * @param cause
		 *            the exception causing the XMLException
		 */
		private XMLException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
}
