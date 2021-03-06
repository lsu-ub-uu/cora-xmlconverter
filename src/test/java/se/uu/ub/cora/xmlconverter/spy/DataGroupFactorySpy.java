package se.uu.ub.cora.xmlconverter.spy;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;

public class DataGroupFactorySpy implements DataGroupFactory {

	public List<String> usedNameInDatas = new ArrayList<>();

	@Override
	public DataGroup factorUsingNameInData(String nameInData) {
		usedNameInDatas.add(nameInData);
		return new DataGroupSpy(nameInData);
	}

	@Override
	public DataGroup factorAsLinkWithNameInDataTypeAndId(String nameInData, String recordType,
			String recordId) {
		// TODO Auto-generated method stub
		return null;
	}

}
