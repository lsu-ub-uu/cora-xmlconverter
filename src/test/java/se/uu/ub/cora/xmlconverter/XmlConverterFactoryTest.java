/*
 * Copyright 2019, 2022 Uppsala University Library
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterException;
import se.uu.ub.cora.converter.ConverterFactory;
import se.uu.ub.cora.converter.ConverterInitializationException;
import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.converter.StringToExternallyConvertibleConverter;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.xmlconverter.converter.ExternallyConvertibleToXml;
import se.uu.ub.cora.xmlconverter.converter.XmlToExternallyConvertible;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class XmlConverterFactoryTest {

	@Test
	public void testXmlConverterFactoryImplementsConverterFactory() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();

		assertTrue(xmlConverterFactory instanceof ConverterFactory);
	}

	@Test
	public void testFactorDataElementConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();

		ExternallyConvertibleToStringConverter factoredConverter = xmlConverterFactory
				.factorExternallyConvertableToStringConverter();

		assertTrue(factoredConverter instanceof ExternallyConvertibleToStringConverter);
	}

	@Test
	public void testFactorStringConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();

		StringToExternallyConvertibleConverter factoredConverter = xmlConverterFactory
				.factorStringToExternallyConvertableConverter();

		assertTrue(factoredConverter instanceof StringToExternallyConvertibleConverter);
	}

	@Test
	public void testFactoryName() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		assertEquals(xmlConverterFactory.getName(), "xml");
	}

	@Test
	public void testXmlConverterFactorySendsCorrectFactoriesToDataElementConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();

		ExternallyConvertibleToXml factorConverter = (ExternallyConvertibleToXml) xmlConverterFactory
				.factorExternallyConvertableToStringConverter();

		assertTrue(factorConverter
				.getDocumentBuilderFactoryOnlyForTest() instanceof DocumentBuilderFactory);
		assertTrue(
				factorConverter.getTransformerFactoryOnlyForTest() instanceof TransformerFactory);
	}

	@Test
	public void testXmlConverterFactorySendsCorrectFactoriesToStringConverter() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();

		XmlToExternallyConvertible factorConverter = (XmlToExternallyConvertible) xmlConverterFactory
				.factorStringToExternallyConvertableConverter();

		assertTrue(factorConverter
				.getDocumentBuilderFactoryOnlyForTest() instanceof DocumentBuilderFactory);
	}

	@Test
	public void testXmlDataElementConverterFactoryHasIncreasedSecurity() throws Exception {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		ExternallyConvertibleToXml factorConverter = (ExternallyConvertibleToXml) xmlConverterFactory
				.factorExternallyConvertableToStringConverter();

		DocumentBuilderFactory documentBuilderFactory = factorConverter
				.getDocumentBuilderFactoryOnlyForTest();

		assertCorrectSecurityInDocumentBuilder(documentBuilderFactory);
		assertDocumentBuilderDefaultsPrevenstSsrfAttacks(documentBuilderFactory);
	}

	private void assertCorrectSecurityInDocumentBuilder(
			DocumentBuilderFactory documentBuilderFactory) throws ParserConfigurationException {
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

	private void assertDocumentBuilderDefaultsPrevenstSsrfAttacks(
			DocumentBuilderFactory documentBuilderFactory) {
		assertFalse(documentBuilderFactory.isXIncludeAware());
		assertFalse(documentBuilderFactory.isNamespaceAware());
	}

	@Test
	public void testXmlStringConverterFactoryHasIncreasedSecurity() throws Exception {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlToExternallyConvertible factorConverter = (XmlToExternallyConvertible) xmlConverterFactory
				.factorStringToExternallyConvertableConverter();

		DocumentBuilderFactory documentBuilderFactory = factorConverter
				.getDocumentBuilderFactoryOnlyForTest();

		assertCorrectSecurityInDocumentBuilder(documentBuilderFactory);
		assertDocumentBuilderDefaultsPrevenstSsrfAttacks(documentBuilderFactory);
	}

	@Test
	public void testXmlDataElementTransformerFactoryHasIncreasedSecurity() {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		ExternallyConvertibleToXml factorConverter = (ExternallyConvertibleToXml) xmlConverterFactory
				.factorExternallyConvertableToStringConverter();

		TransformerFactory transformerFactory = factorConverter.getTransformerFactoryOnlyForTest();
		boolean feature = transformerFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
		assertEquals(feature, true);
	}

	// README : https://portswigger.net/web-security/xxe
	@Test(expectedExceptions = ConverterException.class)
	public void testMaliciousXmlExploitingXxeToRetrieveFiles() {
		XmlToExternallyConvertible xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<!DOCTYPE doc[\n"
				+ " <!ENTITY pwd SYSTEM \"file:///etc/passwd23424\">\n" + "]>"
				+ "<person><firstname>&pwd;</firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void testMaliciousXmlExploitingXInclude() {
		XmlToExternallyConvertible xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person xmlns:xi=\"http://www.w3.org/2001/XInclude\"><firstname><xi:include parse=\"text\" href=\"file:///etc/passwd\"/></firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void testMaliciousXmlExploitingXxeToPerformSsrfAttacks() {
		XmlToExternallyConvertible xmlToDataElement = createXmlToDataElementToTestSecurity();

		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<!DOCTYPE test [ <!ENTITY xxe SYSTEM \"https://somewhere/whatever/\"> ]>"
				+ "<person><firstname>&xxe;</firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	private XmlToExternallyConvertible createXmlToDataElementToTestSecurity() {
		DataGroupFactorySpy dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		DataAtomicFactorySpy dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);

		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		return (XmlToExternallyConvertible) xmlConverterFactory
				.factorStringToExternallyConvertableConverter();
	}

	@Test(expectedExceptions = ConverterInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to set security features for TransformerFactory")
	public void testExceptionWhenSettingWrongSecurityFeatureDataElement() {
		XmlConverterFactoryThrowsExceptionExtendedForTest xmlConverterFactory = new XmlConverterFactoryThrowsExceptionExtendedForTest();
		xmlConverterFactory.throwExceptionInTransformerFactory = true;
		xmlConverterFactory.factorExternallyConvertableToStringConverter();
	}

	@Test(expectedExceptions = ConverterInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to set security features for DocumentBuilderFactory")
	public void testExceptionWhenSettingWrongSecurityFeatureDataElement2() {
		XmlConverterFactoryThrowsExceptionExtendedForTest xmlConverterFactory = new XmlConverterFactoryThrowsExceptionExtendedForTest();
		xmlConverterFactory.throwExceptionInDocumentBuilder = true;
		xmlConverterFactory.factorExternallyConvertableToStringConverter();
	}

	@Test(expectedExceptions = ConverterInitializationException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to set security features for DocumentBuilderFactory")
	public void testExceptionWhenSettingWrongSecurityFeatureString() {
		XmlConverterFactoryThrowsExceptionExtendedForTest xmlConverterFactory = new XmlConverterFactoryThrowsExceptionExtendedForTest();
		xmlConverterFactory.throwExceptionInDocumentBuilder = true;
		xmlConverterFactory.factorStringToExternallyConvertableConverter();
	}

	class XmlConverterFactoryThrowsExceptionExtendedForTest extends XmlConverterFactory {
		boolean throwExceptionInDocumentBuilder = false;
		boolean throwExceptionInTransformerFactory = false;

		@Override
		DocumentBuilderFactory getNewDocumentBuilder() {
			DocumentBuilderFactorySpy documentBuilderFactorySpy = new DocumentBuilderFactorySpy();
			documentBuilderFactorySpy.throwRuntimeException = throwExceptionInDocumentBuilder;
			return documentBuilderFactorySpy;
		}

		@Override
		TransformerFactory getNewTransformerFactory() throws TransformerFactoryConfigurationError {
			TransformerFactorySpy transformerFactorySpy = new TransformerFactorySpy();
			transformerFactorySpy.throwRuntimeException = throwExceptionInTransformerFactory;
			return transformerFactorySpy;
		}
	}

}
