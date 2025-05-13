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

import se.uu.ub.cora.converter.ConverterException;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataProvider;
import se.uu.ub.cora.data.ExternallyConvertible;
import se.uu.ub.cora.data.spies.DataAtomicSpy;
import se.uu.ub.cora.data.spies.DataFactorySpy;
import se.uu.ub.cora.data.spies.DataGroupSpy;
import se.uu.ub.cora.data.spies.DataRecordLinkSpy;
import se.uu.ub.cora.xmlconverter.spy.DocumentBuilderFactorySpy;

public class XmlToExternallyConvertibleTest {
	DataFactorySpy dataFactorySpy;

	private DocumentBuilderFactory documentBuilderFactory;
	private XmlToExternallyConvertible xmlToDataElement;

	@BeforeMethod
	public void setUp() {
		dataFactorySpy = new DataFactorySpy();
		DataProvider.onlyForTestSetDataFactory(dataFactorySpy);

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		xmlToDataElement = new XmlToExternallyConvertible(documentBuilderFactory);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Document must be: version 1.0 and UTF-8")
	public void testParseExceptionWhenNotCorrectVerisonAndEncoding() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="notUTF-8"?>
				<person><firstname/></person>""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: some message from DocumentBuilderFactorySpy")
	public void testParseExceptionOnCreateDocument() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>" + "<person></person>""";

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
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?><person></person>""";
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
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
				<person></person>""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithSpace() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
				<person></person>""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithTwoSpace() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
				<person></person>""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Root element must be a DataGroup")
	public void testIncompleteRootElementWithText() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
				<person> dummy text </person>""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testConvertXmlWithSingleAtomicChildWithSpace() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(" <firstname>Kalle</firstname> ");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		var firstName = dataFactorySpy.MCR.assertCalledParametersReturn(
				"factorAtomicUsingNameInDataAndValue", "firstname", "Kalle");

		convertedDataElement.MCR.assertCalledParameters("addChild", firstName);
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

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		var factoredAtomic = dataFactorySpy.MCR.assertCalledParametersReturn(
				"factorAtomicUsingNameInDataAndValue", "firstname", convertedValue);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredAtomic);
	}

	@Test
	public void testConvertXmlWithAttribute() {
		String atomicXml = """
				<firstname someAttribute="attrib" someAttribute2="attrib2">Kalle</firstname>
				""";
		String xmlToConvert = surroundWithTopLevelXmlGroup(atomicXml);

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Kalle");
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredAtomic);
		factoredAtomic.MCR.assertNumberOfCallsToMethod("addAttributeByIdWithValue", 2);
		factoredAtomic.MCR.assertCalledParameters("addAttributeByIdWithValue", "someAttribute",
				"attrib");
		factoredAtomic.MCR.assertCalledParameters("addAttributeByIdWithValue", "someAttribute2",
				"attrib2");
	}

	private String surroundWithTopLevelXmlGroup(String atomicXml) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
					<person>%s</person>
				""".formatted(atomicXml);
	}

	@Test
	public void testConvertXmlWithMultipleDataGroupAndAtomicChild() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name><firstname>Kalle</firstname></name>");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Kalle");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);

	}

	@Test
	public void testConvertXmlWithMultipleDataGroupAndSpaceAroundAtomicChild() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name> <firstname>Kalle</firstname> </name>");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Kalle");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);
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
		String xmlToConvert = surroundWithTopLevelXmlGroup("""
				<name type="authenticated">
					<firstname>Janne</firstname>
				</name>
				""");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "type",
				"authenticated");

		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Janne");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);

	}

	@Test
	public void testWithRunicCharacters() {
		String xmlToConvert = surroundWithTopLevelXmlGroup(
				"<name><firstname>ᚠᚢᚦᚮᚱᚴ</firstname></name>");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");

		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"ᚠᚢᚦᚮᚱᚴ");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);
	}

	@Test
	public void testAttributesRepeatId() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("""
				<name type="authenticated" repeatId="1">
					<firstname repeatId="2">Janne</firstname>
				</name>
				""");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "type",
				"authenticated");
		factoredGroup.MCR.assertCalledParameters("setRepeatId", "1");
		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Janne");
		factoredAtomic.MCR.assertCalledParameters("setRepeatId", "2");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);
	}

	@Test
	public void testMultipleAttributes() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("""
				<name type="authenticated" multiple="yes" repeatId="1">
					<firstname repeatId="2">Janne</firstname>
				</name>
				""");
		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "type",
				"authenticated");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "multiple", "yes");
		factoredGroup.MCR.assertCalledParameters("setRepeatId", "1");
		DataAtomicSpy factoredAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Janne");
		factoredAtomic.MCR.assertCalledParameters("setRepeatId", "2");
		factoredGroup.MCR.assertParameters("addChild", 0, factoredAtomic);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);
	}

	@Test
	public void testAttributesOnParentGroup() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
					<person gender="man">
						<firstname>Janne</firstname>
					</person>
				""";

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);
		convertedDataElement.MCR.assertCalledParameters("addAttributeByIdWithValue", "gender",
				"man");

	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement: Top dataGroup can not have repeatId")
	public void testRepeatIdOnParentGroup() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
					<person repeatId="someRepeatId" gender="man">
						<firstname>Janne</firstname>
					</person>
				""";

		xmlToDataElement.convert(xmlToConvert);
	}

	@Test
	public void testCompleteExample() {
		String xmlToConvert = surroundWithTopLevelXmlGroup("""
				<name type="authenticated" multiple="yes" repeatId="1">
						<firstname repeatId="2">Janne</firstname>
						<secondname repeatId="3">Fonda</secondname>
						<nickname>
							<short>Fondis</short>
						</nickname>
				</name>
				<shoesize>14</shoesize>
				""");

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement.convert(xmlToConvert);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "name");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "type",
				"authenticated");
		factoredGroup.MCR.assertCalledParameters("addAttributeByIdWithValue", "multiple", "yes");
		factoredGroup.MCR.assertCalledParameters("setRepeatId", "1");
		DataAtomicSpy factoredAtomic0 = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "firstname",
						"Janne");
		factoredAtomic0.MCR.assertCalledParameters("setRepeatId", "2");
		DataAtomicSpy factoredAtomic1 = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "secondname",
						"Fonda");
		DataGroupSpy factoredNickNameGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "nickname");
		DataAtomicSpy factoredShortAtomic = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue", "short",
						"Fondis");
		factoredNickNameGroup.MCR.assertCalledParameters("addChild", factoredShortAtomic);
		factoredAtomic1.MCR.assertCalledParameters("setRepeatId", "3");
		factoredGroup.MCR.assertCalledParameters("addChild", factoredAtomic0);
		factoredGroup.MCR.assertCalledParameters("addChild", factoredAtomic1);
		factoredGroup.MCR.assertCalledParameters("addChild", factoredNickNameGroup);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);

	}

	@Test
	public void testWithMultipleXmlRootElements() {
		String xmlToConvert = """
				<?xml version="1.0" encoding="UTF-8"?>
				<person gender="man">
					<firstname>Janne</firstname>
				</person>
				<person gender="man">
					<firstname>John</firstname>
				</person>""";
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
		String xmlFromXsltAlvinFedoraToCoraPlaces = """
				<?xml version="1.0" encoding="UTF-8"?>
				<authority type="place">
					<recordInfo>
						<id>alvin-place:22</id>
						<type>
							<linkedRecordType>recordType</linkedRecordType>
							<linkedRecordId>place</linkedRecordId>
						</type>
						<createdBy>
							<linkedRecordType>user</linkedRecordType>
							<linkedRecordId>test</linkedRecordId>
						</createdBy>
						<tsCreated>2014-12-18 20:20:38.346 UTC</tsCreated>
						<dataDivider>
							<linkedRecordType>system</linkedRecordType>
							<linkedRecordId>alvin</linkedRecordId>
						</dataDivider>
						<updated repeatId="0">
							<updatedBy>
								<linkedRecordType>user</linkedRecordType>
								<linkedRecordId>test</linkedRecordId>
							</updatedBy>
							<tsUpdated>2014-12-18 20:21:20.880 UTC</tsUpdated>
						</updated>
					</recordInfo>
					<name type="authorized">
						<namePart>Linköping</namePart>
					</name>
					<coordinates>
						<latitude>58.42</latitude>
						<longitude>15.62</longitude>
					</coordinates>
					<country>SE</country>
					<identifier repeatId="0">
						<identifierType>waller</identifierType>
						<identifierValue>114</identifierValue>
					</identifier>
				</authority>
				""";

		DataGroupSpy convertedDataElement = (DataGroupSpy) xmlToDataElement
				.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		DataGroupSpy factoredGroup = (DataGroupSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorGroupUsingNameInData", "identifier");
		factoredGroup.MCR.assertCalledParameters("setRepeatId", "0");
		DataAtomicSpy factoredAtomic0 = (DataAtomicSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorAtomicUsingNameInDataAndValue",
						"identifierType", "waller");
		factoredGroup.MCR.assertCalledParameters("addChild", factoredAtomic0);
		convertedDataElement.MCR.assertCalledParameters("addChild", factoredGroup);
	}

	@Test
	public void testXmlWithLinks() throws Exception {
		String dataGroupWithLinks = """
				<?xml version="1.0" encoding="UTF-8"?>
				<trams>
					<link1>
						<linkedRecordType>recordType</linkedRecordType>
						<linkedRecordId>place</linkedRecordId>
					</link1>
					<link2 repeatId="33">
						<linkedRecordType>user</linkedRecordType>
						<linkedRecordId>test</linkedRecordId>
					</link2>
					<link3 type="someAttribute">
						<linkedRecordType>user</linkedRecordType>
						<linkedRecordId>test</linkedRecordId>
					</link3>
				</trams>
				""";

		DataGroupSpy topDataGroup = (DataGroupSpy) xmlToDataElement.convert(dataGroupWithLinks);

		DataRecordLinkSpy link1Spy = (DataRecordLinkSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorRecordLinkUsingNameInDataAndTypeAndId",
						"link1", "recordType", "place");

		DataRecordLinkSpy link2Spy = (DataRecordLinkSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorRecordLinkUsingNameInDataAndTypeAndId",
						"link2", "user", "test");
		link2Spy.MCR.assertParameters("setRepeatId", 0, "33");

		DataRecordLinkSpy link3Spy = (DataRecordLinkSpy) dataFactorySpy.MCR
				.assertCalledParametersReturn("factorRecordLinkUsingNameInDataAndTypeAndId",
						"link3", "user", "test");
		link3Spy.MCR.assertParameters("addAttributeByIdWithValue", 0, "type", "someAttribute");

		topDataGroup.MCR.assertCalledParameters("addChild", link1Spy);
		topDataGroup.MCR.assertCalledParameters("addChild", link2Spy);
		topDataGroup.MCR.assertCalledParameters("addChild", link3Spy);
	}

	@Test
	public void testLinksAreFactoredCorrectly() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = """
				<?xml version="1.0" encoding="UTF-8"?>
				<authority type="place">
					<recordInfo>
						<id>alvin-place:22</id>
						<type>
							<linkedRecordType>recordType</linkedRecordType>
							<linkedRecordId>place</linkedRecordId>
						</type>
						<createdBy>
							<linkedRecordType>user</linkedRecordType>
							<linkedRecordId>test</linkedRecordId>
						</createdBy>
						<tsCreated>2014-12-18 20:20:38.346 UTC</tsCreated>
						<dataDivider>
							<linkedRecordType>system</linkedRecordType>
							<linkedRecordId>alvin</linkedRecordId>
						</dataDivider>
						<updated repeatId="0">
							<updatedBy>
								<linkedRecordType>user</linkedRecordType>
								<linkedRecordId>test</linkedRecordId>
							</updatedBy>
							<tsUpdated>2014-12-18 20:21:20.880 UTC</tsUpdated>
						</updated>
					</recordInfo>
					<name type="authorized">
						<namePart>Linköping</namePart>
					</name>
					<coordinates>
						<latitude>58.42</latitude>
						<longitude>15.62</longitude>
					</coordinates>
					<country>SE</country>
					<identifier repeatId="0">
						<identifierType>waller</identifierType>
						<identifierValue>114</identifierValue>
					</identifier>
				</authority>
				""";

		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		dataFactorySpy.MCR.assertNumberOfCallsToMethod("factorGroupUsingNameInData", 6);
		dataFactorySpy.MCR.assertNumberOfCallsToMethod("factorAtomicUsingNameInDataAndValue", 9);
		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 4);
		dataFactorySpy.MCR.assertParameters("factorRecordLinkUsingNameInDataAndTypeAndId", 0,
				"type", "recordType", "place");
		dataFactorySpy.MCR.assertParameters("factorRecordLinkUsingNameInDataAndTypeAndId", 1,
				"createdBy", "user", "test");
		dataFactorySpy.MCR.assertParameters("factorRecordLinkUsingNameInDataAndTypeAndId", 2,
				"dataDivider", "system", "alvin");
		dataFactorySpy.MCR.assertParameters("factorRecordLinkUsingNameInDataAndTypeAndId", 3,
				"updatedBy", "user", "test");

	}

	@Test
	public void noLinkedRecordType() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = """
				<?xml version="1.0" encoding="UTF-8"?>
				<authority type="place">
					<recordInfo>
						<id>alvin-place:22</id>
				 		<type>
				 			<NOTlinkedRecordType>recordType</NOTlinkedRecordType>
				 			<linkedRecordId>place</linkedRecordId>
				 		</type>
				 		<dataDivider>
				 			<linkedRecordType>system</linkedRecordType>
				 			<linkedRecordId>alvin</linkedRecordId>
				 		</dataDivider>
				 	</recordInfo>
				 	<name type="authorized">
				 		<namePart>Linköping</namePart>
				 	</name>
				 	<identifier repeatId="0">
				 		<identifierValue>114</identifierValue>
				 	</identifier>
				</authority>""";

		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 1);
	}

	@Test
	public void noLinkedRecordId() {
		String xmlFromXsltAlvinFedoraToCoraPlaces = """
				<?xml version="1.0" encoding="UTF-8"?>
				<authority type="place">
					<recordInfo>
						<id>alvin-place:22</id>
				 		<type>
				 			<linkedRecordType>recordType</linkedRecordType>
				 			<NOTlinkedRecordId>place</NOTlinkedRecordId>
				 		</type>
				 		<dataDivider>
				 			<linkedRecordType>system</linkedRecordType>
				 			<linkedRecordId>alvin</linkedRecordId>
				 		</dataDivider>
				 	</recordInfo>
				 	<name type="authorized">
				 		<namePart>Linköping</namePart>
				 	</name>
				 	<identifier repeatId="0">
				 		<identifierValue>114</identifierValue>
				 	</identifier>
				 </authority>""";

		xmlToDataElement.convert(xmlFromXsltAlvinFedoraToCoraPlaces);

		dataFactorySpy.MCR
				.assertNumberOfCallsToMethod("factorRecordLinkUsingNameInDataAndTypeAndId", 1);
	}

	@Test
	public void testRemoveActionLinksFromRecordLink() {
		String xmlWithActionLinks = """
				<?xml version="1.0" encoding="UTF-8"?>
				<book>
				    <recordInfo>
				        <id>asdf</id>
				        <type>
				            <linkedRecordType>recordType</linkedRecordType>
				            <linkedRecordId>demo</linkedRecordId>
				            <actionLinks>
				                <read>
				                    <requestMethod>GET</requestMethod>
				                    <rel>read</rel>
				                    <url>http://localhost:38080/systemone/rest/record/recordType/demo</url>
				                    <accept>application/vnd.cora.record+xml</accept>
				                </read>
				            </actionLinks>
				        </type>
				    </recordInfo>
				</book>
				 """;

		xmlToDataElement.convert(xmlWithActionLinks);
		dataFactorySpy.MCR.assertCalledParametersReturn(
				"factorRecordLinkUsingNameInDataAndTypeAndId", "type", "recordType", "demo");
	}

	@Test
	public void testRemoveActionLinksFromResourceLink() {
		String xmlWithActionLinks = """
				<?xml version="1.0" encoding="UTF-8"?>
				<medium>
				    <resourceId>binary:597846510460883-medium</resourceId>
				    <medium>
				        <actionLinks>
				            <read>
				                <requestMethod>GET</requestMethod>
				                <rel>read</rel>
				                <url>http://localhost:38080/systemone/rest/record/binary/binary:597846510460883/medium</url>
				                <accept>image/jpeg</accept>
				            </read>
				        </actionLinks>
				        <mimeType>image/jpeg</mimeType>
				    </medium>
				    <fileSize>9796</fileSize>
				    <mimeType>image/jpeg</mimeType>
				    <height>63</height>
				    <width>300</width>
				</medium>
				 """;

		xmlToDataElement.convert(xmlWithActionLinks);

	}
}
