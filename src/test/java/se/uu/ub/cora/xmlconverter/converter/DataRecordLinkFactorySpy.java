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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.DataRecordLink;
import se.uu.ub.cora.data.DataRecordLinkFactory;

public class DataRecordLinkFactorySpy implements DataRecordLinkFactory {
	public List<String> usedNameInDatas = new ArrayList<>();
	public List<String> usedTypes = new ArrayList<>();
	public List<String> usedIds = new ArrayList<>();

	@Override
	public DataRecordLink factorUsingNameInData(String nameInData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRecordLink factorAsLinkWithNameInDataTypeAndId(String nameInData, String recordType,
			String recordId) {
		usedNameInDatas.add(nameInData);
		usedTypes.add(recordType);
		usedIds.add(recordId);
		return new DataRecordLinkSpy(nameInData, recordType, recordId);
	}

}
