package se.uu.ub.cora.xmlconverter.converter;

import static org.testng.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class XmlToDataElementTest {

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement")
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
			+ "Unable to convert from xml to dataElement")
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
