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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactorySpy extends DocumentBuilderFactory {

	public boolean throwParserError = false;

	@Override
	public Object getAttribute(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getFeature(String arg0) throws ParserConfigurationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		if (throwParserError) {
			throw new ParserConfigurationException("some message from DocumentBuilderFactorySpy");
		}
		return null;
	}

	@Override
	public void setAttribute(String arg0, Object arg1) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFeature(String arg0, boolean arg1) throws ParserConfigurationException {
		// TODO Auto-generated method stub

	}

}
