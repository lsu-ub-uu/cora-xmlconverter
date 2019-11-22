package se.uu.ub.cora.xmlconverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataElement;
import se.uu.ub.cora.data.DataGroup;

public class DataGroupSpy implements DataGroup {

	public String nameIndata;
	public List<DataElement> children = new ArrayList<>();
	public Map<String, DataGroup> dataGroups = new HashMap<>();
	public Map<String, String> attributes = new HashMap<>();
	private String repeatId;

	public DataGroupSpy(String nameIndata) {
		this.nameIndata = nameIndata;
	}

	@Override
	public String getRepeatId() {
		return repeatId;
	}

	@Override
	public String getNameInData() {
		// TODO Auto-generated method stub
		return nameIndata;
	}

	@Override
	public String getFirstAtomicValueWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
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
		children.add(dataElement);
	}

	@Override
	public List<DataElement> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public boolean containsChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRepeatId(String repeatId) {
		this.repeatId = repeatId;
	}

	@Override
	public void addAttributeByIdWithValue(String id, String value) {
		attributes.put(id, value);
	}

	@Override
	public DataElement getFirstChildWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataGroup> getAllGroupsWithNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(String attributeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataAtomic> getAllDataAtomicsWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeFirstChildWithNameInData(String childNameInData) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

}
