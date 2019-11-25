package se.uu.ub.cora.xmlconverter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
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

	// TODO: Vi tar den senare
	// @Test(expectedExceptions = XmlConverterException.class, expectedExceptionsMessageRegExp = ""
	// + "Unable to convert from dataElement to xml")
	// public void testConvertXmlWithSingleAtomicChildWithoutValue() {
	// String xmlToconvert = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	// + "<person><firstname></firstname></person>";
	//
	// XmlConverter xmlConverter = new XmlConverter();
	// assertTrue(false);
	// }
}
