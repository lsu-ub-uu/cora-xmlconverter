/*
 * Copyright 2019, 2021 Uppsala University Library
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
import java.util.List;

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

import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.data.ExternallyConvertible;

public class ExternallyConvertibleToXml implements ExternallyConvertibleToStringConverter {

	private static final String APPLICATION_VND_UUB_RECORD_LIST_XML = "application/vnd.uub.recordList+xml";
	private static final String APPLICATION_VND_UUB_RECORD_XML = "application/vnd.uub.record+xml";
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Document domDocument;
	private String baseUrl;
	private boolean addActionLinks;

	public ExternallyConvertibleToXml(DocumentBuilderFactory documentBuildeFactory,
			TransformerFactory transformerFactory) {
		this.documentBuilderFactory = documentBuildeFactory;
		this.transformerFactory = transformerFactory;
	}

	@Override
	public String convert(ExternallyConvertible externallyConvertible) {
		addActionLinks = false;
		return tryToCreateAndTransformDomDocumentToString(externallyConvertible);
	}

	private String tryToCreateAndTransformDomDocumentToString(
			ExternallyConvertible externallyConvertible) {
		try {
			return createAndTransformDomDocumentToString(externallyConvertible);
		} catch (ParserConfigurationException exception) {
			throw new XmlConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private String createAndTransformDomDocumentToString(
			ExternallyConvertible externallyConvertible) throws ParserConfigurationException {
		domDocument = initializeDomDocument();
		if (externallyConvertible instanceof DataRecord dataRecord) {
			convertDataRecordToString(dataRecord);
		} else {
			convertDataGroupToString(externallyConvertible);
		}
		return transformDomDocumentToString(domDocument, transformerFactory);
	}

	private Document initializeDomDocument() throws ParserConfigurationException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document newDomDocument = builder.newDocument();
		newDomDocument.setXmlStandalone(true);
		return newDomDocument;
	}

	private void convertDataRecordToString(DataRecord dataRecord) {
		Element recordDomElement = domDocument.createElement("record");
		domDocument.appendChild(recordDomElement);

		Element dataDomElement = domDocument.createElement("data");
		recordDomElement.appendChild(dataDomElement);

		// topGroup
		DataGroup topDataGroup = dataRecord.getDataGroup();
		Element groupDomElement = generateDomDocumentFromDataGroup(topDataGroup);
		dataDomElement.appendChild(groupDomElement);

		if (addActionLinks && dataRecord.hasActions()) {
			List<Action> actions = dataRecord.getActions();

			Element actionLinks = domDocument.createElement("actionLinks");
			recordDomElement.appendChild(actionLinks);

			String linkedRecordType = dataRecord.getType();
			String linkedRecordId = dataRecord.getId();

			if (actions.contains(Action.READ)) {
				Element readLink = createReadLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(readLink);
			}
			if (actions.contains(Action.UPDATE)) {
				Element updateLink = createUpdateLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(updateLink);
			}
			if (actions.contains(Action.DELETE)) {
				Element deleteLink = createDeleteLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(deleteLink);
			}
			if (actions.contains(Action.READ_INCOMING_LINKS)) {
				Element readIncomingLink = createReadIncomingLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(readIncomingLink);
			}
			if (actions.contains(Action.INDEX)) {
				Element indexLink = createIndexLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(indexLink);
			}
			if (actions.contains(Action.UPLOAD)) {
				Element readIncomingLink = createUploadLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(readIncomingLink);
			}
			if (actions.contains(Action.SEARCH)) {
				Element readIncomingLink = createSearchLink(dataRecord.getSearchId());
				actionLinks.appendChild(readIncomingLink);
			}
			if (actions.contains(Action.CREATE)) {
				Element readIncomingLink = createCreateLink(linkedRecordId);
				actionLinks.appendChild(readIncomingLink);
			}
			if (actions.contains(Action.LIST)) {
				Element readIncomingLink = createListLink(linkedRecordId);
				actionLinks.appendChild(readIncomingLink);
			}
		}
	}

	private Element createListLink(String linkedRecordId) {
		Element searchLink = createStandardLink("GET", "list", linkedRecordId);
		searchLink.appendChild(createAcceptRecordListXML());
		return searchLink;
	}

	private Element createCreateLink(String linkedRecordId) {
		Element searchLink = createStandardLink("POST", "create", linkedRecordId);
		searchLink.appendChild(createContentTypeRecordXML());
		searchLink.appendChild(createAcceptRecordXML());
		return searchLink;
	}

	private Element createSearchLink(String searchId) {
		Element searchLink = createStandardLink("GET", "search", "searchResult", searchId);
		searchLink.appendChild(createAcceptRecordListXML());
		return searchLink;
	}

	private Element createUploadLink(String linkedRecordType, String linkedRecordId) {
		Element uploadLink = createStandardLink("POST", "upload", linkedRecordType, linkedRecordId,
				"master");
		uploadLink.appendChild(createElementWithTextContent("contentType", "multipart/form-data"));
		return uploadLink;
	}

	private Element createIndexLink(String linkedRecordType, String linkedRecordId) {
		Element indexLink = createStandardLink("POST", "index", "workOrder");
		indexLink.appendChild(createContentTypeRecordXML());
		indexLink.appendChild(createAcceptRecordXML());
		indexLink.appendChild(createWorkOrderXML(linkedRecordType, linkedRecordId));
		return indexLink;
	}

	private Element createWorkOrderXML(String linkedRecordType, String linkedRecordId) {
		Element body = domDocument.createElement("body");
		Element workOrder = domDocument.createElement("workOrder");
		body.appendChild(workOrder);
		Element recordType = domDocument.createElement("recordType");
		workOrder.appendChild(recordType);
		recordType.appendChild(createElementWithTextContent("linkedRecordType", "recordType"));
		recordType.appendChild(createElementWithTextContent("linkedRecordId", linkedRecordType));
		recordType.appendChild(createElementWithTextContent("recordId", linkedRecordId));
		recordType.appendChild(createElementWithTextContent("type", "index"));
		return body;
	}

	private Element createReadIncomingLink(String linkedRecordType, String linkedRecordId) {
		Element readIncomingLink = createStandardLink("GET", "read_incoming_links",
				linkedRecordType, linkedRecordId, "incomingLinks");
		readIncomingLink.appendChild(createAcceptRecordListXML());
		return readIncomingLink;
	}

	private Element createUpdateLink(String linkedRecordType, String linkedRecordId) {
		Element readLink = createStandardLink("POST", "update", linkedRecordType, linkedRecordId);
		readLink.appendChild(createContentTypeRecordXML());
		readLink.appendChild(createAcceptRecordXML());
		return readLink;
	}

	private Element createAcceptRecordXML() {
		return createElementWithTextContent("accept", APPLICATION_VND_UUB_RECORD_XML);
	}

	private Element createAcceptRecordListXML() {
		return createElementWithTextContent("accept", APPLICATION_VND_UUB_RECORD_LIST_XML);
	}

	private Element createContentTypeRecordXML() {
		return createElementWithTextContent("contentType", APPLICATION_VND_UUB_RECORD_XML);
	}

	private void convertDataGroupToString(ExternallyConvertible externallyConvertible) {
		DataGroup topDataGroup = (DataGroup) externallyConvertible;
		Element groupDomElement = generateDomDocumentFromDataGroup(topDataGroup);
		domDocument.appendChild(groupDomElement);
	}

	private Element generateDomDocumentFromDataGroup(DataGroup dataGroupToConvert) {
		Element groupDomElement = domDocument.createElement(dataGroupToConvert.getNameInData());
		iterateAndGenerateChildElements(dataGroupToConvert, domDocument, groupDomElement);
		return groupDomElement;
	}

	private void iterateAndGenerateChildElements(DataGroup dataGroup, Document domDocument,
			Element parentXmlDomElement) {
		for (DataElement childDataElement : dataGroup.getChildren()) {
			generateChildElement(domDocument, parentXmlDomElement, childDataElement);
		}
	}

	private void generateChildElement(Document domDocument, Element parentXmlDomElement,
			DataElement childDataElement) {
		Element domElement = createElement(childDataElement);
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
			if (childDataElement instanceof DataRecordLink dataRecordLink
					&& dataRecordLink.hasReadAction() && addActionLinks) {
				// TODO: resourceLink
				String linkedRecordType = dataRecordLink.getLinkedRecordType();
				String linkedRecordId = dataRecordLink.getLinkedRecordId();
				Element actionLinks = domDocument.createElement("actionLinks");
				domElement.appendChild(actionLinks);
				Element readLink = createReadLink(linkedRecordType, linkedRecordId);
				actionLinks.appendChild(readLink);
			}

		}
	}

	private Element createReadLink(String linkedRecordType, String linkedRecordId) {
		Element readLink = createStandardLink("GET", "read", linkedRecordType, linkedRecordId);
		readLink.appendChild(createAcceptRecordXML());
		return readLink;
	}

	private Element createStandardLink(String requestMethod, String action, String... urlParts) {
		String recordURL = baseUrl + String.join("/", urlParts);
		Element readLink = domDocument.createElement(action);
		readLink.appendChild(createElementWithTextContent("requestMethod", requestMethod));
		readLink.appendChild(createElementWithTextContent("rel", action));
		readLink.appendChild(createElementWithTextContent("url", recordURL));
		return readLink;
	}

	private Element createDeleteLink(String linkedRecordType, String linkedRecordId) {
		return createStandardLink("DELETE", "delete", linkedRecordType, linkedRecordId);
	}

	private Element createElementWithTextContent(String tagName, String textContent) {
		Element requestMethod = domDocument.createElement(tagName);
		requestMethod.setTextContent(textContent);
		return requestMethod;
	}

	private Element createElement(DataElement childDataElement) {
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
	public String convertWithLinks(ExternallyConvertible externallyConvertible, String baseUrl) {
		addActionLinks = true;
		this.baseUrl = baseUrl;
		return tryToCreateAndTransformDomDocumentToString(externallyConvertible);
	}

	public DocumentBuilderFactory getDocumentBuilderFactoryOnlyForTest() {
		return documentBuilderFactory;
	}

	public TransformerFactory getTransformerFactoryOnlyForTest() {
		return transformerFactory;
	}

}
