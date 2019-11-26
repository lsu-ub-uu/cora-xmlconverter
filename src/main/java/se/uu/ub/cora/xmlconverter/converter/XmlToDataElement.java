package se.uu.ub.cora.xmlconverter.converter;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

	private DocumentBuilderFactory documentBuilderFactory;

	public XmlToDataElement(DocumentBuilderFactory documentBuilderFactory) {
		this.documentBuilderFactory = documentBuilderFactory;
	}

	public DataElement convert(String dataString) {
		DataGroup convertedDataElement = null;
		try {
			Element domElement = initializeDomElement(dataString);
			convertedDataElement = createTopDataGroup(domElement);
			NodeList childNodes = domElement.getChildNodes();

			convertChildren(convertedDataElement, childNodes);

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
		return DataGroupProvider.getDataGroupUsingNameInData(nodeName);
	}

	private void convertChildren(DataGroup parentElement, NodeList domChildren) {
		for (int i = 0; i < domChildren.getLength(); i++) {
			Node currentNode = domChildren.item(i);
			convertChild(parentElement, currentNode);
		}
	}

	private void convertChild(DataGroup parentDataGroup, Node currentNode) {
		if (hasChildren(currentNode)) {
			convertDataGroup(parentDataGroup, currentNode);
		} else {
			convertDataAtomic(parentDataGroup, currentNode);
		}
	}

	private boolean hasChildren(Node currentNode) {
		if (currentNode.getFirstChild() == null) {
			throw new XmlConverterException("Unable to convert from xml to dataElement due to "
					+ "NULL value on AtomicGroup " + currentNode.getNodeName());
		}
		return currentNode.getFirstChild().getNodeType() == Node.ELEMENT_NODE;
	}

	private void convertDataAtomic(DataGroup parentDataGroup, Node currentNode) {
		String nodeName = currentNode.getNodeName();
		String textContent = currentNode.getTextContent();
		DataAtomic dataAtomicUsingNameInDataAndValue = DataAtomicProvider
				.getDataAtomicUsingNameInDataAndValue(nodeName, textContent);
		parentDataGroup.addChild(dataAtomicUsingNameInDataAndValue);
	}

	private void convertDataGroup(DataGroup parentDataGroup, Node currentNode) {
		String nodeName = currentNode.getNodeName();
		DataGroup dataGroup = DataGroupProvider.getDataGroupUsingNameInData(nodeName);
		convertChildren(dataGroup, currentNode.getChildNodes());
		parentDataGroup.addChild(dataGroup);
	}

}
