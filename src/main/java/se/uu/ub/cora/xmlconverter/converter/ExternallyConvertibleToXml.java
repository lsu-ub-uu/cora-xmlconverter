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
import java.util.Set;

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

import se.uu.ub.cora.converter.ConverterException;
import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.Data;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataLink;
import se.uu.ub.cora.data.DataList;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.data.DataResourceLink;
import se.uu.ub.cora.data.ExternallyConvertible;

public class ExternallyConvertibleToXml implements ExternallyConvertibleToStringConverter {

	private static final String ACCEPT = "accept";
	private static final String INDEX = "index";
	private static final String WORK_ORDER = "workOrder";
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String CONTENT_TYPE = "contentType";
	private static final String APPLICATION_VND_UUB_RECORD_LIST_XML = "application/vnd.uub.recordList+xml";
	private static final String APPLICATION_VND_UUB_RECORD_XML = "application/vnd.uub.record+xml";
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Document domDocument;
	private String baseUrl;
	private boolean addActionLinks;
	private String recordType;
	private String recordId;

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

	@Override
	public String convertWithLinks(ExternallyConvertible externallyConvertible, String baseUrl) {
		addActionLinks = true;
		this.baseUrl = baseUrl;
		return tryToCreateAndTransformDomDocumentToString(externallyConvertible);
	}

	private String tryToCreateAndTransformDomDocumentToString(
			ExternallyConvertible externallyConvertible) {
		try {
			return createAndTransformDomDocumentToString(externallyConvertible);
		} catch (ParserConfigurationException exception) {
			throw new ConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private String createAndTransformDomDocumentToString(
			ExternallyConvertible externallyConvertible) throws ParserConfigurationException {
		domDocument = initializeDomDocument();
		addConvertibleToDomDocument(externallyConvertible);
		return transformDomDocumentToXml();
	}

	private Document initializeDomDocument() throws ParserConfigurationException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document newDomDocument = builder.newDocument();
		newDomDocument.setXmlStandalone(true);
		return newDomDocument;
	}

	private void addConvertibleToDomDocument(ExternallyConvertible externallyConvertible) {
		if (externallyConvertible instanceof DataList) {
			addDataListToDomDocument((DataList) externallyConvertible);
		} else if (externallyConvertible instanceof DataRecord) {
			addDataRecordToDomDocument((DataRecord) externallyConvertible);
		} else {
			addDataGroupToDomDocument(externallyConvertible);
		}
	}

	private void addDataListToDomDocument(DataList dataList) {
		Element listDomElement = domDocument.createElement("dataList");
		domDocument.appendChild(listDomElement);
		addListInfoToDomDocument(dataList, listDomElement);
		addListDataToDomDocument(dataList, listDomElement);
	}

	private void addListInfoToDomDocument(DataList dataList, Element listDomElement) {
		listDomElement.appendChild(createElementWithTextContent("fromNo", dataList.getFromNo()));
		listDomElement.appendChild(createElementWithTextContent("toNo", dataList.getToNo()));
		listDomElement.appendChild(
				createElementWithTextContent("totalNo", dataList.getTotalNumberOfTypeInStorage()));
		listDomElement.appendChild(
				createElementWithTextContent("containDataOfType", dataList.getContainDataOfType()));
	}

	private void addListDataToDomDocument(DataList dataList, Element listDomElement) {
		Element dataDomElement = domDocument.createElement("data");
		listDomElement.appendChild(dataDomElement);
		List<Data> listOfData = dataList.getDataList();
		for (Data data : listOfData) {
			addElementFromData(dataDomElement, data);
		}
	}

	private void addElementFromData(Element dataDomElement, Data data) {
		Element element = generateElementFromData(data);
		dataDomElement.appendChild(element);
	}

	private Element generateElementFromData(Data data) {
		if (data instanceof DataRecord) {
			return generateElementFromDataRecord((DataRecord) data);
		}
		return generateElementFromDataGroup((DataGroup) data);
	}

	private void addDataRecordToDomDocument(DataRecord dataRecord) {
		Element recordDomElement = generateElementFromDataRecord(dataRecord);
		domDocument.appendChild(recordDomElement);
	}

	private Element generateElementFromDataRecord(DataRecord dataRecord) {
		Element recordDomElement = domDocument.createElement("record");
		Element dataDomElement = domDocument.createElement("data");
		recordDomElement.appendChild(dataDomElement);

		addTopDataGroup(dataRecord, dataDomElement);
		possiblyAddActionLinks(dataRecord, recordDomElement);
		possiblyAddPermissions(dataRecord, recordDomElement);
		return recordDomElement;
	}

	private void addTopDataGroup(DataRecord dataRecord, Element dataDomElement) {
		DataGroup topDataGroup = dataRecord.getDataGroup();

		recordType = dataRecord.getType();
		recordId = dataRecord.getId();

		Element groupDomElement = generateElementFromDataGroup(topDataGroup);
		dataDomElement.appendChild(groupDomElement);
	}

	private void possiblyAddActionLinks(DataRecord dataRecord, Element recordDomElement) {
		if (addActionLinks && dataRecord.hasActions()) {
			addExistingActionLinks(dataRecord, recordDomElement);
		}
	}

	private void addExistingActionLinks(DataRecord dataRecord, Element recordDomElement) {
		List<Action> actions = dataRecord.getActions();

		Element actionLinks = domDocument.createElement("actionLinks");
		recordDomElement.appendChild(actionLinks);

		if (actions.contains(Action.READ)) {
			Element readLink = createReadLink(recordType, recordId);
			actionLinks.appendChild(readLink);
		}
		if (actions.contains(Action.UPDATE)) {
			Element updateLink = createUpdateLink();
			actionLinks.appendChild(updateLink);
		}
		if (actions.contains(Action.DELETE)) {
			Element deleteLink = createDeleteLink();
			actionLinks.appendChild(deleteLink);
		}
		if (actions.contains(Action.READ_INCOMING_LINKS)) {
			Element readIncomingLink = createReadIncomingLink();
			actionLinks.appendChild(readIncomingLink);
		}
		if (actions.contains(Action.INDEX)) {
			Element indexLink = createIndexLink();
			actionLinks.appendChild(indexLink);
		}
		if (actions.contains(Action.UPLOAD)) {
			Element uploadLink = createUploadLink();
			actionLinks.appendChild(uploadLink);
		}
		if (actions.contains(Action.SEARCH)) {
			Element searchLink = createSearchLink(dataRecord.getSearchId());
			actionLinks.appendChild(searchLink);
		}
		if (actions.contains(Action.CREATE)) {
			Element createLink = createCreateLink();
			actionLinks.appendChild(createLink);
		}
		if (actions.contains(Action.LIST)) {
			Element listLink = createListLink();
			actionLinks.appendChild(listLink);
		}
		if (actions.contains(Action.BATCH_INDEX)) {
			Element batchIndexLink = createBatchIndexLink();
			actionLinks.appendChild(batchIndexLink);
		}
		if (actions.contains(Action.VALIDATE)) {
			Element validateLink = createValidateLink();
			actionLinks.appendChild(validateLink);
		}
	}

	private void possiblyAddPermissions(DataRecord dataRecord, Element recordDomElement) {
		if (shouldPermissionBeConverted(dataRecord)) {
			addPermissions(dataRecord, recordDomElement);
		}
	}

	private boolean shouldPermissionBeConverted(DataRecord dataRecord) {
		return addActionLinks && hasReadOrWritePermissions(dataRecord);
	}

	private boolean hasReadOrWritePermissions(DataRecord dataRecord) {
		return dataRecord.hasReadPermissions() || dataRecord.hasWritePermissions();
	}

	private void addPermissions(DataRecord dataRecord, Element recordDomElement) {
		Element permissionsElement = domDocument.createElement("permissions");
		recordDomElement.appendChild(permissionsElement);

		if (dataRecord.hasReadPermissions()) {
			addPermissionElements(permissionsElement, dataRecord.getReadPermissions(), "read");
		}
		if (dataRecord.hasWritePermissions()) {
			addPermissionElements(permissionsElement, dataRecord.getWritePermissions(), "write");
		}
	}

	private void addPermissionElements(Element permissionsElement, Set<String> permissions,
			String tagName) {
		Element readPermissions = domDocument.createElement(tagName);
		permissionsElement.appendChild(readPermissions);

		for (String readPermission : permissions) {
			readPermissions.appendChild(createElementWithTextContent("permission", readPermission));
		}
	}

	private Element createValidateLink() {
		Element actionLink = createStandardLink(POST, "validate", WORK_ORDER);
		actionLink.appendChild(
				createElementWithTextContent(CONTENT_TYPE, "application/vnd.uub.workorder+xml"));
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createBatchIndexLink() {
		Element actionLink = createStandardLink(POST, "batch_index", INDEX, recordId);
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createListLink() {
		Element actionLink = createStandardLink(GET, "list", recordId);
		actionLink.appendChild(createAcceptRecordListXML());
		return actionLink;
	}

	private Element createCreateLink() {
		Element actionLink = createStandardLink(POST, "create", recordId);
		actionLink.appendChild(createContentTypeRecordXML());
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createSearchLink(String searchId) {
		Element actionLink = createStandardLink(GET, "search", "searchResult", searchId);
		actionLink.appendChild(createAcceptRecordListXML());
		return actionLink;
	}

	private Element createUploadLink() {
		Element actionLink = createStandardLink(POST, "upload", recordType, recordId, "master");
		actionLink.appendChild(createElementWithTextContent(CONTENT_TYPE, "multipart/form-data"));
		return actionLink;
	}

	private Element createIndexLink() {
		Element actionLink = createStandardLink(POST, INDEX, WORK_ORDER);
		actionLink.appendChild(createContentTypeRecordXML());
		actionLink.appendChild(createAcceptRecordXML());
		actionLink.appendChild(createWorkOrderXML());
		return actionLink;
	}

	private Element createWorkOrderXML() {
		Element body = domDocument.createElement("body");
		Element workOrder = domDocument.createElement(WORK_ORDER);
		body.appendChild(workOrder);
		Element recordTypeElement = domDocument.createElement("recordType");
		workOrder.appendChild(recordTypeElement);
		recordTypeElement
				.appendChild(createElementWithTextContent("linkedRecordType", "recordType"));
		recordTypeElement.appendChild(createElementWithTextContent("linkedRecordId", recordType));
		recordTypeElement.appendChild(createElementWithTextContent("recordId", recordId));
		recordTypeElement.appendChild(createElementWithTextContent("type", INDEX));
		return body;
	}

	private Element createReadIncomingLink() {
		Element actionLink = createStandardLink(GET, "read_incoming_links", recordType, recordId,
				"incomingLinks");
		actionLink.appendChild(createAcceptRecordListXML());
		return actionLink;
	}

	private Element createUpdateLink() {
		Element actionLink = createStandardLink(POST, "update", recordType, recordId);
		actionLink.appendChild(createContentTypeRecordXML());
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createAcceptRecordXML() {
		return createElementWithTextContent(ACCEPT, APPLICATION_VND_UUB_RECORD_XML);
	}

	private Element createAcceptRecordListXML() {
		return createElementWithTextContent(ACCEPT, APPLICATION_VND_UUB_RECORD_LIST_XML);
	}

	private Element createContentTypeRecordXML() {
		return createElementWithTextContent(CONTENT_TYPE, APPLICATION_VND_UUB_RECORD_XML);
	}

	private void addDataGroupToDomDocument(ExternallyConvertible externallyConvertible) {
		DataGroup topDataGroup = (DataGroup) externallyConvertible;
		Element groupDomElement = generateElementFromDataGroup(topDataGroup);
		domDocument.appendChild(groupDomElement);
	}

	private Element generateElementFromDataGroup(DataGroup dataGroupToConvert) {
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
		populateChildElement(domDocument, childDataElement, domElement);
		parentXmlDomElement.appendChild(domElement);
	}

	private void populateChildElement(Document domDocument, DataElement childDataElement,
			Element domElement) {
		if (childDataElement instanceof DataAtomic) {
			possiblyAddTextToElementForDataAtomic((DataAtomic) childDataElement, domElement);
		} else {
			DataGroup childDataGroup = (DataGroup) childDataElement;
			populateDataGroupElement(domDocument, domElement, childDataGroup);

		}
	}

	private void populateDataGroupElement(Document domDocument, Element domElement,
			DataGroup childDataGroup) {
		addAttributesIfExistsToElementForDataGroup(childDataGroup, domElement);
		iterateAndGenerateChildElements(childDataGroup, domDocument, domElement);

		if (isLinkThatShouldBeConverted(childDataGroup)) {
			Element actionLinks = domDocument.createElement("actionLinks");
			domElement.appendChild(actionLinks);
			addLinkElement(childDataGroup, actionLinks);
		}
	}

	private void addLinkElement(DataGroup childDataGroup, Element actionLinks) {
		Element linkElement;
		if (childDataGroup instanceof DataRecordLink) {
			linkElement = createRecordLinkElement(childDataGroup);
		} else {
			linkElement = createResourceLinkElement(childDataGroup);
		}
		actionLinks.appendChild(linkElement);
	}

	private Element createResourceLinkElement(DataGroup childDataGroup) {
		DataResourceLink dataResourceLink = (DataResourceLink) childDataGroup;
		Element readLink = createStandardLink(GET, "read", recordType, recordId,
				dataResourceLink.getNameInData());
		readLink.appendChild(createElementWithTextContent(ACCEPT, dataResourceLink.getMimeType()));
		return readLink;
	}

	private Element createRecordLinkElement(DataGroup childDataGroup) {
		DataRecordLink dataRecordLink = (DataRecordLink) childDataGroup;
		String linkedRecordType = dataRecordLink.getLinkedRecordType();
		String linkedRecordId = dataRecordLink.getLinkedRecordId();
		return createReadLink(linkedRecordType, linkedRecordId);
	}

	private boolean isLinkThatShouldBeConverted(DataElement childDataElement) {
		if (childDataElement instanceof DataLink) {
			return addActionLinks && ((DataLink) childDataElement).hasReadAction();
		}
		return false;
	}

	private Element createReadLink(String linkedRecordType, String linkedRecordId) {
		Element actionLink = createStandardLink(GET, "read", linkedRecordType, linkedRecordId);
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createStandardLink(String requestMethod, String action, String... urlParts) {
		String recordURL = baseUrl + String.join("/", urlParts);
		Element actionLink = domDocument.createElement(action);
		actionLink.appendChild(createElementWithTextContent("requestMethod", requestMethod));
		actionLink.appendChild(createElementWithTextContent("rel", action));
		actionLink.appendChild(createElementWithTextContent("url", recordURL));
		return actionLink;
	}

	private Element createDeleteLink() {
		return createStandardLink("DELETE", "delete", recordType, recordId);
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

	private String transformDomDocumentToXml() {
		try {
			return tryToTransformDomDocumentToXml();
		} catch (TransformerException exception) {
			throw new ConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private String tryToTransformDomDocumentToXml() throws TransformerException {
		DOMSource domSource = new DOMSource(domDocument);
		StringWriter xmlWriter = new StringWriter();
		StreamResult xmlResult = new StreamResult(xmlWriter);

		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(domSource, xmlResult);

		return xmlWriter.toString();
	}

	public DocumentBuilderFactory getDocumentBuilderFactoryOnlyForTest() {
		return documentBuilderFactory;
	}

	public TransformerFactory getTransformerFactoryOnlyForTest() {
		return transformerFactory;
	}

}
