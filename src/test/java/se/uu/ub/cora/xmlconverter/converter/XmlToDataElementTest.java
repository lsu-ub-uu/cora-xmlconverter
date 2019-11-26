/*
 * Copyright 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.xmlconverter.converter;

import static org.testng.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class XmlToDataElementTest {

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: some message from DocumentBuilderFactorySpy")
	public void testParseExceptionOnCreateDocument() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";
		DocumentBuilderFactorySpy documentBuilderFactorySpy = new DocumentBuilderFactorySpy();
		documentBuilderFactorySpy.throwParserError = true;
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactorySpy);

		xmlToDataElement.convert(xmlToconvert);
	}

	@Test
	public void testParseExceptionOriginalExceptionIsSentAlong() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";
		DocumentBuilderFactorySpy documentBuilderFactorySpy = new DocumentBuilderFactorySpy();
		documentBuilderFactorySpy.throwParserError = true;
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactorySpy);

		try {
			xmlToDataElement.convert(xmlToconvert);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML")
	public void testSaxExceptionOnParseMalformedXML() {
		String xmlToconvert = "noXML";
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactory);

		xmlToDataElement.convert(xmlToconvert);
	}

	@Test
	public void testSaxExceptionOnParseMalformedXMLOriginalExceptionIsSentAlong() {
		String xmlToconvert = "noXML";
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactory);

		try {
			xmlToDataElement.convert(xmlToconvert);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof SAXException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: null")
	public void testExceptionOnNullXML() {
		String xmlToconvert = null;
		DocumentBuilderFactorySpy documentBuilderFactorySpy = new DocumentBuilderFactorySpy();
		documentBuilderFactorySpy.throwIOException = true;
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactorySpy);

		xmlToDataElement.convert(xmlToconvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML")
	public void testSaxExceptionOnParseEmptyXML() {
		String xmlToconvert = "";
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactory);

		xmlToDataElement.convert(xmlToconvert);
	}

	@Test
	public void testSaxExceptionOnParseEmptyXMLOriginalExceptionIsSentAlong() {
		String xmlToconvert = "";
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactory);

		try {
			xmlToDataElement.convert(xmlToconvert);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof SAXException);
		}
	}
}
