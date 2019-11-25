module se.uu.ub.cora.xmlconverter {
	requires se.uu.ub.cora.converter;
	requires transitive java.xml;
	requires se.uu.ub.cora.data;

	provides se.uu.ub.cora.converter.ConverterFactory
			with se.uu.ub.cora.xmlconverter.XmlConverterFactory;

}