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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class DataElementToXmlTest {

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private DataElementToXml dataElementToXml;

	@BeforeMethod
	public void setUp() {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		dataElementToXml = new DataElementToXml(documentBuilderFactory, transformerFactory);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testParseExceptionOnCreateDocument() {
		setUpDataElementToXmlWithDocumentBuilderFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		dataElementToXml.convert(new DataGroupSpy("someNameInData"));
	}

	private void setUpDataElementToXmlWithDocumentBuilderFactorySpy() {
		documentBuilderFactory = new DocumentBuilderFactorySpy();
		dataElementToXml = new DataElementToXml(documentBuilderFactory, null);
	}

	@Test
	public void testParseExceptionOriginalExceptionIsSentAlong() {
		setUpDataElementToXmlWithDocumentBuilderFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;
		try {
			dataElementToXml.convert(new DataGroupSpy("someNameInData"));

		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testTransformerExceptionOnTransformDomDocumentToXml() {
		dataElementToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		dataElementToXml.convert(new DataGroupSpy("someNameInData"));
	}

	@Test
	public void testTransformerExceptionOnTransformDomDocumentToXmlOriginalExceptionIsSentAlong() {
		dataElementToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		try {
			dataElementToXml.convert(new DataGroupSpy("someNameInData"));
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof TransformerException);
		}
	}

	private DataElementToXml setUpDataElementToXmlWithTransformerSpy() {
		transformerFactory = new TransformerFactorySpy();
		DataElementToXml dataElementToXml = new DataElementToXml(documentBuilderFactory,
				transformerFactory);
		return dataElementToXml;
	}

	/***************************************************************************************/

	@Test
	public void testConvertToOneAtomicChild() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>Kalle</firstname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	private DataGroup createPersonWithFirstname(String firstNameString) {
		DataGroup person = new DataGroupSpy("person");
		DataAtomic firstName = new DataAtomicSpy("firstname", firstNameString);
		person.addChild(firstName);
		return person;
	}

	@Test
	public void testConvertMultipleDataAtomicChildren() throws Exception {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>Kalle</firstname><lastname>Anka</lastname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		DataAtomic lastName = new DataAtomicSpy("lastname", "Anka");
		person.addChild(lastName);

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);

	}

	@Test
	public void testConvertOneChildGroupWithOneAtomicChild() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name><firstname>Kalle</firstname></name></person>";

		DataGroup person = new DataGroupSpy("person");
		DataGroupSpy name = new DataGroupSpy("name");
		DataAtomic firstName = new DataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);
		person.addChild(name);

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertOneChildGroupWithAttribute() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authorized\"><firstname>Kalle</firstname></name></person>";

		DataGroup person = createPersonWithFirstnameInNameGroupWithAttribute("Kalle", "authorized");

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	private DataGroup createPersonWithFirstnameInNameGroupWithAttribute(String firstnameString,
			String typeAttribute) {
		DataGroup person = new DataGroupSpy("person");
		DataGroupSpy name = new DataGroupSpy("name");

		name.addAttributeByIdWithValue("type", typeAttribute);

		DataAtomic firstName = new DataAtomicSpy("firstname", firstnameString);
		name.addChild(firstName);
		person.addChild(name);
		return person;
	}

	@Test
	public void testConvertMultipleChildrenToDataGroups() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authorized\"><firstname>Kalle</firstname>"
				+ "<lastname>Anka</lastname></name><shoesize>14</shoesize></person>";

		DataGroup person = createPersonWithFirstnameInNameGroupWithAttribute("Kalle", "authorized");
		DataGroup name = person.getFirstGroupWithNameInData("name");
		DataAtomic lastName = new DataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);

		DataAtomic shoeSize = new DataAtomicSpy("shoesize", "14");
		person.addChild(shoeSize);

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertMultipleChildrenToDataGroupsWithRepeatId() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name repeatId=\"2\" type=\"authorized\"><firstname>Kalle</firstname>"
				+ "<lastname>Anka</lastname></name><shoesize repeatId=\"6\">14</shoesize></person>";

		DataGroup person = new DataGroupSpy("person");
		DataGroupSpy name = new DataGroupSpy("name");
		name.setRepeatId("2");

		name.addAttributeByIdWithValue("type", "authorized");

		DataAtomic firstName = new DataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);

		DataAtomic lastName = new DataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);
		person.addChild(name);

		DataAtomic shoeSize = new DataAtomicSpy("shoesize", "14");
		shoeSize.setRepeatId("6");
		person.addChild(shoeSize);

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertMultipleChildrenToDataGroupsWithEmptyRepeatId() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authorized\"><firstname>Kalle</firstname>"
				+ "<lastname>Anka</lastname></name><shoesize repeatId=\"6\">14</shoesize></person>";

		DataGroup person = new DataGroupSpy("person");
		DataGroupSpy name = new DataGroupSpy("name");
		name.setRepeatId("");

		name.addAttributeByIdWithValue("type", "authorized");

		DataAtomic firstName = new DataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);

		DataAtomic lastName = new DataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);
		person.addChild(name);

		DataAtomic shoeSize = new DataAtomicSpy("shoesize", "14");
		shoeSize.setRepeatId("6");
		person.addChild(shoeSize);

		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testContainsCorrectEncodingUTF8AndVersion1() throws Exception {
		String expectedEncoding = "encoding=\"UTF-8\"";
		String expectedVersion = "version=\"1.0\"";

		DataGroup person = new DataGroupSpy("person");
		String xml = dataElementToXml.convert(person);

		assertTrue(xml.contains(expectedEncoding));
		assertTrue(xml.contains(expectedVersion));
	}
}
