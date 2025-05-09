/*
 * Copyright 2019, 2021, 2024 Uppsala University Library
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
import java.util.Optional;
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
import se.uu.ub.cora.converter.ExternalUrls;
import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.Data;
import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataLink;
import se.uu.ub.cora.data.DataList;
import se.uu.ub.cora.data.DataProvider;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.data.DataRecordGroup;
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
	private static final String APPLICATION_VND_CORA_RECORD_LIST_XML = "application/vnd.cora.recordList+xml";
	private static final String APPLICATION_VND_CORA_RECORD_XML = "application/vnd.cora.record+xml";
	private static final String APPLICATION_VND_CORA_RECORDGROUP_XML = "application/vnd.cora.recordgroup+xml";
	private DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;
	private Document domDocument;
	private boolean linksMustBeAdded;
	private String recordType;
	private String recordId;
	private ExternalUrls externalUrls;

	public ExternallyConvertibleToXml(DocumentBuilderFactory documentBuildeFactory,
			TransformerFactory transformerFactory) {
		this.documentBuilderFactory = documentBuildeFactory;
		this.transformerFactory = transformerFactory;
	}

	@Override
	public String convert(ExternallyConvertible externallyConvertible) {
		linksMustBeAdded = false;
		return tryToConvertExternallyConvertibleToXml(externallyConvertible);
	}

	@Override
	public String convertWithLinks(ExternallyConvertible externallyConvertible,
			ExternalUrls externalUrls) {
		linksMustBeAdded = true;
		this.externalUrls = externalUrls;
		return tryToConvertExternallyConvertibleToXml(externallyConvertible);
	}

	private String tryToConvertExternallyConvertibleToXml(
			ExternallyConvertible externallyConvertible) {
		try {
			return convertExternallyConvertibleToXml(externallyConvertible);
		} catch (ParserConfigurationException exception) {
			throw new ConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private String convertExternallyConvertibleToXml(ExternallyConvertible externallyConvertible)
			throws ParserConfigurationException {
		createDomDocumentForExternallyConvertible(externallyConvertible);
		return tryToConvertDomDocumentToXml();
	}

	private void createDomDocumentForExternallyConvertible(
			ExternallyConvertible externallyConvertible) throws ParserConfigurationException {
		domDocument = createAndInitializeDomDocument();
		if (isDataList(externallyConvertible)) {
			addDataListToDomDocument((DataList) externallyConvertible);
		} else if (isDataRecord(externallyConvertible)) {
			addDataRecordToDomDocument((DataRecord) externallyConvertible);
		} else {
			addDataGroupToDomDocument((DataGroup) externallyConvertible);
		}
	}

	private Document createAndInitializeDomDocument() throws ParserConfigurationException {
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document newDomDocument = builder.newDocument();
		newDomDocument.setXmlStandalone(true);
		return newDomDocument;
	}

	private boolean isDataList(ExternallyConvertible externallyConvertible) {
		return externallyConvertible instanceof DataList;
	}

	private void addDataListToDomDocument(DataList dataList) {
		Element listDomElement = domDocument.createElement("dataList");
		domDocument.appendChild(listDomElement);
		addListInfoToDomDocument(dataList, listDomElement);
		Element dataDomElement = createDataElement(listDomElement);
		addAllDatasFromDataListToDomDocument(dataList, dataDomElement);
	}

	private void addListInfoToDomDocument(DataList dataList, Element listDomElement) {
		listDomElement.appendChild(createElementWithTextContent("fromNo", dataList.getFromNo()));
		listDomElement.appendChild(createElementWithTextContent("toNo", dataList.getToNo()));
		listDomElement.appendChild(
				createElementWithTextContent("totalNo", dataList.getTotalNumberOfTypeInStorage()));
		listDomElement.appendChild(
				createElementWithTextContent("containDataOfType", dataList.getContainDataOfType()));
	}

	private Element createDataElement(Element listDomElement) {
		Element dataDomElement = domDocument.createElement("data");
		listDomElement.appendChild(dataDomElement);
		return dataDomElement;
	}

	private void addAllDatasFromDataListToDomDocument(DataList dataList, Element dataDomElement) {
		for (Data data : dataList.getDataList()) {
			Element element = createDomElementForData(data);
			dataDomElement.appendChild(element);
		}
	}

	private String tryToConvertDomDocumentToXml() {
		try {
			return convertDomDocumentToXml();
		} catch (TransformerException exception) {
			throw new ConverterException("Unable to convert from dataElement to xml", exception);
		}
	}

	private String convertDomDocumentToXml() throws TransformerException {
		DOMSource domSource = new DOMSource(domDocument);
		StringWriter xmlWriter = new StringWriter();
		StreamResult xmlResult = new StreamResult(xmlWriter);

		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(domSource, xmlResult);

		return xmlWriter.toString();
	}

	private boolean isDataRecord(ExternallyConvertible externallyConvertible) {
		return externallyConvertible instanceof DataRecord;
	}

	private Element createDomElementForData(Data data) {
		if (isDataRecord(data)) {
			return createDomElementFromDataRecord((DataRecord) data);
		}
		return createDomElementFromDataGroup((DataGroup) data);
	}

	private boolean isDataRecord(Data data) {
		return data instanceof DataRecord;
	}

	private Element createDomElementFromDataRecord(DataRecord dataRecord) {
		Element recordDomElement = domDocument.createElement("record");
		Element dataDomElement = domDocument.createElement("data");
		recordDomElement.appendChild(dataDomElement);

		addTopDataGroup(dataRecord, dataDomElement);
		possiblyAddActionLinks(dataRecord, recordDomElement);
		possiblyAddPermissions(dataRecord, recordDomElement);
		possiblyAddOtherProtocols(dataRecord, recordDomElement);
		return recordDomElement;
	}

	private void addDataRecordToDomDocument(DataRecord dataRecord) {
		Element recordDomElement = createDomElementFromDataRecord(dataRecord);
		domDocument.appendChild(recordDomElement);
	}

	private void addTopDataGroup(DataRecord dataRecord, Element dataDomElement) {
		DataRecordGroup recordGroup = dataRecord.getDataRecordGroup();

		DataGroup topDataGroup = DataProvider.createGroupFromRecordGroup(recordGroup);

		recordType = dataRecord.getType();
		recordId = dataRecord.getId();

		Element groupDomElement = createDomElementFromDataGroup(topDataGroup);
		dataDomElement.appendChild(groupDomElement);
	}

	private void possiblyAddActionLinks(DataRecord dataRecord, Element recordDomElement) {
		if (linksMustBeAdded && dataRecord.hasActions()) {
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
		return linksMustBeAdded && hasReadOrWritePermissions(dataRecord);
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
				createElementWithTextContent(CONTENT_TYPE, "application/vnd.cora.workorder+xml"));
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createBatchIndexLink() {
		Element actionLink = createStandardLink(POST, "batch_index", INDEX, recordId);
		actionLink.appendChild(createContentTypeRecordXML());
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
		actionLink.appendChild(createContentTypeRecordGroupXML());
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
		actionLink.appendChild(createContentTypeRecordGroupXML());
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
		actionLink.appendChild(createContentTypeRecordGroupXML());
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createAcceptRecordXML() {
		return createElementWithTextContent(ACCEPT, APPLICATION_VND_CORA_RECORD_XML);
	}

	private Element createAcceptRecordListXML() {
		return createElementWithTextContent(ACCEPT, APPLICATION_VND_CORA_RECORD_LIST_XML);
	}

	private Element createContentTypeRecordXML() {
		return createElementWithTextContent(CONTENT_TYPE, APPLICATION_VND_CORA_RECORD_XML);
	}

	private Element createContentTypeRecordGroupXML() {
		return createElementWithTextContent(CONTENT_TYPE, APPLICATION_VND_CORA_RECORDGROUP_XML);
	}

	private void addDataGroupToDomDocument(DataGroup topDataGroup) {
		Element groupDomElement = createDomElementFromDataGroup(topDataGroup);
		domDocument.appendChild(groupDomElement);
	}

	private Element createDomElementFromDataGroup(DataGroup dataGroupToConvert) {
		Element groupDomElement = domDocument.createElement(dataGroupToConvert.getNameInData());
		addAttributesIfExistsToElementForDataElement(dataGroupToConvert, groupDomElement);
		iterateAndGenerateChildElements(dataGroupToConvert, domDocument, groupDomElement);
		return groupDomElement;
	}

	private void iterateAndGenerateChildElements(DataGroup dataGroup, Document domDocument,
			Element parentXmlDomElement) {
		for (DataChild childDataElement : dataGroup.getChildren()) {
			createChildElement(domDocument, parentXmlDomElement, childDataElement);
		}
	}

	private void createChildElement(Document domDocument, Element parentXmlDomElement,
			DataChild childDataElement) {
		Element domElement = createElement(childDataElement);
		possiblyAddRepeatIdAsAttribute(childDataElement, domElement);
		if (isAnyDataChildThanResourceLink(childDataElement)) {
			addAttributesIfExistsToElementForDataElement(childDataElement, domElement);
		}
		populateChildElement(domDocument, childDataElement, domElement);
		parentXmlDomElement.appendChild(domElement);
	}

	private boolean isAnyDataChildThanResourceLink(DataChild childDataElement) {
		return !isResourceLink(childDataElement);
	}

	private boolean isResourceLink(DataChild childDataElement) {
		return childDataElement instanceof DataResourceLink;
	}

	private void populateChildElement(Document domDocument, DataChild childDataElement,
			Element domElement) {
		if (isAtomic(childDataElement)) {
			possiblyAddTextToElementForDataAtomic((DataAtomic) childDataElement, domElement);
		} else if (isRecordLink(childDataElement)) {
			populateRecordLink(domDocument, (DataRecordLink) childDataElement, domElement);
		} else if (isResourceLink(childDataElement)) {
			populateResourceLink(domDocument, childDataElement, domElement);
		} else {
			DataGroup childDataGroup = (DataGroup) childDataElement;
			populateDataGroupElement(domDocument, domElement, childDataGroup);

		}
	}

	private void populateRecordLink(Document domDocument, DataRecordLink recordLink,
			Element domElement) {
		// ---SPIKE---
		Optional<DataGroup> linkedRecord = recordLink.getLinkedRecord();
		if (linkedRecord.isPresent()) {
			DataGroup dataGroup = linkedRecord.get();
			iterateAndGenerateChildElements(dataGroup, domDocument, domElement);
		}
		// ---SPIKE---

		Element xmlLinkedType = domDocument.createElement("linkedRecordType");
		xmlLinkedType.setTextContent(recordLink.getLinkedRecordType());

		Element xmlLinkedId = domDocument.createElement("linkedRecordId");
		xmlLinkedId.setTextContent(recordLink.getLinkedRecordId());

		domElement.appendChild(xmlLinkedType);
		domElement.appendChild(xmlLinkedId);
		possiblyAddActionLinks(domDocument, domElement, recordLink);
	}

	private boolean isRecordLink(DataChild childDataElement) {
		return childDataElement instanceof DataRecordLink;
	}

	private boolean isAtomic(DataChild childDataElement) {
		return childDataElement instanceof DataAtomic;
	}

	private void populateResourceLink(Document domDocument, DataChild childDataElement,
			Element domElement) {
		DataResourceLink resourceLink = (DataResourceLink) childDataElement;
		possiblyAddActionLinks(domDocument, domElement, childDataElement);
		Element mimeType = addMimeType(domDocument, resourceLink);
		domElement.appendChild(mimeType);
	}

	private Element addMimeType(Document domDocument, DataResourceLink resourceLink) {
		Element mimeType = domDocument.createElement("mimeType");
		mimeType.setTextContent(resourceLink.getMimeType());
		return mimeType;
	}

	private void populateDataGroupElement(Document domDocument, Element domElement,
			DataGroup childDataGroup) {
		iterateAndGenerateChildElements(childDataGroup, domDocument, domElement);

		possiblyAddActionLinks(domDocument, domElement, childDataGroup);
	}

	private void possiblyAddActionLinks(Document domDocument, Element domElement, DataChild child) {
		if (isLinkThatShouldBeConverted(child)) {
			Element actionLinks = domDocument.createElement("actionLinks");
			domElement.appendChild(actionLinks);
			addLinkElement(child, actionLinks);
		}
	}

	private void addLinkElement(DataChild child, Element actionLinks) {
		Element linkElement;
		if (isRecordLink(child)) {
			linkElement = createRecordLinkElement((DataRecordLink) child);
		} else {
			linkElement = createResourceLinkElement((DataResourceLink) child);
		}
		actionLinks.appendChild(linkElement);
	}

	private Element createResourceLinkElement(DataResourceLink dataResourceLink) {
		Element readLink = createStandardLink(GET, "read", recordType, recordId,
				dataResourceLink.getNameInData());
		readLink.appendChild(createElementWithTextContent(ACCEPT, dataResourceLink.getMimeType()));
		return readLink;
	}

	private Element createRecordLinkElement(DataRecordLink dataRecordLink) {
		String linkedRecordType = dataRecordLink.getLinkedRecordType();
		String linkedRecordId = dataRecordLink.getLinkedRecordId();
		return createReadLink(linkedRecordType, linkedRecordId);
	}

	private boolean isLinkThatShouldBeConverted(DataChild childDataElement) {
		if (isDataLink(childDataElement)) {
			return linksMustBeAdded && ((DataLink) childDataElement).hasReadAction();
		}
		return false;
	}

	private boolean isDataLink(DataChild childDataElement) {
		return childDataElement instanceof DataLink;
	}

	private Element createReadLink(String linkedRecordType, String linkedRecordId) {
		Element actionLink = createStandardLink(GET, "read", linkedRecordType, linkedRecordId);
		actionLink.appendChild(createAcceptRecordXML());
		return actionLink;
	}

	private Element createStandardLink(String requestMethod, String action, String... urlParts) {
		String recordURL = externalUrls.getBaseUrl() + String.join("/", urlParts);
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

	private Element createElement(DataChild childDataElement) {
		return domDocument.createElement(childDataElement.getNameInData());
	}

	private void possiblyAddRepeatIdAsAttribute(DataChild childDataElement, Element domElement) {
		if (hasNonEmptyRepeatId(childDataElement)) {
			domElement.setAttribute("repeatId", childDataElement.getRepeatId());
		}
	}

	private boolean hasNonEmptyRepeatId(DataChild childDataElement) {
		return childDataElement.getRepeatId() != null && !childDataElement.getRepeatId().isEmpty();
	}

	private void possiblyAddTextToElementForDataAtomic(DataAtomic childDataAtomic,
			Element domElement) {
		domElement.setTextContent(childDataAtomic.getValue());
	}

	private void addAttributesIfExistsToElementForDataElement(DataChild childDataElement,
			Element domElement) {
		Collection<DataAttribute> attributes = childDataElement.getAttributes();

		for (DataAttribute attribute : attributes) {
			domElement.setAttribute(attribute.getNameInData(), attribute.getValue());
		}
	}

	private void possiblyAddOtherProtocols(DataRecord dataRecord, Element domElement) {
		if (linksMustBeAddedAndHasOtherProtocols(dataRecord)) {
			Element iiif = createIiifProtocol(dataRecord);
			Element otherProtocols = createOtherProtocolsUsingProtocols(iiif);
			domElement.appendChild(otherProtocols);
		}
	}

	private boolean linksMustBeAddedAndHasOtherProtocols(DataRecord dataRecord) {
		return linksMustBeAdded && hasOtherProtocols(dataRecord);
	}

	private Element createOtherProtocolsUsingProtocols(Element iiif) {
		Element otherProtocols = domDocument.createElement("otherProtocols");
		otherProtocols.appendChild(iiif);
		return otherProtocols;
	}

	private Element createIiifProtocol(DataRecord dataRecord) {
		Element server = createIiifServer();
		Element identifier = createIiifIdentifier(dataRecord);
		return createElementAndAppendChilds(server, identifier);
	}

	private Element createElementAndAppendChilds(Element... childs) {
		Element element = domDocument.createElement("iiif");
		for (Element child : childs) {
			element.appendChild(child);
		}
		return element;
	}

	private Element createIiifServer() {
		Element server = domDocument.createElement("server");
		server.setTextContent(externalUrls.getIfffUrl());
		return server;
	}

	private Element createIiifIdentifier(DataRecord dataRecord) {
		Element identifier = domDocument.createElement("identifier");
		identifier.setTextContent(dataRecord.getId());
		return identifier;
	}

	private boolean hasOtherProtocols(DataRecord dataRecord) {
		return !dataRecord.getProtocols().isEmpty();
	}

	public DocumentBuilderFactory getDocumentBuilderFactoryOnlyForTest() {
		return documentBuilderFactory;
	}

	public TransformerFactory getTransformerFactoryOnlyForTest() {
		return transformerFactory;
	}

}
