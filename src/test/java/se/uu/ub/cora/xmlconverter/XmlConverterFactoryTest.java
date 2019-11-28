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

package se.uu.ub.cora.xmlconverter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.testng.annotations.Test;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterFactory;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.xmlconverter.converter.XmlConverterException;
import se.uu.ub.cora.xmlconverter.converter.XmlToDataElement;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;

public class XmlConverterFactoryTest {

	@Test
	public void testXmlConverterFactoryImplementsConverterFactory() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		assertTrue(xmlConverterFactory instanceof ConverterFactory);

	}

	@Test
	public void testFactorConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		Converter factoredConverter = xmlConverterFactory.factorConverter();
		assertTrue(factoredConverter instanceof XmlConverter);
	}

	@Test
	public void testFactoryName() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		assertEquals(xmlConverterFactory.getName(), "xml");
	}

	@Test
	public void testXmlConverterFactorySendsCorrectFactoriesToConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		assertTrue(factorConverter.getDocumentBuilderFactory() instanceof DocumentBuilderFactory);
		assertTrue(factorConverter.getTransformerFactory() instanceof TransformerFactory);
	}

	@Test
	public void testXmlConverterFactoryHasIncreasedSecurity() throws Exception {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		DocumentBuilderFactory documentBuilderFactory = factorConverter.getDocumentBuilderFactory();
		assertTrue(documentBuilderFactory
				.getFeature("http://apache.org/xml/features/disallow-doctype-decl"));
		assertFalse(documentBuilderFactory
				.getFeature("http://xml.org/sax/features/external-general-entities"));
		assertFalse(documentBuilderFactory
				.getFeature("http://xml.org/sax/features/external-parameter-entities"));
		assertFalse(documentBuilderFactory
				.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"));
		assertFalse(documentBuilderFactory.isExpandEntityReferences());
	}

	@Test
	public void testXmlConverterFactoryHasDefaultsThatPreventSsrfAttacks() throws Exception {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		DocumentBuilderFactory documentBuilderFactory = factorConverter.getDocumentBuilderFactory();
		assertFalse(documentBuilderFactory.isXIncludeAware());
		assertFalse(documentBuilderFactory.isNamespaceAware());
	}

	@Test
	public void testXmlDocumentBuilderFactoryHasIncreasedSecurity() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		DocumentBuilderFactory documentBuilderFactory = factorConverter.getDocumentBuilderFactory();

		TransformerFactory transformerFactory = factorConverter.getTransformerFactory();
		boolean feature = transformerFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
		assertEquals(feature, true);
	}

	// README : https://portswigger.net/web-security/xxe
	@Test(expectedExceptions = XmlConverterException.class)
	public void testMaliciousXmlExploitingXxeToRetrieveFiles() {
		XmlToDataElement xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!DOCTYPE doc[\n"
				+ " <!ENTITY pwd SYSTEM \"file:///etc/passwd23424\">\n" + "]>"
				+ "<person><firstname>&pwd;</firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class)
	public void testMaliciousXmlExploitingXInclude() {
		XmlToDataElement xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person xmlns:xi=\"http://www.w3.org/2001/XInclude\"><firstname><xi:include parse=\"text\" href=\"file:///etc/passwd\"/></firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class)
	public void testMaliciousXmlExploitingXxeToPerformSsrfAttacks() {
		XmlToDataElement xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE test [ <!ENTITY xxe SYSTEM \"https://somewhere/whatever/\"> ]>"
				+ "<person><firstname>&xxe;</firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	private XmlToDataElement createXmlToDataElementToTestSecurity() {
		DataGroupFactorySpy dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		DataAtomicFactorySpy dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);

		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		DocumentBuilderFactory documentBuilderFactory = factorConverter.getDocumentBuilderFactory();

		XmlToDataElement xmlToDataElement = new XmlToDataElement(documentBuilderFactory);
		return xmlToDataElement;
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to set security features for XmlConverterFactory")
	public void testExceptionWhenSetttingWrongSecurityFeature() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		xmlConverterFactory.throwExceptionForTest();
		xmlConverterFactory.factorConverter();
	}

}
