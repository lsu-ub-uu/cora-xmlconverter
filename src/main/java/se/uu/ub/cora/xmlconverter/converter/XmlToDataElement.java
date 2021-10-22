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
import java.util.ArrayList;
import java.util.List;
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
import se.uu.ub.cora.data.DataRecordLinkProvider;

public class XmlToDataElement {

	private static final int NUM_OF_LINK_CHILDREN = 2;
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
					"Unable to convert from xml to dataElement due to malformed XML: " + dataString,
					exception);
		} catch (Exception exception) {
			throw new XmlConverterException(
					"Unable to convert from xml to dataElement: " + exception.getMessage(),
					exception);
		}
	}

	private DataElement tryToConvert(String dataString)
			throws ParserConfigurationException, SAXException, IOException {
		Element domElement = generateDomElement(dataString);
		validateXmlHeader(dataString);
		DataGroup convertedDataElement = createTopDataGroup(domElement);
		List<Node> elementNodeChildren = getChildren(domElement);
		convertChildren(convertedDataElement, elementNodeChildren);
		return convertedDataElement;
	}

	private Element generateDomElement(String dataString)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document domDocument = documentBuilder.parse(new InputSource(new StringReader(dataString)));
		return domDocument.getDocumentElement();
	}

	private void validateXmlHeader(String dataString) {
		if (!dataString.startsWith(XML_HEADER)) {
			throw new XmlConverterException("Document must be: version 1.0 and UTF-8");
		}
	}

	private DataGroup createTopDataGroup(Element domElement) {
		String nodeName = domElement.getNodeName();
		DataGroup topDataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);
		XmlAttributes xmlAttributes = extractAttributesAndRepeatId(domElement);
		ensureNoRepeatId(xmlAttributes.repeatId);
		addAttributes(topDataGroup, xmlAttributes);
		return topDataGroup;
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

	private void ensureNoRepeatId(String repeatId) {
		if (!repeatId.isBlank()) {
			throw new XmlConverterException("Top dataGroup can not have repeatId");
		}
	}

	private void addAttributes(DataGroup dataGroup, XmlAttributes xmlAttributes) {
		for (Entry<String, String> attribute : xmlAttributes.getAttributeSet()) {
			dataGroup.addAttributeByIdWithValue(attribute.getKey(), attribute.getValue());
		}
	}

	private List<Node> getChildren(Node currentNode) {
		List<Node> elementNodes = new ArrayList<>();
		if (doesHaveNodeChilds(currentNode)) {
			itarateAndCollectElementNodesOnly(currentNode, elementNodes);
		}
		return elementNodes;
	}

	private boolean doesHaveNodeChilds(Node currentNode) {
		return currentNode.getFirstChild() != null;
	}

	private void itarateAndCollectElementNodesOnly(Node currentNode, List<Node> elementNodes) {
		NodeList childNodes = currentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			possiblyAddNodeIfElementNode(elementNodes, childNode);
		}
	}

	private void possiblyAddNodeIfElementNode(List<Node> elementNodes, Node childNode) {
		if (isElementNode(childNode)) {
			elementNodes.add(childNode);
		}
	}

	private boolean isElementNode(Node childNode) {
		return childNode.getNodeType() == Node.ELEMENT_NODE;
	}

	private void convertChildren(DataGroup parentElement, List<Node> elementNodeChildren) {
		if (elementNodeChildren.isEmpty()) {
			throw new XmlConverterException("Root element must be a DataGroup");
		}
		for (Node element : elementNodeChildren) {
			convertChild(parentElement, element);
		}
	}

	private void convertChild(DataGroup parentDataGroup, Node currentNode) {
		XmlAttributes xmlAttributes = extractAttributesAndRepeatId(currentNode);
		List<Node> elementNodeChildren = getChildren(currentNode);
		if (!elementNodeChildren.isEmpty()) {
			convertNodeWithChildren(parentDataGroup, currentNode, xmlAttributes,
					elementNodeChildren);
		} else {
			convertDataAtomic(parentDataGroup, currentNode, xmlAttributes);
		}
	}

	private void convertNodeWithChildren(DataGroup parentDataGroup, Node currentNode,
			XmlAttributes xmlAttributes, List<Node> elementNodeChildren) {
		if (isLink(elementNodeChildren)) {
			convertLink(parentDataGroup, currentNode, xmlAttributes, elementNodeChildren);
		} else {
			convertDataGroup(parentDataGroup, currentNode, xmlAttributes, elementNodeChildren);
		}
	}

	private boolean isLink(List<Node> elementNodeChildren) {
		if (elementNodeChildren.size() == NUM_OF_LINK_CHILDREN) {
			List<String> nodeNames = extractNodeNames(elementNodeChildren);
			if (nodeNamesContainsLinkChildren(nodeNames)) {
				return true;
			}
		}
		return false;
	}

	private List<String> extractNodeNames(List<Node> elementNodeChildren) {
		List<String> nodeNames = new ArrayList<>(elementNodeChildren.size());
		for (Node childNode : elementNodeChildren) {
			nodeNames.add(childNode.getNodeName());
		}
		return nodeNames;
	}

	private boolean nodeNamesContainsLinkChildren(List<String> nodeNames) {
		return nodeNames.contains("linkedRecordType") && nodeNames.contains("linkedRecordId");
	}

	private void convertLink(DataGroup parentDataGroup, Node currentNode,
			XmlAttributes xmlAttributes, List<Node> elementNodeChildren) {
		String nodeName = currentNode.getNodeName();
		DataGroup dataRecordLink = createLink(elementNodeChildren, nodeName);
		possiblyAddAttributesAndRepeatId(dataRecordLink, xmlAttributes);
		parentDataGroup.addChild(dataRecordLink);
	}

	private void possiblyAddAttributesAndRepeatId(DataGroup dataRecordLink,
			XmlAttributes xmlAttributes) {
		addAttributes(dataRecordLink, xmlAttributes);
		possiblyAddRepeatId(dataRecordLink, xmlAttributes);
	}

	private DataGroup createLink(List<Node> elementNodeChildren, String nodeName) {
		String linkedRecordType = getTextContentForNodeName(elementNodeChildren,
				"linkedRecordType");
		String linkedRecordId = getTextContentForNodeName(elementNodeChildren, "linkedRecordId");

		return DataRecordLinkProvider.getDataRecordLinkAsLinkUsingNameInDataTypeAndId(nodeName,
				linkedRecordType, linkedRecordId);
	}

	private String getTextContentForNodeName(List<Node> elementNodeChildren, String nodeName) {
		String valueToReturn = "";
		for (Node childNode : elementNodeChildren) {
			if (childNode.getNodeName().equals(nodeName)) {
				valueToReturn = childNode.getTextContent().trim();
			}
		}
		return valueToReturn;
	}

	private void convertDataGroup(DataGroup parentDataGroup, Node currentNode,
			XmlAttributes xmlAttributes, List<Node> elementNodeChildren) {
		String nodeName = currentNode.getNodeName();
		DataGroup dataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);
		possiblyAddAttributesAndRepeatId(dataGroup, xmlAttributes);
		convertChildren(dataGroup, elementNodeChildren);
		parentDataGroup.addChild(dataGroup);
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
		String textContent = currentNode.getTextContent().trim();
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
