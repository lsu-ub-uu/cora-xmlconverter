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
package se.uu.ub.cora.xmlconverter.spy;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

public class TransformerFactorySpy extends TransformerFactory {

	public boolean throwTransformError = false;
	public boolean newTransformerWasCalled = false;

	@Override
	public Source getAssociatedStylesheet(Source arg0, String arg1, String arg2, String arg3)
			throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorListener getErrorListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getFeature(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URIResolver getURIResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Templates newTemplates(Source arg0) throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transformer newTransformer() throws TransformerConfigurationException {
		newTransformerWasCalled = true;
		TransformerSpy transformerSpy = new TransformerSpy();
		if (throwTransformError) {
			transformerSpy.throwTransformerError = true;
		}
		return transformerSpy;
	}

	@Override
	public Transformer newTransformer(Source arg0) throws TransformerConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setErrorListener(ErrorListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFeature(String arg0, boolean arg1) throws TransformerConfigurationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setURIResolver(URIResolver arg0) {
		// TODO Auto-generated method stub

	}

}
