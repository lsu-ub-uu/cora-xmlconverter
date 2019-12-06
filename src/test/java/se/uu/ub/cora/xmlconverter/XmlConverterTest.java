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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class XmlConverterTest {

	DataGroupFactorySpy dataGroupFactorySpy = null;
	DataAtomicFactorySpy dataAtomicFactorySpy = null;
	private DocumentBuilderFactorySpy documentBuilderFactory;
	private TransformerFactorySpy tranformerFactory;
	private XmlConverter xmlConverter;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);

		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		documentBuilderFactory = new DocumentBuilderFactorySpy();
		tranformerFactory = new TransformerFactorySpy();
		xmlConverter = new XmlConverter(documentBuilderFactory, tranformerFactory);
	}

	@Test
	public void testXmlConverterFactoryImplementsConverterFactory() {
		assertTrue(xmlConverter instanceof Converter);
	}

	@Test
	public void testXmlToDataElementConverterUsesFactory() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";
		xmlConverter.convert(xmlToConvert);
		assertTrue(documentBuilderFactory.newDocumentBuilderWasCalled);
	}

	@Test
	public void testDataToElementConverterUserFactories() {
		DataGroup dataGroup = new DataGroupSpy("someNameInData");
		xmlConverter.convert(dataGroup);
		assertTrue(documentBuilderFactory.newDocumentBuilderWasCalled);
		assertTrue(tranformerFactory.newTransformerWasCalled);

	}
}
