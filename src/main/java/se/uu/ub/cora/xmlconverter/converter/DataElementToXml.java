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

import java.io.StringWriter;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import se.uu.ub.cora.converter.DataElementToStringConverter;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class DataElementToXml implements DataElementToStringConverter {

	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;

	public DataElementToXml(DocumentBuilderFactory documentBuildeFactory,
			TransformerFactory transformerFactory) {
		this.documentBuilderFactory = documentBuildeFactory;
		this.transformerFactory = transformerFactory;
	}

	@Override
	public String convert(DataElement dataElement) {
		return tryToCreateAndTransformDomDocumentToString(dataElement);
	}

	private String tryToCreateAndTransformDomDocumentToString(DataElement dataElement) {
		DataGroup topDataGroup = (DataGroup) dataElement;
		try {
			Document domDocument = createDomDocument(topDataGroup);
			return transformDomDocumentToString(domDocument, transformerFactory);
		} catch (ParserConfigurationException exception) {
			throw new XmlConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private Document createDomDocument(DataGroup dataGroupToConvert)
			throws ParserConfigurationException {
		Document domDocument = initializeDomDocument();
		return generateDomDocumentFromDataGroup(dataGroupToConvert, domDocument);
	}

	private Document initializeDomDocument() throws ParserConfigurationException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document domDocument = builder.newDocument();
		domDocument.setXmlStandalone(true);
		return domDocument;
	}

	private Document generateDomDocumentFromDataGroup(DataGroup dataGroupToConvert,
			Document domDocument) {
		generateRootElement(dataGroupToConvert, domDocument);
		iterateAndGenerateChildElements(dataGroupToConvert, domDocument,
				domDocument.getDocumentElement());
		return domDocument;
	}

	private void generateRootElement(DataGroup dataGroupToConvert, Document domDocument) {
		Element rootXmlDomElement = domDocument.createElement(dataGroupToConvert.getNameInData());
		domDocument.appendChild(rootXmlDomElement);
	}

	private void iterateAndGenerateChildElements(DataGroup dataGroup, Document domDocument,
			Element parentXmlDomElement) {
		for (DataElement childDataElement : dataGroup.getChildren()) {
			generateChildElement(domDocument, parentXmlDomElement, childDataElement);
		}
	}

	private void generateChildElement(Document domDocument, Element parentXmlDomElement,
			DataElement childDataElement) {
		Element domElement = createElement(domDocument, childDataElement);
		possiblyAddRepeatIdAsAttribute(childDataElement, domElement);
		populateChildElements(domDocument, childDataElement, domElement);
		parentXmlDomElement.appendChild(domElement);
	}

	private void populateChildElements(Document domDocument, DataElement childDataElement,
			Element domElement) {
		if (childDataElement instanceof DataAtomic) {
			possiblyAddTextToElementForDataAtomic((DataAtomic) childDataElement, domElement);
		} else {
			DataGroup childDataGroup = (DataGroup) childDataElement;
			addAttributesIfExistsToElementForDataGroup(childDataGroup, domElement);
			iterateAndGenerateChildElements(childDataGroup, domDocument, domElement);
		}
	}

	private Element createElement(Document domDocument, DataElement childDataElement) {
		return domDocument.createElement(childDataElement.getNameInData());
	}

	private void possiblyAddRepeatIdAsAttribute(DataElement childDataElement, Element domElement) {
		if (hasNonEmptyRepeatId(childDataElement)) {
			domElement.setAttribute("repeatId", childDataElement.getRepeatId());
		}
	}

	private boolean hasNonEmptyRepeatId(DataElement childDataElement) {
		return childDataElement.getRepeatId() != null && !childDataElement.getRepeatId().isEmpty();
	}

	private void possiblyAddTextToElementForDataAtomic(DataAtomic childDataAtomic,
			Element domElement) {
		domElement.setTextContent(childDataAtomic.getValue());
	}

	private void addAttributesIfExistsToElementForDataGroup(DataGroup childDataGroup,
			Element domElement) {
		Collection<DataAttribute> attributes = childDataGroup.getAttributes();

		for (DataAttribute attribute : attributes) {
			domElement.setAttribute(attribute.getNameInData(), attribute.getValue());
		}
	}

	private static String transformDomDocumentToString(Document domDocument,
			TransformerFactory transformerFactory) {
		try {
			return tryToTransformDomDocumentToString(domDocument, transformerFactory);
		} catch (TransformerException exception) {
			throw new XmlConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private static String tryToTransformDomDocumentToString(Document domDocument,
			TransformerFactory transformerFactory) throws TransformerException {
		DOMSource domSource = new DOMSource(domDocument);
		StringWriter xmlWriter = new StringWriter();
		StreamResult xmlResult = new StreamResult(xmlWriter);

		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(domSource, xmlResult);

		return xmlWriter.toString();
	}

	@Override
	public String convertWithLinks(DataElement dataElement, String baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	public DocumentBuilderFactory getDocumentBuilderFactoryOnlyForTest() {
		return documentBuilderFactory;
	}

	public TransformerFactory getTransformerFactoryOnlyForTest() {
		return transformerFactory;
	}

}
