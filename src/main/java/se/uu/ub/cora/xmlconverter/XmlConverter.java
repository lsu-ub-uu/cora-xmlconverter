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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.xmlconverter.converter.DataElementToXml;

public class XmlConverter implements Converter {

	@Override
	public String convert(DataElement dataElement) {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		return new DataElementToXml(documentBuilderFactory, transformerFactory)
				.convert(dataElement);
	}

	@Override
	public DataElement convert(String dataString) {
		// TODO Auto-generated method stub
		return null;
	}

}
