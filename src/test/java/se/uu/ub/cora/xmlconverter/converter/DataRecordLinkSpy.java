/*
 * Copyright 2021 Uppsala University Library
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

import java.util.List;

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.xmlconverter.spy.DataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.DataGroupSpy;

public class DataRecordLinkSpy extends DataGroupSpy implements DataRecordLink {
	public boolean readAction = false;
	public String linkedType;
	public String linkedId;

	public DataRecordLinkSpy(String nameInData, String linkedType, String linkedId) {
		super(nameInData);
		this.linkedType = linkedType;
		this.linkedId = linkedId;
		addChild(new DataAtomicSpy("linkedRecordType", linkedType));
		addChild(new DataAtomicSpy("linkedRecordId", linkedId));
	}

	@Override
	public void addAction(Action action) {
		readAction = Action.READ.equals(action);
	}

	@Override
	public List<Action> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasReadAction() {
		return readAction;
	}

	@Override
	public String getLinkedRecordId() {
		return linkedId;
	}

	@Override
	public String getLinkedRecordType() {
		return linkedType;
	}
}
