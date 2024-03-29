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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import se.uu.ub.cora.converter.ConverterException;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.data.DataProvider;
import se.uu.ub.cora.data.DataRecordLinkProvider;
import se.uu.ub.cora.data.ExternallyConvertible;
import se.uu.ub.cora.testspies.data.DataFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.OldDataGroupSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;

public class XmlToExternallyConvertibleTest {
	DataFactorySpy dataFactorySpy;
	DataGroupFactorySpy dataGroupFactorySpy = null;
	DataAtomicFactorySpy dataAtomicFactorySpy = null;
	DataRecordLinkFactorySpy dataRecordLinkFactory = null;

	private DocumentBuilderFactory documentBuilderFactory;
	private XmlToExternallyConvertible xmlToDataElement;

	@BeforeMethod
	public void setUp() {
		dataFactorySpy = new DataFactorySpy();
		DataProvider.onlyForTestSetDataFactory(dataFactorySpy);

		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		dataRecordLinkFactory = new DataRecordLinkFactorySpy();
		DataRecordLinkProvider.setDataRecordLinkFactory(dataRecordLinkFactory);

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		xmlToDataElement = new XmlToExternallyConvertible(documentBuilderFactory);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Document must be: version 1.0 and UTF-8")
	public void testParseExceptionWhenNotCorrectVerisonAndEncoding() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"notUTF-8\"?>"
				+ "<person><firstname/></person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: some message from DocumentBuilderFactorySpy")
	public void testParseExceptionOnCreateDocument() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";

		setUpXmlToDataElementWithDocumentFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwParserError = true;

		xmlToDataElement.convert(xmlToConvert);
	}

	private void setUpXmlToDataElementWithDocumentFactorySpy() {
		documentBuilderFactory = new DocumentBuilderFactorySpy();
		xmlToDataElement = new XmlToExternallyConvertible(documentBuilderFactory);
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

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML: noXML")
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

	@Test
	public void testExceptionOnNullXML() {
		String xmlToConvert = null;
		setUpXmlToDataElementWithDocumentFactorySpy();
		((DocumentBuilderFactorySpy) documentBuilderFactory).throwIOException = true;
		Exception expectedException = null;
		try {
			xmlToDataElement.convert(xmlToConvert);
		} catch (Exception e) {
			expectedException = e;
		}
		assertTrue(expectedException instanceof ConverterException);

		assertMessageIsCorrectForJava14or15(expectedException);
	}

	private void assertMessageIsCorrectForJava14or15(Exception expectedException) {
		String exceptionMessage = expectedException.getMessage();
		String errorStartsWith = "Unable to convert from xml to dataElement: ";
		assertTrue(exceptionMessage.startsWith(errorStartsWith));
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to malformed XML: ")
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

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElement() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithSpace() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person> </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithTwoSpace() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person>  </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithText() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person> dummy text </person>";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithSpace() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(" <firstname>Kalle</firstname> ");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChild() {
		convertInsideFirstNameTagAndAssertEqual("Kalle", "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithSpaceAroundText() {
		convertInsideFirstNameTagAndAssertEqual(" Kalle ", "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithNewLineStartingText() {
		convertInsideFirstNameTagAndAssertEqual("\nKalle", "Kalle");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithNewLineInsideText() {
		convertInsideFirstNameTagAndAssertEqual("Kal\nle", "Kal\nle");
	}

	@Test
	public void testConvertXmlWithHtmlParagraphInsideTextSurroundedByCdata() {
		convertInsideFirstNameTagAndAssertEqual(
				"<![CDATA[&lt;p&gt; &quot;trams&quot; &lt;/p&gt;]]>",
				"&lt;p&gt; &quot;trams&quot; &lt;/p&gt;");
	}

	@Test(enabled = false)
	public void weWouldLikeItToWorkLikeThisButItDoesNot_testConvertXmlWithHtmlParagraphInsideText() {
		convertInsideFirstNameTagAndAssertEqual("&lt;p&gt; &quot;trams &lt;/p&gt;",
				"&lt;p&gt; &quot;trams &lt;/p&gt;");
	}

	private void convertInsideFirstNameTagAndAssertEqual(String valueToConvert,
			String convertedValue) {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<firstname>" + valueToConvert + "</firstname>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"),
				convertedValue);
	}

	@Test
	public void testConvertXmlWithAttribute() {
		String atomicXml = "<firstname someAttribute=\"attrib\" someAttribute2=\"attrib2\">Kalle</firstname>";
		String xmlToConvert = surroundWithTopLevelXmlGroup(atomicXml);

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");
		DataChild firstName = convertedDataElement.getFirstChildWithNameInData("firstname");
		assertEquals(firstName.getAttribute("someAttribute").getValue(), "attrib");
		assertEquals(firstName.getAttribute("someAttribute2").getValue(), "attrib2");
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
		String valueToConvert = "";
		String convertedValue = "";
		convertInsideFirstNameTagAndAssertEqual(valueToConvert, convertedValue);
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithoutValue2() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("<firstname/>");
		ExternallyConvertible convertedDataElement = xmlToDataElement.convert(xmlToConvert);
		DataGroup convertedDataGroup = (DataGroup) convertedDataElement;
		assertEquals(convertedDataGroup.getFirstAtomicValueWithNameInData("firstname"), "");
	}

	@Test
	public void testAttributesAddedToDataGroup() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name type=\"authenticated\"><firstname>Janne</firstname></name>");

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type")
				.getValue(), "authenticated");
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
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type")
				.getValue(), "authenticated");
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name")
				.getAttribute("multiple").getValue(), "yes");
	}

	@Test
	public void testAttributesOnParentGroup() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person gender=\"man\"><firstname>Janne</firstname></person>";

		DataGroup convertedDataElement = (DataGroup) xmlToDataElement.convert(xmlToConvert);
		assertEquals(convertedDataElement.getAttribute("gender").getValue(), "man");

	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
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
		assertEquals(nameGroup.getAttribute("type").getValue(), "authenticated");
		assertEquals(nameGroup.getAttribute("multiple").getValue(), "yes");

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

	@Test
	public void testWithMultipleXmlRootElements() {
		String xmlToConvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person gender=\"man\"><firstname>Janne</firstname></person>"
				+ "<person gender=\"man\"><firstname>John</firstname></person>";
		try {
			xmlToDataElement.convert(xmlToConvert);
			makeSureErrorIsThrown();
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Unable to convert from xml to dataElement due to malformed XML: "
							+ xmlToConvert);
		}
	}

	private void makeSureErrorIsThrown() {
		assertTrue(false);
	}

	@Test
	public void testXmlWithMoreDataInXml() throws Exception {
		String xmlFromXsltAlvinFedoraToCoraPlaces = getPlaceXml();
		DataGroup topDataGroup = (DataGroup) xmlToDataElement
				.convert(xmlFromXsltAlvinFedoraToCoraPlaces);
		DataGroup identifier = (DataGroup) topDataGroup.getFirstChildWithNameInData("identifier");
		assertEquals(identifier.getRepeatId(), "0");

		DataAtomic identifierType = (DataAtomic) identifier
				.getFirstChildWithNameInData("identifierType");
		assertEquals(identifierType.getValue(), "waller");
	}

	private String getPlaceXml() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<authority type=\"place\">" + " <recordInfo>" + " <id>alvin-place:22</id>"
				+ " <type>" + " <linkedRecordType>recordType</linkedRecordType>"
				+ " <linkedRecordId>place</linkedRecordId>" + " </type>" + " <createdBy>"
				+ " <linkedRecordType>user</linkedRecordType>"
				+ " <linkedRecordId>test</linkedRecordId>" + " </createdBy>"
				+ " <tsCreated>2014-12-18 20:20:38.346 UTC</tsCreated>" + " <dataDivider>"
				+ " <linkedRecordType>system</linkedRecordType>"
				+ " <linkedRecordId>alvin</linkedRecordId>" + " </dataDivider>"
				+ " <updated repeatId=\"0\">" + " <updatedBy>"
				+ " <linkedRecordType>user</linkedRecordType>"
				+ " <linkedRecordId>test</linkedRecordId>" + " </updatedBy>"
				+ " <tsUpdated>2014-12-18 20:21:20.880 UTC</tsUpdated>" + " </updated>"
				+ " </recordInfo>" + " <name type=\"authorized\">"
				+ " <namePart>Linköping</namePart>" + " </name>" + " <coordinates>"
				+ " <latitude>58.42</latitude>" + " <longitude>15.62</longitude>"
				+ " </coordinates>" + " <country>SE</country>" + " <identifier repeatId=\"0\">"
				+ " <identifierType>waller</identifierType>"
				+ " <identifierValue>114</identifierValue>" + " </identifier>" + "</authority>";
		return xmlFromXsltAlvinFedoraToCoraPlaces;
	}

	@Test
	public void testXmlWithLinks() throws Exception {
		dataFactorySpy.MRV.setReturnValues("createRecordLinkUsingNameInDataAndTypeAndId",
				List.of(new se.uu.ub.cora.testspies.data.DataRecordLinkSpy()), "link1",
				"recordType", "place");

		String dataGroupWithLinks = getLinksXml();

		OldDataGroupSpy topDataGroup = (OldDataGroupSpy) xmlToDataElement.convert(dataGroupWithLinks);

		assertCorrectLink(0, "link1", "recordType", "place");
		se.uu.ub.cora.testspies.data.DataRecordLinkSpy link1Spy = (se.uu.ub.cora.testspies.data.DataRecordLinkSpy) dataFactorySpy.MCR
				.getReturnValue("factorRecordLinkUsingNameInDataAndTypeAndId", 0);
		var addedChild1 = topDataGroup.children.get(0);
		assertSame(addedChild1, link1Spy);

		assertCorrectLink(1, "link2", "user", "test");
		se.uu.ub.cora.testspies.data.DataRecordLinkSpy link2Spy = (se.uu.ub.cora.testspies.data.DataRecordLinkSpy) dataFactorySpy.MCR
				.getReturnValue("factorRecordLinkUsingNameInDataAndTypeAndId", 1);
		link2Spy.MCR.assertParameters("setRepeatId", 0, "33");
		var addedChild2 = topDataGroup.children.get(1);
		assertSame(addedChild2, link2Spy);

		assertCorrectLink(2, "link3", "user", "test");
		se.uu.ub.cora.testspies.data.DataRecordLinkSpy link3Spy = (se.uu.ub.cora.testspies.data.DataRecordLinkSpy) dataFactorySpy.MCR
				.getReturnValue("factorRecordLinkUsingNameInDataAndTypeAndId", 2);
		link3Spy.MCR.assertParameters("addAttributeByIdWithValue", 0, "type", "someAttribute");
		var addedChild3 = topDataGroup.children.get(2);
		assertSame(addedChild3, link3Spy);
	}

	private String getLinksXml() {
		String out = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		out += "<trams>";
		out += " <link1>";
		out += " <linkedRecordType>recordType</linkedRecordType>";
		out += " <linkedRecordId>place</linkedRecordId>";
		out += " </link1>";
		out += " <link2 repeatId=\"33\">";
		out += " <linkedRecordType>user</linkedRecordType>";
		out += " <linkedRecordId>test</linkedRecordId>";
		out += " </link2>";
		out += " <link3 type=\"someAttribute\">";
		out += " <linkedRecordType>user</linkedRecordType>";
		out += " <linkedRecordId>test</linkedRecordId>";
		out += " </link3>";
		out += "</trams>";
		return out;
	}

	@Test
	public void testLinksAreFactoredCorrectly() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = getPlaceXml();
		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 4);
		assertCorrectLink(0, "type", "recordType", "place");
		assertCorrectLink(1, "createdBy", "user", "test");
		assertCorrectLink(2, "dataDivider", "system", "alvin");
		assertCorrectLink(3, "updatedBy", "user", "test");

		assertEquals(dataGroupFactorySpy.usedNameInDatas.size(), 6);
		int numOfNotInlcudingAtomicsInLinks = 9;
		assertEquals(dataAtomicFactorySpy.usedNameInDatas.size(), numOfNotInlcudingAtomicsInLinks);

	}

	private void assertCorrectLink(int index, String nameInData, String type, String id) {
		dataFactorySpy.MCR.assertParameters("factorRecordLinkUsingNameInDataAndTypeAndId", index,
				nameInData, type, id);
	}

	@Test
	public void noLinkedRecordType() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<authority type=\"place\">" + " <recordInfo>" + " <id>alvin-place:22</id>"
				+ " <type>" + " <NOTlinkedRecordType>recordType</NOTlinkedRecordType>"
				+ " <linkedRecordId>place</linkedRecordId>" + " </type>" + " <dataDivider>"
				+ " <linkedRecordType>system</linkedRecordType>"
				+ " <linkedRecordId>alvin</linkedRecordId>" + " </dataDivider>" + " </recordInfo>"
				+ " <name type=\"authorized\">" + " <namePart>Linköping</namePart>" + " </name>"
				+ " <identifier repeatId=\"0\">" + " <identifierValue>114</identifierValue>"
				+ " </identifier>" + "</authority>";

		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 1);
	}

	@Test
	public void noLinkedRecordId() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<authority type=\"place\">" + " <recordInfo>" + " <id>alvin-place:22</id>"
				+ " <type>" + " <linkedRecordType>recordType</linkedRecordType>"
				+ " <NOTlinkedRecordId>place</NOTlinkedRecordId>" + " </type>" + " <dataDivider>"
				+ " <linkedRecordType>system</linkedRecordType>"
				+ " <linkedRecordId>alvin</linkedRecordId>" + " </dataDivider>" + " </recordInfo>"
				+ " <name type=\"authorized\">" + " <namePart>Linköping</namePart>" + " </name>"
				+ " <identifier repeatId=\"0\">" + " <identifierValue>114</identifierValue>"
				+ " </identifier>" + "</authority>";
		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);
		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 1);
	}
}
