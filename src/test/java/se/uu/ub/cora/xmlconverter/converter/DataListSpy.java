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

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.data.Data;
import se.uu.ub.cora.data.DataList;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;

public class DataListSpy implements DataList {
	MethodCallRecorder MCR = new MethodCallRecorder();
	List<Data> list = new ArrayList<>();

	@Override
	public String getFromNo() {
		MCR.addCall();
		String out = "1";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public String getToNo() {
		MCR.addCall();
		String out = "99";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public String getTotalNumberOfTypeInStorage() {
		MCR.addCall();
		String out = "99999";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public String getContainDataOfType() {
		MCR.addCall();
		String out = "mix";
		MCR.addReturned(out);
		return out;
	}

	@Override
	public List<Data> getDataList() {
		MCR.addCall();
		MCR.addReturned(list);
		return list;
	}

	@Override
	public void addData(Data data) {
		MCR.addCall("data", data);
		list.add(data);
	}

	@Override
	public void setFromNo(String position) {
	}

	@Override
	public void setToNo(String position) {
	}

	@Override
	public void setTotalNo(String totalNumber) {
	}

}
