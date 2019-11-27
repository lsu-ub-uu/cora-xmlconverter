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
import javax.xml.transform.TransformerFactory;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterFactory;

/**
 * Implementation of {@link ConverterFactory} for XmlConverter.
 */

public class XmlConverterFactory implements ConverterFactory {

	private static final String NAME = "xml";

	@Override
	public Converter factorConverter() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		try {

			// TODO: READ https://portswigger.net/web-security/xxe and
			// https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
			documentBuilderFactory
					.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			// documentBuilderFactory
			// .setFeature("http://xml.org/sax/features/external-general-entities", false);
			// documentBuilderFactory
			// .setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// documentBuilderFactory.setFeature(
			// "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// documentBuilderFactory.setXIncludeAware(false);
			// documentBuilderFactory.setExpandEntityReferences(false);
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new XmlConverter(documentBuilderFactory, transformerFactory);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
