/**
 * This module provides an implementation for the converter interface converting between XML and
 * {@link DataElement}.
 * 
 * This implementation maps XML and DataElement as it is described below: For each DataGroup a XML
 * tag is created where the tag name is the DataGroups nameInData. The DataGroups attributes are
 * added as XML attributes, to the DataGroups XML tag. If the DataGroup has a repeatId it is also
 * added as an attribute with the name repeatId to the groups XML tag.
 * 
 * Note that this implies that there can not exist an attribute with the name in data repeatId for
 * any DataElement.
 * 
 * Each DataGroup can have several children which can be either DataGroup or DataAtomic. The
 * DataAtomic is mapped to a XML with the tag name using nameIndata, and its value using the
 * DataAtomics value. Only one attribut is mapped from DataAtomic to an XML tag, which is the
 * repeatId.
 * 
 * This module converts both to and from XML.
 * 
 * @provides ConverterFactory the ConverterFactory interface which is implemented in this module.
 */
module se.uu.ub.cora.xmlconverter {
	requires se.uu.ub.cora.converter;
	requires transitive java.xml;
	requires se.uu.ub.cora.data;

	exports se.uu.ub.cora.xmlconverter.converter;

	provides se.uu.ub.cora.converter.ConverterFactory
			with se.uu.ub.cora.xmlconverter.XmlConverterFactory;

	// opens place;
	opens xslt;
}