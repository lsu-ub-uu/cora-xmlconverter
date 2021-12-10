/*
 * Copyright 2019, 2021 Uppsala University Library
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

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DataRecordSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class ExternallyConvertibleToXmlTest {

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private ExternallyConvertibleToXml dataElementToXml;

	@BeforeMethod
	public void setUp() {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		dataElementToXml = new ExternallyConvertibleToXml(documentBuilderFactory,
				transformerFactory);
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
		dataElementToXml = new ExternallyConvertibleToXml(documentBuilderFactory, null);
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

	private ExternallyConvertibleToXml setUpDataElementToXmlWithTransformerSpy() {
		transformerFactory = new TransformerFactorySpy();
		ExternallyConvertibleToXml dataElementToXml = new ExternallyConvertibleToXml(
				documentBuilderFactory, transformerFactory);
		return dataElementToXml;
	}

	@Test
	public void testConvertToOneAtomicChild() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>Kalle</firstname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		String xml = dataElementToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertToOneAtomicChildWithRunicCharacters() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>ᚠᚢᚦᚮᚱᚴ</firstname></person>";

		DataGroup person = createPersonWithFirstname("ᚠᚢᚦᚮᚱᚴ");
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

	@Test
	public void testConvertWithLink_noReadAction() {
		String baseUrl = "https://some.domain.now/rest/record/";
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData repeatId=\"someRepeatId\" someAttributeId=\"someAttributeValue\">";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		DataGroup person = new DataGroupSpy("person");
		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");
		person.addChild(linkSpy);

		String xml = dataElementToXml.convertWithLinks(person, baseUrl);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertWithLink_readAction() {
		String baseUrl = "https://some.domain.now/rest/record/";
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		DataGroup person = new DataGroupSpy("person");
		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.addAction(Action.READ);
		person.addChild(linkSpy);

		String xml = dataElementToXml.convertWithLinks(person, baseUrl);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertRecord() throws Exception {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "</record>";

		DataRecord dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.addAction(Action.READ);
		person.addChild(linkSpy);

		String xml = dataElementToXml.convert(dataRecord);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertRecordWithLink_readAction() throws Exception {
		String baseUrl = "https://some.domain.now/rest/record/";

		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</record>";

		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.addAction(Action.READ);
		person.addChild(linkSpy);

		dataRecord.actions = List.of(Action.READ);

		String xml = dataElementToXml.convertWithLinks(dataRecord, baseUrl);

		assertEquals(xml, expectedXml);

	}

	@Test
	public void testConvertRecordWithLink_deleteAction() throws Exception {
		String baseUrl = "https://some.domain.now/rest/record/";

		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "<actionLinks>";
		expectedXml += "<delete>";
		expectedXml += "<requestMethod>DELETE</requestMethod>";
		expectedXml += "<rel>delete</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedXml += "</delete>";
		expectedXml += "</actionLinks>";
		expectedXml += "</record>";

		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		// linkSpy.addAction(Action.DELETE);
		person.addChild(linkSpy);

		dataRecord.actions = List.of(Action.DELETE);

		String xml = dataElementToXml.convertWithLinks(dataRecord, baseUrl);

		assertEquals(xml, expectedXml);

	}

	@Test
	public void testConvertRecordWithLink_updateAction() throws Exception {
		String baseUrl = "https://some.domain.now/rest/record/";

		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "<actionLinks>";
		expectedXml += "<update>";
		expectedXml += "<requestMethod>POST</requestMethod>";
		expectedXml += "<rel>update</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedXml += "<contentType>application/vnd.uub.record+xml</contentType>";
		expectedXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedXml += "</update>";
		expectedXml += "</actionLinks>";
		expectedXml += "</record>";

		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		person.addChild(linkSpy);

		dataRecord.actions = List.of(Action.UPDATE);

		String xml = dataElementToXml.convertWithLinks(dataRecord, baseUrl);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertRecordWithLink_incommingLinksAction() throws Exception {
		String baseUrl = "https://some.domain.now/rest/record/";

		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read_incoming_links>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read_incoming_links</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId/incomingLinks</url>";
		expectedXml += "<accept>application/vnd.uub.recordList+xml</accept>";
		expectedXml += "</read_incoming_links>";
		expectedXml += "</actionLinks>";
		expectedXml += "</record>";

		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		person.addChild(linkSpy);

		dataRecord.actions = List.of(Action.READ_INCOMING_LINKS);

		String xml = dataElementToXml.convertWithLinks(dataRecord, baseUrl);

		assertEquals(xml, expectedXml);
	}

}
