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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;

public class XmlToDataElementTest {

	DataGroupFactorySpy dataGroupFactorySpy = null;
	DataAtomicFactorySpy dataAtomicFactorySpy = null;

	private DocumentBuilderFactory documentBuilderFactory;
	private XmlToDataElement xmlToDataElement;

	@BeforeMethod
	public void setUp() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		xmlToDataElement = new XmlToDataElement(documentBuilderFactory);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Document must be: version 1.0 and UTF-8")
	public void testParseExceptionWhenNotCorrectVerisonAndEncoding() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"notUTF-8\"?>"
				+ "<person><firstname/></person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: some message from DocumentBuilderFactorySpy")
	public void testParseExceptionOnCreateDocument() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";

		setUpXmlToDataElementWithDocumentFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		xmlToDataElement.convert(xmlToConvert);
	}

	private void setUpXmlToDataElementWithDocumentFactorySpy() {
		documentBuilderFactory = new DocumentBuilderFactorySpy();
		xmlToDataElement = new XmlToDataElement(documentBuilderFactory);
	}

	@Test
	public void testParseExceptionOriginalExceptionIsSentAlong() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";
		setUpXmlToDataElementWithDocumentFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		try {
			xmlToDataElement.convert(xmlToConvert);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof ParserConfigurationException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML")
	public void testSaxExceptionOnParseMalformedXML() {
		String xmlToConvert = "noXML";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testSaxExceptionOnParseMalformedXMLOriginalExceptionIsSentAlong() {
		String xmlToConvert = "noXML";

		try {
			xmlToDataElement.convert(xmlToConvert);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof SAXException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: null")
	public void testExceptionOnNullXML() {
		String xmlToConvert = null;
		setUpXmlToDataElementWithDocumentFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwIOException = true;
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML")
	public void testSaxExceptionOnParseEmptyXML() {
		String xmlToConvert = "";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testSaxExceptionOnParseEmptyXMLOriginalExceptionIsSentAlong() {

		try {
			xmlToDataElement.convert("");
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof SAXException);
		}
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElement() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithSpace() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person> </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithTwoSpace() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person>  </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithText() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person> dummy text </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testConvertXmlWithSingleAtomicChild() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname>Kalle</firstname>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithSpace() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(" <firstname>Kalle</firstname> ");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithSpaceAroundText() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname> Kalle </firstname>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithNewLineStartingText() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname>\nKalle</firstname>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithNewLineInsideText() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname>Kal\nle</firstname>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"),
				"Kal\nle");
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: DataAtomic can not have attributes")
	public void testConvertXmlWithAttribute() {
		String atomicXml = "<firstname someAttribute=\"attrib\">Kalle</firstname>";
		String xmlToConvert = surroundWithTopLevelXmlGroup(atomicXml);

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	private String surroundWithTopLevelXmlGroup(String atomicXml) {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person>" + atomicXml
				+ "</person>";
		return xmlToConvert;
	}

	@Test
	public void testConvertXmlWithMultipleDataGroupAndAtomicChild() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name><firstname>Kalle</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithMultipleDataGroupAndSpaceAroundAtomicChild() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name> <firstname>Kalle</firstname> </name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithoutValue() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname></firstname>");

		DataElement convertedDataElement = xmlToDataElement.convert(xmlToConvert);
		DataGroup convertedDataGroup = (DataGroup) convertedDataElement;
		assertEquals(convertedDataGroup.getFirstAtomicValueWithNameInData("firstname"), "");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithoutValue2() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname/>");
		DataElement convertedDataElement = xmlToDataElement.convert(xmlToConvert);
		DataGroup convertedDataGroup = (DataGroup) convertedDataElement;
		assertEquals(convertedDataGroup.getFirstAtomicValueWithNameInData("firstname"), "");
	}

	@Test
	public void testAttributesAddedToDataGroup() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name type=\"authenticated\"><firstname>Janne</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type"),
				"authenticated");
	}

	@Test
	public void testWithRunicCharacters() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name><firstname>ᚠᚢᚦᚮᚱᚴ</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("firstname"), "ᚠᚢᚦᚮᚱᚴ");
	}

	@Test
	public void testAttributesRepeatId() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name type=\"authenticated\" repeatId=\"1\">"
						+ "<firstname repeatId=\"2\">Janne</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);

		DataGroup firstDataGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(firstDataGroup.getRepeatId(), "1");

		DataAtomic dataAtomic = (DataAtomic) firstDataGroup
				.getFirstChildWithNameInData("firstname");
		assertEquals(dataAtomic.getRepeatId(), "2");
	}

	@Test
	public void testMultipleAttributes() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name type=\"authenticated\" multiple=\"yes\" repeatId=\"1\">"
						+ "<firstname repeatId=\"2\">Janne</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type"),
				"authenticated");
		assertEquals(
				convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("multiple"),
				"yes");
	}

	@Test
	public void testAttributesOnParentGroup() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person gender=\"man\"><firstname>Janne</firstname></person>";

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getAttribute("gender"), "man");

	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Top dataGroup can not have repeatId")
	public void testRepeatIdOnParentGroup() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person repeatId=\"someRepeatId\"><firstname>Janne</firstname></person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testCompleteExample() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name type=\"authenticated\" multiple=\"yes\" repeatId=\"1\">"
						+ "<firstname repeatId=\"2\">Janne</firstname>"
						+ "<secondname repeatId=\"3\">Fonda</secondname>"
						+ "<nickname><short>Fondis</short></nickname>" + "</name>"
						+ "<shoesize>14</shoesize>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);

		assertEquals(convertedDataElement.getNameInData(), "person");

		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getAttribute("type"), "authenticated");
		assertEquals(nameGroup.getAttribute("multiple"), "yes");

		DataAtomic secondnameAtomic = (DataAtomic) nameGroup
				.getFirstChildWithNameInData("secondname");
		assertEquals(secondnameAtomic.getRepeatId(), "3");
		assertEquals(secondnameAtomic.getValue(), "Fonda");

		DataAtomic shoeSize = (DataAtomic) convertedDataElement
				.getFirstChildWithNameInData("shoesize");
		assertEquals(shoeSize.getValue(), "14");
		assertEquals(shoeSize.getRepeatId(), null);

		DataGroup nicknameGroup = nameGroup.getFirstGroupWithNameInData("nickname");
		assertEquals(nicknameGroup.getFirstAtomicValueWithNameInData("short"), "Fondis");
	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML")
	public void testWithMultipleXmlRootElements() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person gender=\"man\"><firstname>Janne</firstname></person>"
				+ "<person gender=\"man\"><firstname>John</firstname></person>";
		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testXmlWithSpacesBetweenTags() throws Exception {
		String xmlFromXsltAlvinFedoraToCoraPlaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<authority type=\"place\">" + " <recordinfo>" + " <id>alvin-place:22</id>"
				+ " <type>" + " <linkedRecordType>recordType</linkedRecordType>"
				+ " <linkRecordId>place</linkRecordId>" + " </type>" + " <createdBy>"
				+ " <linkedRecordType>user</linkedRecordType>"
				+ " <linkRecordId>test</linkRecordId>" + " </createdBy>"
				+ " <tsCreated>2014-12-18 20:20:38.346 UTC</tsCreated>" + " <dataDivider>"
				+ " <linkedRecordType>system</linkedRecordType>"
				+ " <linkRecordId>alvin</linkRecordId>" + " </dataDivider>"
				+ " <updated repeatId=\"0\">" + " <updatedBy>"
				+ " <linkedRecordType>user</linkedRecordType>"
				+ " <linkRecordId>test</linkRecordId>" + " </updatedBy>"
				+ " <tsUpdated>2014-12-18 20:21:20.880 UTC</tsUpdated>" + " </updated>"
				+ " </recordinfo>" + " <name type=\"authorized\">"
				+ " <namePart>Linköping</namePart>" + " </name>" + " <coordinates>"
				+ " <latitude>58.42</latitude>" + " <longitude>15.62</longitude>"
				+ " </coordinates>" + " <country>SE</country>" + " <identifier repeatId=\"0\">"
				+ " <identifierType>waller</identifierType>"
				+ " <identifierValue>114</identifierValue>" + " </identifier>" + "</authority>";
		DataGroup topDataGroup = (DataGroup) xmlToDataElement
				.convert(xmlFromXsltAlvinFedoraToCoraPlaces);
		DataGroup identifier = (DataGroup) topDataGroup.getFirstChildWithNameInData("identifier");
		assertEquals(identifier.getRepeatId(), "0");

		DataAtomic identifierType = (DataAtomic) identifier
				.getFirstChildWithNameInData("identifierType");
		assertEquals(identifierType.getValue(), "waller");
	}
}
