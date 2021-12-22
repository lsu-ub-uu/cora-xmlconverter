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
import se.uu.ub.cora.data.DataResourceLink;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DataRecordSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class ExternallyConvertibleToXmlTest {

	private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
		String expectedXml = XML_DECLARATION + "<person><firstname>Kalle</firstname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertToOneAtomicChildWithRunicCharacters() {
		String expectedXml = XML_DECLARATION + "<person><firstname>ᚠᚢᚦᚮᚱᚴ</firstname></person>";

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
		String expectedXml = XML_DECLARATION
				+ "<person><firstname>Kalle</firstname><lastname>Anka</lastname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		DataAtomic lastName = new DataAtomicSpy("lastname", "Anka");
		person.addChild(lastName);

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);

	}

	@Test
	public void testConvertOneChildGroupWithOneAtomicChild() {
		String expectedXml = XML_DECLARATION
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
		String expectedXml = XML_DECLARATION
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
		String expectedXml = XML_DECLARATION
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
		String expectedXml = XML_DECLARATION
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
		String expectedXml = XML_DECLARATION
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
	public void testConvertLink_noReadAction() {
		DataGroup person = new DataGroupSpy("person");
		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");
		person.addChild(linkSpy);

		String xml = extConvToXml.convertWithLinks(person, SOME_BASE_URL);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<person>";
		expectedXml += "<someLinkNameInData repeatId=\"someRepeatId\" someAttributeId=\"someAttributeValue\">";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</person>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertLink_readAction() {
		DataGroup person = new DataGroupSpy("person");
		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.addAction(Action.READ);
		person.addChild(linkSpy);

		String xml = extConvToXml.convertWithLinks(person, SOME_BASE_URL);

		String expectedXml = XML_DECLARATION;
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
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertResourceLink_noReadAction() {
		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroup person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataResourceLink linkSpy = new DataResourceLinkSpy("master", "someStreamId", "someFileName",
				"18580", "application/octet-stream");
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");
		person.addChild(linkSpy);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<master repeatId=\"someRepeatId\" someAttributeId=\"someAttributeValue\">";
		expectedXml += "<streamId>someStreamId</streamId>";
		expectedXml += "<fileName>someFileName</fileName>";
		expectedXml += "<fileSize>18580</fileSize>";
		expectedXml += "<mimeType>application/octet-stream</mimeType>";
		expectedXml += "</master>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "</record>";

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertResourceLink_readActionNoLinksRequested() {
		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroup person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataResourceLink linkSpy = new DataResourceLinkSpy("master", "someStreamId", "someFileName",
				"18580", "application/octet-stream");
		person.addChild(linkSpy);
		linkSpy.addAction(Action.READ);
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");

		String xml = extConvToXml.convert(dataRecord);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<master repeatId=\"someRepeatId\" someAttributeId=\"someAttributeValue\">";
		expectedXml += "<streamId>someStreamId</streamId>";
		expectedXml += "<fileName>someFileName</fileName>";
		expectedXml += "<fileSize>18580</fileSize>";
		expectedXml += "<mimeType>application/octet-stream</mimeType>";
		expectedXml += "</master>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "</record>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertResourceLink_readAction() {
		DataRecordSpy dataRecord = new DataRecordSpy();
		DataGroup person = new DataGroupSpy("person");
		dataRecord.setDataGroup(person);

		DataResourceLink linkSpy = new DataResourceLinkSpy("master", "someStreamId", "someFileName",
				"18580", "application/octet-stream");
		linkSpy.addAction(Action.READ);
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");
		person.addChild(linkSpy);

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<person>";
		expectedXml += "<master repeatId=\"someRepeatId\" someAttributeId=\"someAttributeValue\">";
		expectedXml += "<streamId>someStreamId</streamId>";
		expectedXml += "<fileName>someFileName</fileName>";
		expectedXml += "<fileSize>18580</fileSize>";
		expectedXml += "<mimeType>application/octet-stream</mimeType>";

		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		// TODO: where do we get type and id from (master is nameInData)?
		/**
		 * Do we need two different interfaces ExternallyConvertible --> for going out through the
		 * API. Known imp. DataList, DataRecord.<br>
		 * convertWithLinks(ExternallyConvertible)
		 * <p>
		 * and InternallyConvertible for going to external resources such as databases? Known imp.
		 * DataGroup<br>
		 * convert(InternallyConvertible)
		 */
		expectedXml += "<url>https://some.domain.now/rest/record/"
				+ dataRecord.MCR.getReturnValue("getType", 0) + "/"
				+ dataRecord.MCR.getReturnValue("getId", 0) + "/master</url>";
		expectedXml += "<accept>application/octet-stream</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";

		expectedXml += "</master>";
		expectedXml += "</person>";
		expectedXml += "</data>";
		expectedXml += "</record>";
		assertEquals(xml, expectedXml);
	}

	// TODO:
	/**
	 * test for list of records with resourceLink, uses recordType and id from current record<br>
	 * test for one dataGroup convertWithLinks should throw exception (as we can not calculate
	 * resource links)<br>
	 * test for list of dataGroups convertWithLinks should throw exception
	 */

	// "actionLinks": {
	// "read": {
	// "requestMethod": "GET",
	// "rel": "read",
	// "url":
	// "https://cora.epc.ub.uu.se/systemone/rest/record/image/image:12081016459542285/master",
	// "accept": "application/octet-stream"
	// }
	// },
	// "name": "master"

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
		String expectedXml = XML_DECLARATION;
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
	public void testToJsonWithLinks_noPermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(List.of(), List.of());

		String xml = extConvToXml.convertWithLinks(dataRecord, SOME_BASE_URL);

		String expectedPermissionsXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}

	@Test
	public void testToJsonWithLinks_ListOfReadPermissions() {
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
	public void testToJsonWithLinks_ListOfWritePermissions() {
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
	public void testToJsonWithLinks_ListOfReadAndWritePermissions() {
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

	@Test
	public void testToJsonWithoutLinks_ListOfReadAndWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convert(dataRecord);

		String expectedPermissionsXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(expectedPermissionsXml, xml);
	}

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

	@Test
	public void testToJsonWithoutLinks_ListOfRecordsNoRecord() throws Exception {
		DataListSpy dataList = new DataListSpy();

		String xml = extConvToXml.convert(dataList);

		String expectedListXml = XML_DECLARATION;
		expectedListXml += "<dataList>";
		expectedListXml += "<fromNo>";
		expectedListXml += dataList.MCR.getReturnValue("getFromNo", 0);
		expectedListXml += "</fromNo>";
		expectedListXml += "<toNo>";
		expectedListXml += dataList.MCR.getReturnValue("getToNo", 0);
		expectedListXml += "</toNo>";
		expectedListXml += "<totalNo>";
		expectedListXml += dataList.MCR.getReturnValue("getTotalNumberOfTypeInStorage", 0);
		expectedListXml += "</totalNo>";
		expectedListXml += "<containDataOfType>";
		expectedListXml += dataList.MCR.getReturnValue("getContainDataOfType", 0);
		expectedListXml += "</containDataOfType>";
		expectedListXml += "<data/>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);

	}

	@Test
	public void testToJsonWithoutLinks_ListOfRecordsTwoRecords() throws Exception {
		DataListSpy dataList = new DataListSpy();
		DataRecordSpy dataRecord1 = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));
		dataList.addData(dataRecord1);
		DataRecordSpy dataRecord2 = createRecordWithLinkAddRecordActions(Action.VALIDATE);
		dataList.addData(dataRecord2);

		String xml = extConvToXml.convert(dataList);

		String expectedListXml = XML_DECLARATION;
		expectedListXml += "<dataList>";
		expectedListXml += "<fromNo>";
		expectedListXml += dataList.MCR.getReturnValue("getFromNo", 0);
		expectedListXml += "</fromNo>";
		expectedListXml += "<toNo>";
		expectedListXml += dataList.MCR.getReturnValue("getToNo", 0);
		expectedListXml += "</toNo>";
		expectedListXml += "<totalNo>";
		expectedListXml += dataList.MCR.getReturnValue("getTotalNumberOfTypeInStorage", 0);
		expectedListXml += "</totalNo>";
		expectedListXml += "<containDataOfType>";
		expectedListXml += dataList.MCR.getReturnValue("getContainDataOfType", 0);
		expectedListXml += "</containDataOfType>";
		expectedListXml += "<data>";
		expectedListXml += removeXmlDeclaration(extConvToXml.convert(dataRecord1));
		expectedListXml += removeXmlDeclaration(extConvToXml.convert(dataRecord2));
		expectedListXml += "</data>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);
	}

	@Test
	public void testToJsonWithLinks_ListOfRecordsTwoRecords() throws Exception {
		DataListSpy dataList = new DataListSpy();
		DataRecordSpy dataRecord1 = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));
		dataList.addData(dataRecord1);
		DataRecordSpy dataRecord2 = createRecordWithLinkAddRecordActions(Action.VALIDATE);
		dataList.addData(dataRecord2);

		String xml = extConvToXml.convertWithLinks(dataList, SOME_BASE_URL);

		String expectedListXml = XML_DECLARATION;
		expectedListXml += "<dataList>";
		expectedListXml += "<fromNo>";
		expectedListXml += dataList.MCR.getReturnValue("getFromNo", 0);
		expectedListXml += "</fromNo>";
		expectedListXml += "<toNo>";
		expectedListXml += dataList.MCR.getReturnValue("getToNo", 0);
		expectedListXml += "</toNo>";
		expectedListXml += "<totalNo>";
		expectedListXml += dataList.MCR.getReturnValue("getTotalNumberOfTypeInStorage", 0);
		expectedListXml += "</totalNo>";
		expectedListXml += "<containDataOfType>";
		expectedListXml += dataList.MCR.getReturnValue("getContainDataOfType", 0);
		expectedListXml += "</containDataOfType>";
		expectedListXml += "<data>";
		expectedListXml += removeXmlDeclaration(
				extConvToXml.convertWithLinks(dataRecord1, SOME_BASE_URL));
		expectedListXml += removeXmlDeclaration(
				extConvToXml.convertWithLinks(dataRecord2, SOME_BASE_URL));
		expectedListXml += "</data>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);
	}
	// {
	// "dataList": {
	// "fromNo": "1",
	// "data": [ ],
	// "totalNo": "43524",
	// "containDataOfType": "mix",
	// "toNo": "100"
	// }
	// }

	private String removeXmlDeclaration(String xml) {
		return xml.substring(XML_DECLARATION.length());
	}

}
