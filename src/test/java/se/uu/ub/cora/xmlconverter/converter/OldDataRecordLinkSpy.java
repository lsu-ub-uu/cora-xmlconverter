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
import java.util.Optional;

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataChild;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.xmlconverter.spy.OldDataAtomicSpy;
import se.uu.ub.cora.xmlconverter.spy.OldDataGroupSpy;

public class OldDataRecordLinkSpy extends OldDataGroupSpy implements DataRecordLink {
	public boolean readAction = false;
	public String linkedType;
	public String linkedId;

	public OldDataRecordLinkSpy(String nameInData, String linkedType, String linkedId) {
		super(nameInData);
		this.linkedType = linkedType;
		this.linkedId = linkedId;
		addChild(new OldDataAtomicSpy("linkedRecordType", linkedType));
		addChild(new OldDataAtomicSpy("linkedRecordId", linkedId));
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
	public String getLinkedRecordId() {
		return linkedId;
	}

	@Override
	public String getLinkedRecordType() {
		return linkedType;
	}

	@Override
	public <T extends DataChild> List<T> getChildrenOfType(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLinkedRecord(DataGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<DataGroup> getLinkedRecord() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
