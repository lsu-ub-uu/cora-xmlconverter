/*
 * Copyright 2019, 2021, 2024, 2025 Uppsala University Library
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.converter.ConverterException;
import se.uu.ub.cora.converter.ExternalUrls;
import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.Data;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataProvider;
import se.uu.ub.cora.data.DataResourceLink;
import se.uu.ub.cora.data.spies.DataAttributeSpy;
import se.uu.ub.cora.data.spies.DataFactorySpy;
import se.uu.ub.cora.data.spies.DataGroupSpy;
import se.uu.ub.cora.data.spies.DataListSpy;
import se.uu.ub.cora.data.spies.DataRecordGroupSpy;
import se.uu.ub.cora.data.spies.DataRecordLinkSpy;
import se.uu.ub.cora.data.spies.DataRecordSpy;
import se.uu.ub.cora.data.spies.DataResourceLinkSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.OldDataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.OldDataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.TransformerFactorySpy;

public class ExternallyConvertibleToXmlTest {

	private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String SOME_BASE_URL = "https://some.domain.now/rest/record/";
	private static final String IIIF_URL = "someIiifFUrl";
	private static final String APPLICATION_VND_CORA_RECORDGROUP_XML = "application/vnd.cora.recordgroup+xml";
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private ExternallyConvertibleToXml extConvToXml;
	private ExternalUrls externalUrls;
	DataFactorySpy dataFactorySpy;

	@BeforeMethod
	public void setUp() {
		dataFactorySpy = new DataFactorySpy();
		DataProvider.onlyForTestSetDataFactory(dataFactorySpy);

		setExternalUrls();

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
		extConvToXml = new ExternallyConvertibleToXml(documentBuilderFactory, transformerFactory);
	}

	private void setExternalUrls() {
		externalUrls = new ExternalUrls();
		externalUrls.setBaseUrl(SOME_BASE_URL);
		externalUrls.setIfffUrl(IIIF_URL);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testParseExceptionOnCreateDocument() {
		setUpDataElementToXmlWithDocumentBuilderFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		extConvToXml.convert(new OldDataGroupSpy("someNameInData"));
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
			extConvToXml.convert(new OldDataGroupSpy("someNameInData"));

		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from dataElement to xml")
	public void testTransformerExceptionOnTransformDomDocumentToXml() {
		extConvToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		extConvToXml.convert(new OldDataGroupSpy("someNameInData"));
	}

	@Test
	public void testTransformerExceptionOnTransformDomDocumentToXmlOriginalExceptionIsSentAlong() {
		extConvToXml = setUpDataElementToXmlWithTransformerSpy();
		((TransformerFactorySpy) transformerFactory).throwTransformError = true;

		try {
			extConvToXml.convert(new OldDataGroupSpy("someNameInData"));
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof TransformerException);
		}
	}

	private ExternallyConvertibleToXml setUpDataElementToXmlWithTransformerSpy() {
		transformerFactory = new TransformerFactorySpy();
		return new ExternallyConvertibleToXml(documentBuilderFactory, transformerFactory);
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
		DataGroup person = new OldDataGroupSpy("person");
		DataAtomic firstName = new OldDataAtomicSpy("firstname", firstNameString);
		person.addChild(firstName);
		return person;
	}

	@Test
	public void testConvertMultipleDataAtomicChildren() {
		String expectedXml = XML_DECLARATION
				+ "<person><firstname>Kalle</firstname><lastname>Anka</lastname></person>";

		DataGroup person = createPersonWithFirstname("Kalle");
		DataAtomic lastName = new OldDataAtomicSpy("lastname", "Anka");
		person.addChild(lastName);

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);

	}

	@Test
	public void testConvertOneChildGroupWithOneAtomicChild() {
		String expectedXml = XML_DECLARATION
				+ "<person><name><firstname>Kalle</firstname></name></person>";

		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");
		DataAtomic firstName = new OldDataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);
		person.addChild(name);

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertOneChildGroupWithOneAtomicChildWithOneAttribute() {
		String expectedXml = XML_DECLARATION
				+ "<person><name><firstname type=\"authorized\">Kalle</firstname></name></person>";

		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");
		person.addChild(name);
		DataAtomic firstName = new OldDataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);
		firstName.addAttributeByIdWithValue("type", "authorized");

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertOneChildGroupWithOneAtomicChildWithTwoAttribute() {
		String expectedXml = XML_DECLARATION
				+ "<person><name><firstname shoeSize=\"43\" type=\"authorized\">Kalle</firstname></name></person>";

		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");
		person.addChild(name);
		DataAtomic firstName = new OldDataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);
		firstName.addAttributeByIdWithValue("type", "authorized");
		firstName.addAttributeByIdWithValue("shoeSize", "43");

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
		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");

		name.addAttributeByIdWithValue("type", typeAttribute);

		DataAtomic firstName = new OldDataAtomicSpy("firstname", firstnameString);
		name.addChild(firstName);
		person.addChild(name);
		return person;
	}

	@Test
	public void testConvertTopGroupWithAttributesOneChildGroupWithAttribute() {
		String expectedXml = XML_DECLARATION + "<person someAttributeName=\"someAttributeValue\""
				+ " someAttributeName2=\"someAttributeValue2\">"
				+ "<name type=\"authorized\" type2=\"authorized2\">"
				+ "<firstname>Kalle</firstname></name></person>";

		DataGroup person = createPersonWithAttributesAndFirstnameInNameGroupWithTwoAttributes(
				"Kalle", "authorized");

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	private DataGroup createPersonWithAttributesAndFirstnameInNameGroupWithTwoAttributes(
			String firstnameString, String typeAttribute) {
		DataGroup person = new OldDataGroupSpy("person");
		person.addAttributeByIdWithValue("someAttributeName", "someAttributeValue");
		person.addAttributeByIdWithValue("someAttributeName2", "someAttributeValue2");
		OldDataGroupSpy name = new OldDataGroupSpy("name");

		name.addAttributeByIdWithValue("type", typeAttribute);
		name.addAttributeByIdWithValue("type2", typeAttribute + "2");

		DataAtomic firstName = new OldDataAtomicSpy("firstname", firstnameString);
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
		DataAtomic lastName = new OldDataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);

		DataAtomic shoeSize = new OldDataAtomicSpy("shoesize", "14");
		person.addChild(shoeSize);

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertMultipleChildrenToDataGroupsWithRepeatId() {
		String expectedXml = XML_DECLARATION
				+ "<person><name repeatId=\"2\" type=\"authorized\"><firstname>Kalle</firstname>"
				+ "<lastname>Anka</lastname></name><shoesize repeatId=\"6\">14</shoesize></person>";

		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");
		name.setRepeatId("2");

		name.addAttributeByIdWithValue("type", "authorized");

		DataAtomic firstName = new OldDataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);

		DataAtomic lastName = new OldDataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);
		person.addChild(name);

		DataAtomic shoeSize = new OldDataAtomicSpy("shoesize", "14");
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

		DataGroup person = new OldDataGroupSpy("person");
		OldDataGroupSpy name = new OldDataGroupSpy("name");
		name.setRepeatId("");

		name.addAttributeByIdWithValue("type", "authorized");

		DataAtomic firstName = new OldDataAtomicSpy("firstname", "Kalle");
		name.addChild(firstName);

		DataAtomic lastName = new OldDataAtomicSpy("lastname", "Anka");
		name.addChild(lastName);
		person.addChild(name);

		DataAtomic shoeSize = new OldDataAtomicSpy("shoesize", "14");
		shoeSize.setRepeatId("6");
		person.addChild(shoeSize);

		String xml = extConvToXml.convert(person);
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testContainsCorrectEncodingUTF8AndVersion1() {
		String expectedEncoding = "encoding=\"UTF-8\"";
		String expectedVersion = "version=\"1.0\"";

		DataGroup person = new OldDataGroupSpy("person");
		String xml = extConvToXml.convert(person);

		assertTrue(xml.contains(expectedEncoding));
		assertTrue(xml.contains(expectedVersion));
	}

	@Test
	public void testConvertLink_noReadAction() {
		DataGroup person = new OldDataGroupSpy("person");
		OldDataRecordLinkSpy linkSpy = new OldDataRecordLinkSpy("someLinkNameInData", "someType",
				"someId");
		linkSpy.setRepeatId("someRepeatId");
		linkSpy.addAttributeByIdWithValue("someAttributeId", "someAttributeValue");
		person.addChild(linkSpy);

		String xml = extConvToXml.convertWithLinks(person, externalUrls);

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
		DataGroupSpy group = createDataRecordWithOneLinkWithReadAction();
		String xml = extConvToXml.convertWithLinks(group, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<recordToRecordLink>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</recordToRecordLink>";
		assertEquals(xml, expectedXml);
	}

	private DataGroupSpy createDataRecordWithOneLinkWithReadAction() {
		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);

		return createGroupWithNameInDataAndChildren("recordToRecordLink", dataRecordLink);
	}

	@Test
	public void testConvertLink_linkedRecord_withOneGroup() {
		DataGroupSpy group = createDataRecordWithOneLinkWithReadActionAndLinkedRecord();
		String xml = extConvToXml.convertWithLinks(group, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<recordToRecordLink>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<linkedRecord>";
		expectedXml += "<linkedSomeRecord/>";
		expectedXml += "</linkedRecord>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</recordToRecordLink>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertLink_linkedRecord_withOneGroupAndChildren() {
		DataGroupSpy group = createDataRecordWithOneLinkWithReadActionAndLinkedRecordWithChildren();
		String xml = extConvToXml.convertWithLinks(group, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<recordToRecordLink>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<linkedRecord>";
		expectedXml += "<linkedSomeRecord><groupChild01/><groupChild02/></linkedSomeRecord>";
		expectedXml += "</linkedRecord>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</recordToRecordLink>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertLink_linkedRecord_withOneGroupWithAttributes() {
		DataGroupSpy group = createDataRecordWithOneLinkWithReadActionAndLinkedRecordWithAttributes();
		String xml = extConvToXml.convertWithLinks(group, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<recordToRecordLink>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<linkedRecord>";
		expectedXml += "<linkedSomeRecord attribute01=\"value01\" attribute02=\"value02\"/>";
		expectedXml += "</linkedRecord>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</recordToRecordLink>";
		assertEquals(xml, expectedXml);
	}

	private DataGroupSpy createDataRecordWithOneLinkWithReadActionAndLinkedRecord() {
		DataGroupSpy linkedDataGroup = createGroupWithNameInDataAndChildren("linkedSomeRecord");

		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getLinkedRecord",
				() -> Optional.of(linkedDataGroup));

		return createGroupWithNameInDataAndChildren("recordToRecordLink", dataRecordLink);
	}

	private DataGroupSpy createDataRecordWithOneLinkWithReadActionAndLinkedRecordWithAttributes() {
		DataGroupSpy linkedDataGroup = createGroupWithNameInDataAndChildren("linkedSomeRecord");

		DataAttributeSpy attribute01 = createAttribute("attribute01", "value01");
		DataAttributeSpy attribute02 = createAttribute("attribute02", "value02");
		addAttributesToDataGroup(linkedDataGroup, attribute01, attribute02);

		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getLinkedRecord",
				() -> Optional.of(linkedDataGroup));
		return createGroupWithNameInDataAndChildren("recordToRecordLink", dataRecordLink);
	}

	private DataGroupSpy createDataRecordWithOneLinkWithReadActionAndLinkedRecordWithChildren() {
		DataGroupSpy groupChild01 = createGroupWithNameInData("groupChild01");
		DataGroupSpy groupChild02 = createGroupWithNameInData("groupChild02");
		DataGroupSpy linkedDataGroup = createGroupWithNameInDataAndChildren("linkedSomeRecord",
				groupChild01, groupChild02);

		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getLinkedRecord",
				() -> Optional.of(linkedDataGroup));

		return createGroupWithNameInDataAndChildren("recordToRecordLink", dataRecordLink);
	}

	private void addAttributesToDataGroup(DataGroupSpy dataGroup, DataAttributeSpy... attributes) {
		dataGroup.MRV.setDefaultReturnValuesSupplier("getAttributes",
				() -> Arrays.asList(attributes));
		dataGroup.MRV.setDefaultReturnValuesSupplier("hasAttributes", () -> true);
	}

	private DataAttributeSpy createAttribute(String nameInData, String value) {
		DataAttributeSpy attribute = new DataAttributeSpy();
		attribute.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> nameInData);
		attribute.MRV.setDefaultReturnValuesSupplier("getValue", () -> value);
		return attribute;
	}

	private DataGroupSpy createGroupWithNameInData(String nameInData) {
		DataGroupSpy linkedDataGroup = new DataGroupSpy();
		linkedDataGroup.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> nameInData);
		return linkedDataGroup;
	}

	private DataGroupSpy createGroupWithNameInDataAndChildren(String nameInData,
			DataChild... dataRecordLink) {
		DataGroupSpy dataGroup = new DataGroupSpy();
		dataGroup.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> nameInData);
		dataGroup.MRV.setDefaultReturnValuesSupplier("getChildren",
				() -> Arrays.asList(dataRecordLink));
		return dataGroup;
	}

	@Test
	public void testConvertResourceLink_noRepeatId_noAttributes_noReadAction() {
		DataResourceLinkSpy linkSpy = new DataResourceLinkSpy();
		linkSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "jp2");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getMimeType", () -> "image/jp2");

		DataRecordSpy dataRecord = createRecordWithDataResourceLink(linkSpy);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<binary>";
		expectedXml += "<jp2>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<mimeType>image/jp2</mimeType>";
		expectedXml += "</jp2>";
		expectedXml += "</binary>";
		expectedXml += "</data>";
		expectedXml += "</record>";

		assertEquals(xml, expectedXml);
	}

	private DataRecordSpy createRecordWithDataResourceLink(DataResourceLink link) {
		DataRecordSpy dataRecord = new DataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getType", () -> "fakeType");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "fakeId");

		DataRecordGroupSpy personRecordGroup = new DataRecordGroupSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup",
				() -> personRecordGroup);

		DataGroupSpy person = new DataGroupSpy();
		dataFactorySpy.MRV.setSpecificReturnValuesSupplier("factorGroupFromDataRecordGroup",
				() -> person, personRecordGroup);

		person.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "binary");

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy();
		linkSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "someLinkNameInData");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getLinkedRecordType", () -> "someType");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getLinkedRecordId", () -> "someId");

		List<DataChild> personChildren = new ArrayList<>();
		personChildren.add(link);
		person.MRV.setDefaultReturnValuesSupplier("getChildren", () -> personChildren);
		return dataRecord;
	}

	@Test
	public void testConvertResourceLink_withRepeatId_noReadAction() {
		DataResourceLinkSpy linkSpy = createResourceLinkNoReadAction();

		DataRecordSpy dataRecord = createRecordWithDataResourceLink(linkSpy);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<binary>";
		expectedXml += "<jp2 repeatId=\"someRepeatId\">";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<mimeType>image/jp2</mimeType>";
		expectedXml += "</jp2>";
		expectedXml += "</binary>";
		expectedXml += "</data>";
		expectedXml += "</record>";

		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertResourceLink_readActionNoLinksRequested() {
		DataResourceLinkSpy linkSpy = createResourceLinkWithReadAction();
		DataRecordSpy dataRecord = createRecordWithDataResourceLink(linkSpy);

		String xml = extConvToXml.convert(dataRecord);

		String expectedXml = XML_DECLARATION;
		expectedXml += "<record>";
		expectedXml += "<data>";
		expectedXml += "<binary>";
		expectedXml += "<jp2 repeatId=\"someRepeatId\">";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<mimeType>image/jp2</mimeType>";
		expectedXml += "</jp2>";
		expectedXml += "</binary>";
		expectedXml += "</data>";
		expectedXml += "</record>";
		assertEquals(xml, expectedXml);
	}

	@Test
	public void testConvertResourceLink_readAction() {
		DataResourceLinkSpy resourceLinkSpy = createResourceLinkWithReadAction();
		DataRecordSpy dataRecord = createRecordWithDataResourceLink(resourceLinkSpy);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedXml = XML_DECLARATION;
		expectedXml += expectedXMLForRecordResourceLink(resourceLinkSpy);
		assertEquals(xml, expectedXml);
		System.out.println(xml);
	}

	private DataResourceLinkSpy createResourceLinkWithReadAction() {
		DataResourceLinkSpy linkSpy = createResourceLinkNoReadAction();
		linkSpy.MRV.setDefaultReturnValuesSupplier("hasReadAction", () -> true);
		return linkSpy;
	}

	private DataResourceLinkSpy createResourceLinkNoReadAction() {
		DataResourceLinkSpy linkSpy = new DataResourceLinkSpy();
		linkSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "jp2");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getMimeType", () -> "image/jp2");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getRepeatId", () -> "someRepeatId");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getAttributes", Collections::emptySet);
		return linkSpy;
	}

	private String expectedXMLForRecordResourceLink(DataResourceLinkSpy resourceLink) {

		String expectedXml = "<record>";
		expectedXml += "<data>";
		expectedXml += "<binary>";
		expectedXml += "<jp2 repeatId=\"someRepeatId\">";
		expectedXml += "<linkedRecordType>" + resourceLink.MCR.getReturnValue("getType", 0)
				+ "</linkedRecordType>";
		expectedXml += "<linkedRecordId>" + resourceLink.MCR.getReturnValue("getId", 0)
				+ "</linkedRecordId>";
		expectedXml += "<mimeType>image/jp2</mimeType>";

		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/"
				+ resourceLink.MCR.getReturnValue("getType", 0) + "/"
				+ resourceLink.MCR.getReturnValue("getId", 0) + "/jp2</url>";
		expectedXml += "<accept>image/jp2</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";

		expectedXml += "</jp2>";
		expectedXml += "</binary>";
		expectedXml += "</data>";
		expectedXml += "</record>";

		return expectedXml;
	}

	@Test
	public void testConvertListWithResourceLink_readAction() {
		// * test for list of records with resourceLink, uses recordType and id from current
		// record<br>
		DataResourceLinkSpy resourceLink01 = createResourceLinkWithReadAction();
		DataRecordSpy dataRecord1 = createRecordWithDataResourceLink(resourceLink01);
		DataResourceLinkSpy resourceLink02 = createResourceLinkWithReadAction();
		DataRecordSpy dataRecord2 = createRecordWithDataResourceLink(resourceLink02);
		DataListSpy dataList = createDataList(dataRecord1, dataRecord2);

		String xml = extConvToXml.convertWithLinks(dataList, externalUrls);

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
		expectedListXml += expectedXMLForRecordResourceLink(resourceLink01);
		expectedListXml += expectedXMLForRecordResourceLink(resourceLink02);
		expectedListXml += "</data>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);

	}

	private DataListSpy createDataList(Data... data) {
		DataListSpy dataList = new DataListSpy();
		dataList.MRV.setDefaultReturnValuesSupplier("getFromNo", () -> "1");
		dataList.MRV.setDefaultReturnValuesSupplier("getToNo", () -> "99");
		dataList.MRV.setDefaultReturnValuesSupplier("getTotalNumberOfTypeInStorage", () -> "9999");
		dataList.MRV.setDefaultReturnValuesSupplier("getContainDataOfType", () -> "mix");

		dataList.MRV.setDefaultReturnValuesSupplier("getDataList", () -> Arrays.asList(data));
		return dataList;
	}

	@Test
	public void testConvertIncomingLinksDataGroups() {
		DataListSpy dataList = new DataListSpy();
		dataList.MRV.setDefaultReturnValuesSupplier("getFromNo", () -> "1");
		dataList.MRV.setDefaultReturnValuesSupplier("getToNo", () -> "99");
		dataList.MRV.setDefaultReturnValuesSupplier("getTotalNumberOfTypeInStorage", () -> "9999");
		dataList.MRV.setDefaultReturnValuesSupplier("getContainDataOfType", () -> "mix");
		DataGroupSpy dataRecord1 = createDataRecordWithOneLinkWithReadAction();
		DataGroupSpy dataRecord2 = createDataRecordWithOneLinkWithReadAction();
		dataList.MRV.setDefaultReturnValuesSupplier("getDataList",
				() -> List.of(dataRecord1, dataRecord2));

		String xml = extConvToXml.convertWithLinks(dataList, externalUrls);

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
		expectedListXml += expectedXMLForDataGroupWithRecordLink();
		expectedListXml += expectedXMLForDataGroupWithRecordLink();
		expectedListXml += "</data>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);
	}

	private String expectedXMLForDataGroupWithRecordLink() {

		String expectedXml = "<recordToRecordLink>";
		expectedXml += "<someLinkNameInData>";
		expectedXml += "<linkedRecordType>someType</linkedRecordType>";
		expectedXml += "<linkedRecordId>someId</linkedRecordId>";
		expectedXml += "<actionLinks>";
		expectedXml += "<read>";
		expectedXml += "<requestMethod>GET</requestMethod>";
		expectedXml += "<rel>read</rel>";
		expectedXml += "<url>https://some.domain.now/rest/record/someType/someId</url>";
		expectedXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedXml += "</read>";
		expectedXml += "</actionLinks>";
		expectedXml += "</someLinkNameInData>";
		expectedXml += "</recordToRecordLink>";
		return expectedXml;
	}

	// TODO:
	/**
	 * test for list of records with resourceLink, uses recordType and id from current record<br>
	 * test for one dataGroup convertWithLinks should throw exception (as we can not calculate
	 * resource links)<br>
	 * test for list of dataGroups convertWithLinks should throw exception
	 */

	// TODO: where do we get type and id from (master is nameInData)?
	/**
	 * Do we need two different interfaces ExternallyConvertible --> for going out through the API.
	 * Known imp. DataList, DataRecord.<br>
	 * convertWithLinks(ExternallyConvertible)
	 * <p>
	 * and InternallyConvertible for going to external resources such as databases? Known imp.
	 * DataGroup<br>
	 * convert(InternallyConvertible)
	 */

	@Test
	public void testConvertRecord_forAllActions_hasNoActionLinksInResult() {
		for (Action action : Action.values()) {
			DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(action);

			String xml = extConvToXml.convert(dataRecord);

			String expectedActionLinksXml = "";
			assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
		}
	}

	@Test
	public void testConvertRecordWithLinks_noAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions();

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_readAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.READ);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<read>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>read</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedActionLinksXml += "</read>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_deleteAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.DELETE);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<delete>";
		expectedActionLinksXml += "<requestMethod>DELETE</requestMethod>";
		expectedActionLinksXml += "<rel>delete</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "</delete>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_updateAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.UPDATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<update>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>update</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId</url>";
		expectedActionLinksXml += "<contentType>" + APPLICATION_VND_CORA_RECORDGROUP_XML
				+ "</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedActionLinksXml += "</update>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_incommingLinksAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.READ_INCOMING_LINKS);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<read_incoming_links>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>read_incoming_links</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId/incomingLinks</url>";
		expectedActionLinksXml += "<accept>application/vnd.cora.recordList+xml</accept>";
		expectedActionLinksXml += "</read_incoming_links>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_indexAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.INDEX);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<index>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>index</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/workOrder</url>";
		expectedActionLinksXml += "<contentType>" + APPLICATION_VND_CORA_RECORDGROUP_XML
				+ "</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
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
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_uploadAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.UPLOAD);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<upload>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>upload</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeType/fakeId/master</url>";
		expectedActionLinksXml += "<contentType>multipart/form-data</contentType>";
		expectedActionLinksXml += "</upload>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_searchAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.SEARCH);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String searchId = (String) dataRecord.MCR.getReturnValue("getSearchId", 0);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<search>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>search</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/searchResult/"
				+ searchId + "</url>";
		expectedActionLinksXml += "<accept>application/vnd.cora.recordList+xml</accept>";
		expectedActionLinksXml += "</search>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_createAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.CREATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<create>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>create</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeId</url>";
		expectedActionLinksXml += "<contentType>" + APPLICATION_VND_CORA_RECORDGROUP_XML
				+ "</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedActionLinksXml += "</create>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_listAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.LIST);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<list>";
		expectedActionLinksXml += "<requestMethod>GET</requestMethod>";
		expectedActionLinksXml += "<rel>list</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/fakeId</url>";
		expectedActionLinksXml += "<accept>application/vnd.cora.recordList+xml</accept>";
		expectedActionLinksXml += "</list>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_batchIndexAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.BATCH_INDEX);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<batch_index>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>batch_index</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/index/fakeId</url>";
		expectedActionLinksXml += "<contentType>application/vnd.cora.record+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedActionLinksXml += "</batch_index>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	@Test
	public void testConvertRecordWithLinks_validateAction() {
		DataRecordSpy dataRecord = createRecordWithLinkAddRecordActions(Action.VALIDATE);

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedActionLinksXml = "<actionLinks>";
		expectedActionLinksXml += "<validate>";
		expectedActionLinksXml += "<requestMethod>POST</requestMethod>";
		expectedActionLinksXml += "<rel>validate</rel>";
		expectedActionLinksXml += "<url>https://some.domain.now/rest/record/workOrder</url>";
		expectedActionLinksXml += "<contentType>application/vnd.cora.workorder+xml</contentType>";
		expectedActionLinksXml += "<accept>application/vnd.cora.record+xml</accept>";
		expectedActionLinksXml += "</validate>";
		expectedActionLinksXml += "</actionLinks>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedActionLinksXml);
	}

	private DataRecordSpy createRecordWithLinkAddRecordActions(Action... actions) {
		DataRecordSpy dataRecord = new DataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getType", () -> "fakeType");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "fakeId");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getActions", () -> Arrays.asList(actions));
		if (actions.length > 0) {
			dataRecord.MRV.setDefaultReturnValuesSupplier("hasActions", () -> true);
		}

		DataRecordGroupSpy personRecordGroup = new DataRecordGroupSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup",
				() -> personRecordGroup);

		DataGroupSpy person = new DataGroupSpy();
		dataFactorySpy.MRV.setSpecificReturnValuesSupplier("factorGroupFromDataRecordGroup",
				() -> person, personRecordGroup);

		person.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "person");

		DataRecordLinkSpy linkSpy = new DataRecordLinkSpy();
		linkSpy.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "someLinkNameInData");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getLinkedRecordType", () -> "someType");
		linkSpy.MRV.setDefaultReturnValuesSupplier("getLinkedRecordId", () -> "someId");

		List<DataChild> personChildren = new ArrayList<>();
		personChildren.add(linkSpy);
		person.MRV.setDefaultReturnValuesSupplier("getChildren", () -> personChildren);
		// TODO: make converter return dataGroup for this dataRecordGroup

		return dataRecord;
	}

	private void assertRecordCorrectWithSuppliedExpectedPart(String xmlToAssert,
			String expectedActionLinksXml) {
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
		assertEquals(xmlToAssert, expectedXml);
	}

	@Test
	public void testToXmlWithLinks_noPermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(List.of(), List.of());

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedPermissionsXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedPermissionsXml);
	}

	@Test
	public void testToXmlWithLinks_ListOfReadPermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"), List.of());

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedPermissionsXml = "<permissions>";
		expectedPermissionsXml += "<read>";
		expectedPermissionsXml += "<permission>readPermissionOne</permission>";
		expectedPermissionsXml += "<permission>readPermissionTwo</permission>";
		expectedPermissionsXml += "</read>";
		expectedPermissionsXml += "</permissions>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedPermissionsXml);
	}

	@Test
	public void testToXmlWithLinks_ListOfWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(List.of(),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String expectedPermissionsXml = "<permissions>";
		expectedPermissionsXml += "<write>";
		expectedPermissionsXml += "<permission>writePermissionOne</permission>";
		expectedPermissionsXml += "<permission>writePermissionTwo</permission>";
		expectedPermissionsXml += "</write>";
		expectedPermissionsXml += "</permissions>";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedPermissionsXml);
	}

	@Test
	public void testToXmlWithLinks_ListOfReadAndWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

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

		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedPermissionsXml);
	}

	@Test
	public void testToXmlWithoutLinks_ListOfReadAndWritePermissions() {
		DataRecordSpy dataRecord = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));

		String xml = extConvToXml.convert(dataRecord);

		String expectedPermissionsXml = "";
		assertRecordCorrectWithSuppliedExpectedPart(xml, expectedPermissionsXml);
	}

	private DataRecordSpy createRecordWithReadAndWritePermissions(List<String> readPermissions,
			List<String> writePermissions) {
		DataRecordSpy dataRecord = new DataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getType", () -> "fakeType");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "fakeId");

		DataRecordGroupSpy personRecordGroup = new DataRecordGroupSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup",
				() -> personRecordGroup);

		DataGroupSpy person = new DataGroupSpy();
		dataFactorySpy.MRV.setSpecificReturnValuesSupplier("factorGroupFromDataRecordGroup",
				() -> person, personRecordGroup);

		person.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "person");

		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		person.MRV.setDefaultReturnValuesSupplier("getChildren", () -> List.of(dataRecordLink));

		LinkedHashSet<String> readSet = new LinkedHashSet<>();
		readSet.addAll(readPermissions);
		LinkedHashSet<String> writeSet = new LinkedHashSet<>();
		writeSet.addAll(writePermissions);

		dataRecord.MRV.setDefaultReturnValuesSupplier("getReadPermissions", () -> readSet);
		if (readSet.size() > 0) {
			dataRecord.MRV.setDefaultReturnValuesSupplier("hasReadPermissions", () -> true);
		}
		dataRecord.MRV.setDefaultReturnValuesSupplier("getWritePermissions", () -> writeSet);
		if (writeSet.size() > 0) {
			dataRecord.MRV.setDefaultReturnValuesSupplier("hasWritePermissions", () -> true);
		}

		return dataRecord;
	}

	@Test
	public void testToXmlWithoutLinks_ListOfRecordsNoRecord() {
		DataListSpy dataList = createDataList();

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
	public void testToXmlWithoutLinks_ListOfRecordsTwoRecords() {
		DataRecordSpy dataRecord1 = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));
		DataRecordSpy dataRecord2 = createRecordWithLinkAddRecordActions(Action.VALIDATE);

		DataListSpy dataList = createDataList(dataRecord1, dataRecord2);

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
	public void testToXmlWithLinks_ListOfRecordsTwoRecords() {
		DataRecordSpy dataRecord1 = createRecordWithReadAndWritePermissions(
				List.of("readPermissionOne", "readPermissionTwo"),
				List.of("writePermissionOne", "writePermissionTwo"));
		DataRecordSpy dataRecord2 = createRecordWithLinkAddRecordActions(Action.VALIDATE);

		DataListSpy dataList = createDataList(dataRecord1, dataRecord2);

		String xml = extConvToXml.convertWithLinks(dataList, externalUrls);

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
				extConvToXml.convertWithLinks(dataRecord1, externalUrls));
		expectedListXml += removeXmlDeclaration(
				extConvToXml.convertWithLinks(dataRecord2, externalUrls));
		expectedListXml += "</data>";
		expectedListXml += "</dataList>";

		assertEquals(xml, expectedListXml);
	}

	private String removeXmlDeclaration(String xml) {
		return xml.substring(XML_DECLARATION.length());
	}

	@Test
	public void testOtherProtocols() {

		DataRecordSpy dataRecord = createDataRecordWithOneLink();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "someRecordId");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getProtocols", () -> Set.of("iiif"));

		String xml = extConvToXml.convertWithLinks(dataRecord, externalUrls);

		String otherProtocolsXml = "<otherProtocols>";
		otherProtocolsXml += "<iiif>";
		otherProtocolsXml += "<server>" + IIIF_URL + "</server>";
		otherProtocolsXml += "<identifier>someRecordId</identifier>";
		otherProtocolsXml += "</iiif>";
		otherProtocolsXml += "</otherProtocols>";

		assertRecordCorrectWithSuppliedExpectedPart(xml, otherProtocolsXml);

	}

	private DataRecordSpy createDataRecordWithOneLink() {
		DataRecordSpy dataRecord = new DataRecordSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getType", () -> "fakeType");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "fakeId");

		DataRecordGroupSpy dataRecordGroup = new DataRecordGroupSpy();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getDataRecordGroup", () -> dataRecordGroup);

		DataGroupSpy dataGroup = new DataGroupSpy();
		dataFactorySpy.MRV.setSpecificReturnValuesSupplier("factorGroupFromDataRecordGroup",
				() -> dataGroup, dataRecordGroup);

		dataGroup.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> "person");

		DataRecordLinkSpy dataRecordLink = createRecordLink("someLinkNameInData", "someType",
				"someId");
		dataGroup.MRV.setDefaultReturnValuesSupplier("getChildren", () -> List.of(dataRecordLink));
		return dataRecord;
	}

	private DataRecordLinkSpy createRecordLink(String linkedName, String linkedType,
			String linkToId) {
		DataRecordLinkSpy dataRecordLink = new DataRecordLinkSpy();
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getNameInData", () -> linkedName);
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getLinkedRecordType", () -> linkedType);
		dataRecordLink.MRV.setDefaultReturnValuesSupplier("getLinkedRecordId", () -> linkToId);
		return dataRecordLink;
	}

	@Test
	public void testConvertWithoutLinkOtherProtocolsShouldNotBeInXML() {

		DataRecordSpy dataRecord = createDataRecordWithOneLink();
		dataRecord.MRV.setDefaultReturnValuesSupplier("getId", () -> "someRecordId");
		dataRecord.MRV.setDefaultReturnValuesSupplier("getProtocols", () -> Set.of("iiif"));

		String xml = extConvToXml.convert(dataRecord);

		String empty = "";

		assertRecordCorrectWithSuppliedExpectedPart(xml, empty);

	}
}
