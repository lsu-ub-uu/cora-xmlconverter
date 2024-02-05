package se.uu.ub.cora.xmlconverter.spy;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataAtomicFactory;

public class DataAtomicFactorySpy implements DataAtomicFactory {

	public List<String> usedNameInDatas = new ArrayList<>();
	public List<String> usedValues = new ArrayList<>();

	@Override
	public DataAtomic factorUsingNameInDataAndValue(String nameInData, String value) {
		usedNameInDatas.add(nameInData);
		usedValues.add(value);
		return new OldDataAtomicSpy(nameInData, value);
	}

	@Override
	public DataAtomic factorUsingNameInDataAndValueAndRepeatId(String nameInData, String value,
			String repeatId) {
		// TODO Auto-generated method stub
		return null;
	}

}
