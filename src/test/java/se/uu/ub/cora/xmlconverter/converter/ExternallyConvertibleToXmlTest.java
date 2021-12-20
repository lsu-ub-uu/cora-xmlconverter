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

import java.util.LinkedHashSet;
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
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DataRecordSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class ExternallyConvertibleToXmlTest {

	private static final String SOME_BASE_URL = "https://some.domain.now/rest/record/";
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private ExternallyConvertibleToXml extConvToXml;

	@BeforeMethod
	public void setUp() {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		extConvToXml = new ExternallyConvertibleToXml(documentBuilderFactory, transformerFactory);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testParseExceptionOnCreateDocument() {
		setUpDataElementToXmlWithDocumentBuilderFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		extConvToXml.convert(new DataGroupSpy("someNameInData"));
	}

	private void setUpDataElementToXmlWithDocumentBuilderFactorySpy() {
		documentBuilderFactory = new DocumentBuilderFactorySpy();
		extConvToXml = new ExternallyConvertibleToXml(documentBuilderFactory, null);
	}

	@Test
	public void testParseExceptionOriginalExceptionIsSentAlong() {
		setUpDataElementToXmlWithDocumentBuilderFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;
		try {
			extConvToXml.convert(new DataGroupSpy("someNameInData"));

		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testTransformerExceptionOnTransformDomDocumentToXml() {
		extConvToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		extConvToXml.convert(new DataGroupSpy("someNameInData"));
	}

	@Test
	public void testTransformerExceptionOnTransformDomDocumentToXmlOriginalExceptionIsSentAlong() {
		extConvToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		try {
			extConvToXml.convert(new DataGroupSpy("someNameInData"));
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
		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertToOneAtomicChildWithRunicCharacters() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>ᚠᚢᚦᚮᚱᚴ</firstname></person>";

		DataGroup person = createPersonWithFirstname("ᚠᚢᚦᚮᚱᚴ");
		String xml = extConvToXml.convert(person);
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

		String xml = extConvToXml.convert(person);
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

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertOneChildGroupWithAttribute() {
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authorized\"><firstname>Kalle</firstname></name></person>";

		DataGroup person = createPersonWithFirstnameInNameGroupWithAttribute("Kalle", "authorized");

		String xml = extConvToXml.convert(person);
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

		String xml = extConvToXml.convert(person);
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

		String xml = extConvToXml.convert(person);
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

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testContainsCorrectEncodingUTF8AndVersion1() throws Exception {
		String expectedEncoding = "encoding=\"UTF-8\"";
		String expectedVersion = "version=\"1.0\"";

		DataGroup person = new DataGroupSpy("person");
		String xml = extConvToXml.convert(person);

		assertTrue(xml.contains(expectedEncoding));
		assertTrue(xml.contains(expectedVersion));
	}

	@Test
	public void testConvertWithLink_noReadAction() {
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

		String xml = extConvToXml.convertWithLinks(person, SOME_BASE_URL);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertWithLink_readAction() {
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

		String xml = extConvToXml.convertWithLinks(person, SOME_BASE_URL);

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertRecord_forAllActions_hasNoActionLinksInResult() throws Exception {
		for (Action action : Action.values()) {
			DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(action);

			String xml = extConvToXml.convert(dataRecord);

			String expectedActionLinksXml = "";
			assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
		}
	}

	@Test
	public void testConvertRecordWithLinks_noAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions();

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_readAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.READ);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<read>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>read</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "</read>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_deleteAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.DELETE);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<delete>";
		expectedActionLinksXml += "<requestMethod>DELETE</requestMethod>";
		expectedActionLinksXml += "<rel>delete</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "</delete>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_updateAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.UPDATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<update>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>update</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "<contentType>application/vnd.uub.record+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "</update>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_incommingLinksAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.READ_INCOMING_LINKS);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<read_incoming_links>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>read_incoming_links</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId/incomingLinks</url>";
		expectedActionLinksXml += "<accept>application/vnd.uub.recordList+xml</accept>";
		expectedActionLinksXml += "</read_incoming_links>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_indexAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.INDEX);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<index>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>index</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/workOrder</url>";
		expectedActionLinksXml += "<contentType>application/vnd.uub.record+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "<body>";
		expectedActionLinksXml += "<workOrder>";
		expectedActionLinksXml += "<recordType>";
		expectedActionLinksXml += "<linkedRecordType>recordType</linkedRecordType>";
		expectedActionLinksXml += "<linkedRecordId>fakeType</linkedRecordId>";
		expectedActionLinksXml += "<recordId>fakeId</recordId>";
		expectedActionLinksXml += "<type>index</type>";
		expectedActionLinksXml += "</recordType>";
		expectedActionLinksXml += "</workOrder>";
		expectedActionLinksXml += "</body>";
		expectedActionLinksXml += "</index>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_uploadAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.UPLOAD);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<upload>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>upload</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId/master</url>";
		expectedActionLinksXml += "<contentType>multipart/form-data</contentType>";
		expectedActionLinksXml += "</upload>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_searchAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.SEARCH);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String searchId = (String) dataRecord.MCR.getReturnValue("getSearchId", 0);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<search>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>search</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/searchResult/"
				+ searchId + "</url>";
		expectedActionLinksXml += "<accept>application/vnd.uub.recordList+xml</accept>";
		expectedActionLinksXml += "</search>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_createAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.CREATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<create>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>create</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeId</url>";
		expectedActionLinksXml += "<contentType>application/vnd.uub.record+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "</create>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_listAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.LIST);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<list>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>list</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeId</url>";
		expectedActionLinksXml += "<accept>application/vnd.uub.recordList+xml</accept>";
		expectedActionLinksXml += "</list>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_batchIndexAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.BATCH_INDEX);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<batch_index>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>batch_index</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/index/fakeId</url>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "</batch_index>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	@Test
	public void testConvertRecordWithLinks_validateAction() throws Exception {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.VALIDATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<validate>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>validate</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/workOrder</url>";
		expectedActionLinksXml += "<contentType>application/vnd.uub.workorder+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.uub.record+xml</accept>";
		expectedActionLinksXml += "</validate>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedActionLinksXml, xml);
	}

	private DataRecordSpy createRecordWithLinkAddRecordActions(Action... actions) {
		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		person.addChild(linkSpy);

		dataRecord.actions = List.of(actions);
		return dataRecord;
	}

	private void assertRecordCorrectWithSuppliedExpectedPart(String expectedActionLinksXml,
			String xml) {
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
		expectedXml += expectedActionLinksXml;
		expectedXml += "</record>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testToJsonWith_noPermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(List.of(), List.of());

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedPermissionsXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}

	@Test
	public void testToJsonWith_ListOfReadPermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"), List.of());

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedPermissionsXml = "<permissions>";
		expectedPermissionsXml += "<read>";
		expectedPermissionsXml += "<permission>readPermissionOne</permission>";
		expectedPermissionsXml += "<permission>readPermissionTwo</permission>";
		expectedPermissionsXml += "</read>";
		expectedPermissionsXml += "</permissions>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}

	@Test
	public void testToJsonWith_ListOfWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(List.of(),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedPermissionsXml = "<permissions>";
		expectedPermissionsXml += "<write>";
		expectedPermissionsXml += "<permission>writePermissionOne</permission>";
		expectedPermissionsXml += "<permission>writePermissionTwo</permission>";
		expectedPermissionsXml += "</write>";
		expectedPermissionsXml += "</permissions>";
		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}

	@Test
	public void testToJsonWith_ListOfReadAndWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedPermissionsXml = "<permissions>";
		expectedPermissionsXml += "<read>";
		expectedPermissionsXml += "<permission>readPermissionOne</permission>";
		expectedPermissionsXml += "<permission>readPermissionTwo</permission>";
		expectedPermissionsXml += "</read>";
		expectedPermissionsXml += "<write>";
		expectedPermissionsXml += "<permission>writePermissionOne</permission>";
		expectedPermissionsXml += "<permission>writePermissionTwo</permission>";
		expectedPermissionsXml += "</write>";
		expectedPermissionsXml += "</permissions>";

		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}
	// "permissions": {
	// "read": [
	// "externalURL",
	// "otherAffiliation",
	// "biographySwedish",
	// "personDomainPart",
	// "public",
	// "biographyEnglish",
	// "Libris_ID",
	// "domain",
	// "ORCID_ID",
	// "alternativeName",
	// "VIAF_ID",
	// "academicTitle"
	// ]
	// },

	private DataRecordSpy createRecordWithReadAndWritePermissions(List<String> readPermissions,
			List<String> writePermissions) {
		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroupSpy person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		person.addChild(linkSpy);
		LinkedHashSet<String> readSet = new LinkedHashSet<>();
		readSet.addAll(readPermissions);
		dataRecord.readPermissions = readSet;
		LinkedHashSet<String> writeSet = new LinkedHashSet<>();
		writeSet.addAll(writePermissions);
		dataRecord.writePermissions = writeSet;
		return dataRecord;
	}

	// private CoraDataList createDataList() {
	// CoraDataList dataList = CoraDataList.withContainDataOfType("place");
	// DataRecord dataRecord = new DataRecordSpy();
	// dataList.addData(dataRecord);
	// DataRecord dataRecord2 = new DataRecordSpy();
	// dataList.addData(dataRecord2);
	// dataList.setTotalNo("111");
	// dataList.setFromNo("1");
	// dataList.setToNo("100");
	// return dataList;
	// }

}
