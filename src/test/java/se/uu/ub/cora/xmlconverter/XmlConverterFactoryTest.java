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
import static org.testng.Assert.assertTrue;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.testng.annotations.Test;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterFactory;

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
	public void testXmlConverterFactoryHasIncreasedSecurity() throws ParserConfigurationException {
		XmlConverterFactory xmlConverterFactory = new XmlConverterFactory();
		XmlConverter factorConverter = (XmlConverter) xmlConverterFactory.factorConverter();
		DocumentBuilderFactory documentBuilderFactory = factorConverter.getDocumentBuilderFactory();
		boolean feature = documentBuilderFactory
				.getFeature("http://apache.org/xml/features/disallow-doctype-decl");
		assertTrue(feature);

		// TransformerFactory transformerFactory = factorConverter.getTransformerFactory();
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

}
