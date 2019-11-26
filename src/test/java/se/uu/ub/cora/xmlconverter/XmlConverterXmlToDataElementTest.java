package se.uu.ub.cora.xmlconverter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
	// TODO: Test att attributes adderas som attribut BARA i DataGroup
	// TODO: Test att repeatId adderas båda i DataGroup och DataAtomic
	// TODO: Test xml med flera barn med djup minst två
	// TODO: test throw Exception om attributes finns för DataAtomic
	// TODO: Test trhow Exception if incomming xml document does not have encoding=UTF-8
	// TODO: Test throw Excpetion if incomming xml document does not have xml version = 1.0
	// TODO: Test to handle SAXException | IOException | ParserConfigurationException
	// TODO: Test malformed XML
	// TODO: Test empty XML
}
