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
package se.uu.ub.cora.xmlconverter.spy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import se.uu.ub.cora.data.Action;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataRecord;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DataRecordSpy implements DataRecord {

	public MethodCallRecorder MCR = new MethodCallRecorder();

	private DataGroupSpy dataGroup;

	public List<Action> actions = Collections.emptyList();

	public String type = "fakeType";

	public String searchId = "fakeSearchId";

	public DataRecordSpy(DataGroupSpy dataGroup) {
		this.dataGroup = dataGroup;
	}

	public DataRecordSpy() {
	}

	@Override
	public String getType() {
		MCR.addCall();
		String returnedValue = type;
		MCR.addReturned(returnedValue);
		return returnedValue;
	}

	@Override
	public String getId() {
		MCR.addCall();
		String returnValue = "fakeId";
		MCR.addReturned(returnValue);
		return returnValue;
	}

	@Override
	public void setDataGroup(DataGroup dataGroup) {
		MCR.addCall("dataGroup", dataGroup);
		this.dataGroup = (DataGroupSpy) dataGroup;
	}

	@Override
	public DataGroup getDataGroup() {
		MCR.addCall();
		MCR.addReturned(this.dataGroup);
		return this.dataGroup;
	}

	@Override
	public void addAction(Action action) {
		MCR.addCall("action", action);
	}

	@Override
	public List<Action> getActions() {
		MCR.addCall();
		MCR.addReturned(actions);
		return actions;
	}

	@Override
	public boolean hasActions() {
		MCR.addCall();
		boolean hasActions = !actions.isEmpty();
		MCR.addReturned(hasActions);
		return hasActions;
	}

	@Override
	public void addReadPermission(String readPermission) {
		MCR.addCall("readPermission", readPermission);
	}

	@Override
	public void addReadPermissions(Collection<String> readPermissions) {
		MCR.addCall("readPermissions", readPermissions);
	}

	@Override
	public Set<String> getReadPermissions() {
		MCR.addCall();
		MCR.addReturned(null);
		return null;
	}

	@Override
	public void addWritePermission(String writePermission) {
		MCR.addCall("writePermission", writePermission);
	}

	@Override
	public void addWritePermissions(Collection<String> writePermissions) {
		MCR.addCall("writePermissions", writePermissions);
	}

	@Override
	public Set<String> getWritePermissions() {
		MCR.addCall();
		MCR.addReturned(null);
		return null;
	}

	@Override
	public boolean hasReadPermissions() {
		MCR.addCall();
		boolean returnValue = false;
		MCR.addReturned(returnValue);
		return returnValue;
	}

	@Override
	public boolean hasWritePermissions() {
		MCR.addCall();
		boolean returnValue = false;
		MCR.addReturned(returnValue);
		return false;
	}

	@Override
	public String getSearchId() {
		MCR.addCall();
		MCR.addReturned(searchId);
		return searchId;
	}

}
