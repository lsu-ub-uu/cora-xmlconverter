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

import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class TransformerSpy extends Transformer {

	public boolean throwTransformerError = false;

	@Override
	public void clearParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public ErrorListener getErrorListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getOutputProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOutputProperty(String arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URIResolver getURIResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorListener(ErrorListener arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputProperties(Properties arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputProperty(String arg0, String arg1) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParameter(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setURIResolver(URIResolver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transform(Source arg0, Result arg1) throws TransformerException {
		if (throwTransformerError) {
			throw new TransformerException("some error from TransformerSpy");
		}
		// TODO Auto-generated method stub

	}

}
