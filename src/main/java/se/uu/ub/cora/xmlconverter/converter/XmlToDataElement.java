package se.uu.ub.cora.xmlconverter.converter;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
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

	private static final String ATTRIBUTES = "attributes";
	private static final String REPEAT_IDS = "repeatIds";
	private static final String REPEAT_ID = "repeatId";
	private DocumentBuilderFactory documentBuilderFactory;

	public XmlToDataElement(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	public DataElement convert(String dataString) {
		DataGroup convertedDataElement = null;
		try {
			Element domElement = initializeDomElement(dataString);
			convertedDataElement = createTopDataGroup(domElement);
			convertChildren(convertedDataElement, domElement);

		} catch (XmlConverterException xmlConverterException) {
			throw xmlConverterException;
		} catch (SAXException exception) {
			throw new XmlConverterException(
					"Unable to convert from xml to dataElement due to malformed XML", exception);
		} catch (Exception exception) {
			throw new XmlConverterException("Unable to convert from xml to dataElement", exception);
		}
		return convertedDataElement;
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
		addAttributesTopDataGroup(domElement, topDataGroup);
		return topDataGroup;
	}

	private void addAttributesTopDataGroup(Element domElement, DataGroup topDataGroup) {
		Map<String, Map<String, String>> attributesAndRepeatId = extractAttributesAndRepeatId(
				domElement);
		Map<String, String> attributes = attributesAndRepeatId.get(ATTRIBUTES);
		addAttributes(topDataGroup, attributes);
	}

	private void convertChildren(DataGroup parentElement, Node domElement) {
		NodeList childNodes = domElement.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node currentNode = childNodes.item(i);
			convertChild(parentElement, currentNode);
		}
	}

	private void convertChild(DataGroup parentDataGroup, Node currentNode) {
		Map<String, Map<String, String>> attributesAndRepeatId = extractAttributesAndRepeatId(
				currentNode);
		Map<String, String> attributes = attributesAndRepeatId.get(ATTRIBUTES);
		Map<String, String> repeatIds = attributesAndRepeatId.get(REPEAT_IDS);
		if (hasChildren(currentNode)) {
			convertDataGroup(parentDataGroup, currentNode, attributes, repeatIds);
		} else {
			convertDataAtomic(parentDataGroup, currentNode, repeatIds);
		}
	}

	private boolean hasChildren(Node currentNode) {
		if (currentNode.getFirstChild() == null) {
			throw new XmlConverterException("Unable to convert from xml to dataElement due to "
					+ "NULL value on AtomicGroup " + currentNode.getNodeName());
		}
		return currentNode.getFirstChild().getNodeType() == Node.ELEMENT_NODE;
	}

	private void convertDataAtomic(DataGroup parentDataGroup, Node currentNode,
			Map<String, String> repeatIds) {
		String nodeName = currentNode.getNodeName();
		String textContent = currentNode.getTextContent();

		DataAtomic dataAtomic = DataAtomicProvider.getDataAtomicUsingNameInDataAndValue(nodeName,
				textContent);
		addRepeatId(dataAtomic, repeatIds);
		parentDataGroup.addChild(dataAtomic);
	}

	private void convertDataGroup(DataGroup parentDataGroup, Node currentNode,
			Map<String, String> attributes, Map<String, String> repeatIds) {
		String nodeName = currentNode.getNodeName();
		DataGroup dataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);

		addAttributes(dataGroup, attributes);
		addRepeatId(dataGroup, repeatIds);
		convertChildren(dataGroup, currentNode);
		parentDataGroup.addChild(dataGroup);
	}

	private void addRepeatId(DataElement dataElement, Map<String, String> repeatIdsMap) {
		if (repeatIdsMap.size() > 0) {
			String repeatIdValue = repeatIdsMap.get(REPEAT_ID);
			if (dataElement instanceof DataGroup) {
				((DataGroup) dataElement).setRepeatId(repeatIdValue);
			} else {
				((DataAtomic) dataElement).setRepeatId(repeatIdValue);
			}
		}
	}

	private void addAttributes(DataGroup dataGroup, Map<String, String> attributesMap) {
		for (Entry<String, String> attribute : attributesMap.entrySet()) {
			dataGroup.addAttributeByIdWithValue(attribute.getKey(), attribute.getValue());
		}
	}

	private Map<String, Map<String, String>> extractAttributesAndRepeatId(Node currentNode) {
		Map<String, Map<String, String>> attributeMap = new HashMap<>();
		Map<String, String> repeatIds = new HashMap<>();
		Map<String, String> attributes = new HashMap<>();

		NamedNodeMap domAttributes = currentNode.getAttributes();
		int domAttributesSize = domAttributes.getLength();
		for (int position = 0; position < domAttributesSize; position++) {
			possiblyExtractAttributesOrRepeatsIds(repeatIds, attributes, domAttributes, position);
		}
		attributeMap.put(REPEAT_IDS, repeatIds);
		attributeMap.put(ATTRIBUTES, attributes);
		return attributeMap;
	}

	private void possiblyExtractAttributesOrRepeatsIds(Map<String, String> repeatIds,
			Map<String, String> attributes, NamedNodeMap domAttributes, int position) {
		Node attribute = domAttributes.item(position);
		if (attribute.getNodeName().equals(REPEAT_ID)) {
			repeatIds.put(REPEAT_ID, attribute.getTextContent());
		} else {
			attributes.put(attribute.getNodeName(), attribute.getTextContent());
		}
	}
}
