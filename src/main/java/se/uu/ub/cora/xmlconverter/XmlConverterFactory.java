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

package se.uu.ub.cora.xmlconverter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import se.uu.ub.cora.converter.ConverterFactory;
import se.uu.ub.cora.converter.ConverterInitializationException;
import se.uu.ub.cora.converter.ExternallyConvertibleToStringConverter;
import se.uu.ub.cora.converter.StringToExternallyConvertibleConverter;
import se.uu.ub.cora.xmlconverter.converter.ExternallyConvertibleToXml;
import se.uu.ub.cora.xmlconverter.converter.XmlToExternallyConvertible;

/**
 * Implementation of {@link ConverterFactory} for XmlConverter.
 */
public class XmlConverterFactory implements ConverterFactory {

	private static final String NAME = "xml";

	@Override
	public ExternallyConvertibleToStringConverter factorExternallyConvertableToStringConverter() {
		DocumentBuilderFactory documentBuilderFactory = createDocumentBuilder();

		TransformerFactory transformerFactory = createTransformerFactory();
		return new ExternallyConvertibleToXml(documentBuilderFactory, transformerFactory);
	}

	private DocumentBuilderFactory createDocumentBuilder() {
		DocumentBuilderFactory documentBuilderFactory = getNewDocumentBuilder();

		try {
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			setApacheFeature(documentBuilderFactory, "disallow-doctype-decl", true);
			setApacheFeature(documentBuilderFactory, "nonvalidating/load-external-dtd", false);
			setXmlFeature(documentBuilderFactory, "external-general-entities");
			setXmlFeature(documentBuilderFactory, "external-parameter-entities");
			documentBuilderFactory.setExpandEntityReferences(false);
		} catch (Exception exception) {
			throw new ConverterInitializationException(
					"Unable to set security features for DocumentBuilderFactory", exception);
		}
		return documentBuilderFactory;
	}

	private void setApacheFeature(DocumentBuilderFactory documentBuilderFactory, String feature,
			boolean featureOnOff) throws ParserConfigurationException {
		documentBuilderFactory.setFeature("http://apache.org/xml/features/" + feature,
				featureOnOff);
	}

	private void setXmlFeature(DocumentBuilderFactory documentBuilderFactory, String feature)
			throws ParserConfigurationException {
		documentBuilderFactory.setFeature("http://xml.org/sax/features/" + feature, false);
	}

	private TransformerFactory createTransformerFactory()
			throws TransformerFactoryConfigurationError {
		TransformerFactory transformerFactory = getNewTransformerFactory();

		try {
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (Exception exception) {
			throw new ConverterInitializationException(
					"Unable to set security features for TransformerFactory", exception);
		}
		return transformerFactory;
	}

	TransformerFactory getNewTransformerFactory() throws TransformerFactoryConfigurationError {
		return TransformerFactory.newInstance();
	}

	@Override
	public StringToExternallyConvertibleConverter factorStringToExternallyConvertableConverter() {
		DocumentBuilderFactory documentBuilderFactory = createDocumentBuilder();
		return new XmlToExternallyConvertible(documentBuilderFactory);
	}

	DocumentBuilderFactory getNewDocumentBuilder() {
		return DocumentBuilderFactory.newInstance();
	}

	@Override
	public String getName() {
		return NAME;
	}
}
