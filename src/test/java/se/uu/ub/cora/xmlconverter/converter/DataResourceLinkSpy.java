/*
 * Copyright  2021 Uppsala University Library
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

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataResourceLink;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;

public class DataResourceLinkSpy extends DataGroupSpy implements DataResourceLink {
	public boolean readAction = false;
	private String mimeType;

	public DataResourceLinkSpy(String nameInData, String streamId, String fileName, String fileSize,
			String mimeType) {
		super(nameInData);
		this.mimeType = mimeType;
		addChild(new DataAtomicSpy("streamId", streamId));
		addChild(new DataAtomicSpy("fileName", fileName));
		addChild(new DataAtomicSpy("fileSize", fileSize));
		addChild(new DataAtomicSpy("mimeType", mimeType));
	}

	@Override
	public void addAction(Action action) {
		readAction = Action.READ.equals(action);
	}

	@Override
	public boolean hasReadAction() {
		return readAction;
	}

	@Override
	public String getMimeType() {
		return mimeType;
	}

	@Override
	public void setStreamId(String streamId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getStreamId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFileName(String fileName) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFileSize(String fileSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFileSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMimeType(String mimeType) {
		// TODO Auto-generated method stub

	}

}
