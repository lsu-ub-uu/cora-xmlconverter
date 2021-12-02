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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAttribute;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class DataGroupSpy implements DataGroup {

	public String nameIndata;
	public List<DataElement> children = new ArrayList<>();
	public Map<String, DataGroup> dataGroups = new HashMap<>();
	public Map<String, DataAtomic> dataAtomics = new HashMap<>();
	public List<DataAttribute> attributes = new ArrayList<>();
	private String repeatId;
	public Map<String, String> atomicValues = new HashMap<>();

	public DataGroupSpy(String nameIndata) {
		this.nameIndata = nameIndata;
	}

	@Override
	public String getRepeatId() {
		return repeatId;
	}

	@Override
	public String getNameInData() {
		return nameIndata;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		return atomicValues.get(nameInData);
	}

	@Override
	public DataGroup getFirstGroupWithNameInData(String childNameInData) {
		return dataGroups.get(childNameInData);
	}

	@Override
	public void addChild(DataElement dataElement) {
		if (dataElement instanceof DataGroup) {
			dataGroups.put(dataElement.getNameInData(), (DataGroup) dataElement);
		}
		if (dataElement instanceof DataAtomicSpy) {
			DataAtomicSpy atomicSpyChild = (DataAtomicSpy) dataElement;
			atomicValues.put(atomicSpyChild.nameInData, atomicSpyChild.value);
			dataAtomics.put(dataElement.getNameInData(), (DataAtomic) dataElement);

		}
		children.add(dataElement);
	}

	@Override
	public List<DataElement> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		if (atomicValues.containsKey(nameInData) || dataGroups.containsKey(nameInData)) {
			return true;
		}
		return false;
	}

	@Override
	public void setRepeatId(String repeatId) {
		this.repeatId = repeatId;
	}

	@Override
	public void addAttributeByIdWithValue(String id, String value) {
		attributes.add(new DataAttributeSpy(id, value));
	}

	@Override
	public DataElement getFirstChildWithNameInData(String nameInData) {
		if (dataGroups.containsKey(nameInData)) {
			return dataGroups.get(nameInData);
		}
		return dataAtomics.get(nameInData);
	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataAttribute getAttribute(String attributeId) {
		// TODO Auto-generated method stub
		for (DataAttribute dataAttribute : attributes) {
			if (dataAttribute.getNameInData().equals(attributeId)) {
				return dataAttribute;
			}
		}
		return null;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeFirstChildWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public Collection<DataAttribute> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

	@Override
	public Collection<DataGroup> getAllGroupsWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addChildren(Collection<DataElement> dataElements) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DataElement> getAllChildrenWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataElement> getAllChildrenWithNameInDataAndAttributes(String nameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAllChildrenWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAllChildrenWithNameInDataAndAttributes(String childNameInData,
			DataAttribute... childAttributes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataAtomic getFirstDataAtomicWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

}
