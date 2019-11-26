package se.uu.ub.cora.xmlconverter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.xmlconverter.converter.XmlConverterException;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicFactorySpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupFactorySpy;

public class XmlConverterXmlToDataElementTest {

	DataGroupFactorySpy dataGroupFactorySpy = null;
	DataAtomicFactorySpy dataAtomicFactorySpy = null;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);

		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
	}

	@Test
	public void testConvertSimpleXmlWithOneRootElement() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person></person>";
		XmlConverter xmlConverter = new XmlConverter();
		DataElement convertedDataElement = xmlConverter.convert(xmlToconvert);

		assertEquals(convertedDataElement.getNameInData(), "person");
	}

	@Test
	public void testConvertXmlWithSingleAtomicChild() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname>Kalle</firstname></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);

		assertEquals(convertedDataElement.getFirstAtomicValueWithNameInData("firstname"), "Kalle");

	}

	@Test
	public void testConvertXmlWithMultipleDataGroupAndAtomicChild() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name><firstname>Kalle</firstname></name></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);
		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getFirstAtomicValueWithNameInData("firstname"), "Kalle");

	}

	@Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to convert from xml to dataElement due to NULL value on AtomicGroup firstname")
	public void testConvertXmlWithSingleAtomicChildWithoutValue() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><firstname></firstname></person>";

		XmlConverter xmlConverter = new XmlConverter();
		xmlConverter.convert(xmlToconvert);
	}

	// TODO: test throw Exception om attributes finns för DataAtomic
	// TODO: Test trhow Exception if incomming xml document does not have encoding=UTF-8
	// TODO: Test throw Excpetion if incomming xml document does not have xml version = 1.0
	// TODO: Test malformed XML

	@Test
	public void testAttributesAddedOnlyToDataGroup() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authenticated\"><firstname>Janne</firstname></name></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type"),
				"authenticated");

	}

	@Test
	public void testAttributesRepeatId() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authenticated\" repeatId=\"1\"><firstname repeatId=\"2\">Janne</firstname></name></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);

		DataGroup firstDataGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(firstDataGroup.getRepeatId(), "1");

		DataAtomic dataAtomic = (DataAtomic) firstDataGroup
				.getFirstChildWithNameInData("firstname");

		assertEquals(dataAtomic.getRepeatId(), "2");
	}

	@Test
	public void testMultipleAttributes() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person><name type=\"authenticated\" multiple=\"yes\" repeatId=\"1\"><firstname repeatId=\"2\">Janne</firstname></name></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);
		assertEquals(convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("type"),
				"authenticated");
		assertEquals(
				convertedDataElement.getFirstGroupWithNameInData("name").getAttribute("multiple"),
				"yes");
	}

	@Test
	public void testAttributesOnParentGroup() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<person gender=\"man\"><firstname>Janne</firstname></person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);
		assertEquals(convertedDataElement.getAttribute("gender"), "man");

	}

	@Test
	public void testcompleteExample() {
		String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<person>"
				+ "<name type=\"authenticated\" multiple=\"yes\" repeatId=\"1\">"
				+ "<firstname repeatId=\"2\">Janne</firstname>"
				+ "<secondname repeatId=\"3\">Fonda</secondname>"
				+ "<nickname><short>Fondis</short></nickname>" + "</name>"
				+ "<shoesize>14</shoesize>" + "</person>";

		XmlConverter xmlConverter = new XmlConverter();
		DataGroup convertedDataElement = (DataGroup) xmlConverter.convert(xmlToconvert);
		assertEquals(convertedDataElement.getNameInData(), "person");
		DataGroup nameGroup = convertedDataElement.getFirstGroupWithNameInData("name");
		assertEquals(nameGroup.getAttribute("type"), "authenticated");
		assertEquals(nameGroup.getAttribute("multiple"), "yes");
		DataAtomic secondnameAtomic = (DataAtomic) nameGroup
				.getFirstChildWithNameInData("secondname");
		assertEquals(secondnameAtomic.getRepeatId(), "3");
		assertEquals(secondnameAtomic.getValue(), "Fonda");
		assertEquals(convertedDataElement.getFirstChildWithNameInData("shoesize").getRepeatId(),
				null);
		DataGroup nicknameGroup = nameGroup.getFirstGroupWithNameInData("nickname");
		assertEquals(nicknameGroup.getFirstAtomicValueWithNameInData("short"), "Fondis");
	}

}
