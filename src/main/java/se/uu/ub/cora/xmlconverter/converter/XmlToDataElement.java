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

import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;

public class XmlToDataElement {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String REPEAT_ID = "repeatId";
	private DocumentBuilderFactory documentBuilderFactory;

	public XmlToDataElement(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	public DataElement convert(String dataString) {
		try {
			return tryToConvert(dataString);
		} catch (SAXException exception) {
			throw new XmlConverterException(
					"Unable to convert from xml to dataElement due to malformed XML", exception);
		} catch (Exception exception) {
			throw new XmlConverterException(
					"Unable to convert from xml to dataElement: " + exception.getMessage(),
					exception);
		}
	}

	private DataElement tryToConvert(String dataString)
			throws ParserConfigurationException, SAXException, IOException {
		Element domElement = initializeDomElement(dataString);
		ensureIncommingXmlHasExpectedHeader(dataString);
		DataGroup convertedDataElement = createTopDataGroup(domElement);
		convertChildren(convertedDataElement, domElement);
		return convertedDataElement;
	}

	private void ensureIncommingXmlHasExpectedHeader(String dataString) {
		if (!dataString.startsWith(XML_HEADER)) {
			throw new XmlConverterException("Document must be: version 1.0 and UTF-8");
		}
	}

	private Element initializeDomElement(String dataString)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document domDocument = documentBuilder.parse(new InputSource(new StringReader(dataString)));
		return domDocument.getDocumentElement();
	}

	private DataGroup createTopDataGroup(Element domElement) {
		String nodeName = domElement.getNodeName();
		DataGroup topDataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);
		XmlAttributes xmlAttributes = extractAttributesAndRepeatId(domElement);
		ensureNoRepeatId(xmlAttributes.repeatId);
		addAttributes(topDataGroup, xmlAttributes);
		return topDataGroup;
	}

	private void ensureNoRepeatId(String repeatId) {
		if (!repeatId.isBlank()) {
			throw new XmlConverterException("Top dataGroup can not have repeatId");
		}
	}

	private XmlAttributes extractAttributesAndRepeatId(Node currentNode) {
		XmlAttributes xmlAttributes = new XmlAttributes();
		NamedNodeMap domAttributes = currentNode.getAttributes();
		iterateAndExtractXmlAttributes(xmlAttributes, domAttributes);
		return xmlAttributes;
	}

	private void iterateAndExtractXmlAttributes(XmlAttributes xmlAttributes,
			NamedNodeMap domAttributes) {
		int domAttributesSize = domAttributes.getLength();
		for (int position = 0; position < domAttributesSize; position++) {
			Node domAttribute = domAttributes.item(position);
			possiblyExtractAttributesOrRepeatsIds(xmlAttributes, domAttribute);
		}
	}

	private void possiblyExtractAttributesOrRepeatsIds(XmlAttributes xmlattributes,
			Node domAttribute) {
		if (REPEAT_ID.equals(domAttribute.getNodeName())) {
			xmlattributes.repeatId = domAttribute.getTextContent();
		} else {
			xmlattributes.setAttribute(domAttribute.getNodeName(), domAttribute.getTextContent());
		}
	}

	private void convertChildren(DataGroup parentElement, Node domElement) {
		NodeList childNodes = domElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node currentNode = childNodes.item(i);
			convertChild(parentElement, currentNode);
		}
	}

	private void convertChild(DataGroup parentDataGroup, Node currentNode) {
		XmlAttributes xmlAttributes = extractAttributesAndRepeatId(currentNode);
		if (hasChildren(currentNode)) {
			convertDataGroup(parentDataGroup, currentNode, xmlAttributes);
		} else {
			convertDataAtomic(parentDataGroup, currentNode, xmlAttributes);
		}
	}

	private boolean hasChildren(Node currentNode) {
		if (currentNode.getFirstChild() == null) {
			return false;
		}
		return currentNode.getFirstChild().getNodeType() == Node.ELEMENT_NODE;
	}

	private void convertDataGroup(DataGroup parentDataGroup, Node currentNode,
			XmlAttributes xmlAttributes) {
		String nodeName = currentNode.getNodeName();
		DataGroup dataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);

		addAttributes(dataGroup, xmlAttributes);
		possiblyAddRepeatId(dataGroup, xmlAttributes);
		convertChildren(dataGroup, currentNode);
		parentDataGroup.addChild(dataGroup);
	}

	private void addAttributes(DataGroup dataGroup, XmlAttributes xmlAttributes) {
		for (Entry<String, String> attribute : xmlAttributes.getAttributeSet()) {
			dataGroup.addAttributeByIdWithValue(attribute.getKey(), attribute.getValue());
		}
	}

	private void possiblyAddRepeatId(DataElement dataElement, XmlAttributes xmlAttributes) {
		String repeatIdValue = xmlAttributes.repeatId;
		if (!repeatIdValue.isEmpty()) {
			addRepeatId(dataElement, repeatIdValue);
		}
	}

	private void addRepeatId(DataElement dataElement, String repeatIdValue) {
		if (dataElement instanceof DataGroup) {
			((DataGroup) dataElement).setRepeatId(repeatIdValue);
		} else {
			((DataAtomic) dataElement).setRepeatId(repeatIdValue);
		}
	}

	private void convertDataAtomic(DataGroup parentDataGroup, Node currentNode,
			XmlAttributes xmlAttributes) {
		ensureNoAttributes(xmlAttributes);
		String nodeName = currentNode.getNodeName();
		String textContent = currentNode.getTextContent();
		DataAtomic dataAtomic = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nodeName,
				textContent);
		possiblyAddRepeatId(dataAtomic, xmlAttributes);
		parentDataGroup.addChild(dataAtomic);

	}

	private void ensureNoAttributes(XmlAttributes xmlAttributes) {
		if (xmlAttributes.hasAttributes()) {
			throw new XmlConverterException("DataAtomic can not have attributes");
		}
	}

}
