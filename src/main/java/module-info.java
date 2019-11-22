module se.uu.ub.cora.xmlconverter {
	requires se.uu.ub.cora.converter;
	requires transitive java.xml;

	provides se.uu.ub.cora.converter.ConverterFactory
			with se.uu.ub.cora.xmlconverter.XmlConverterFactory;

}