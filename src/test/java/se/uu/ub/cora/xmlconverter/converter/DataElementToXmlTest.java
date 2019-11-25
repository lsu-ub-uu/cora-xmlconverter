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
import javax.xml.transform.TransformerException;

import org.testng.annotations.Test;

import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;

public class DataElementToXmlTest {

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testParseExceptionOnCreateDocument() {
		DocumentBuilderFactorySpy documentBuildeFactorySpy = new DocumentBuilderFactorySpy();
		documentBuildeFactorySpy.throwParserError = true;
		DataElementToXml toXmlConverter = new DataElementToXml(documentBuildeFactorySpy, null);
		toXmlConverter.convert(new DataGroupSpy("someNameInData"));
	}

	@Test
	public void testParseExceptionOriginalExceptionIsSentAlong() {
		DocumentBuilderFactorySpy documentBuildeFactorySpy = new DocumentBuilderFactorySpy();
		documentBuildeFactorySpy.throwParserError = true;
		DataElementToXml toXmlConverter = new DataElementToXml(documentBuildeFactorySpy, null);
		try {
			toXmlConverter.convert(new DataGroupSpy("someNameInData"));

		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testTransformerExceptionOnTransformDomDocumentToXml() {
		DocumentBuilderFactory documentBuildeFactory = DocumentBuilderFactory.newInstance();

		TransformerFactorySpy transformerFactorySpy = new TransformerFactorySpy();
		transformerFactorySpy.throwTransformError = true;

		DataElementToXml toXmlConverter = new DataElementToXml(documentBuildeFactory,
				transformerFactorySpy);
		toXmlConverter.convert(new DataGroupSpy("someNameInData"));
	}

	@Test
	public void testTransformerExceptionOnTransformDomDocumentToXmlOriginalExceptionIsSentAlong() {
		DocumentBuilderFactory documentBuildeFactory = DocumentBuilderFactory.newInstance();

		TransformerFactorySpy transformerFactorySpy = new TransformerFactorySpy();
		transformerFactorySpy.throwTransformError = true;

		DataElementToXml toXmlConverter = new DataElementToXml(documentBuildeFactory,
				transformerFactorySpy);
		try {
			toXmlConverter.convert(new DataGroupSpy("someNameInData"));
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof TransformerException);
		}
	}
}
