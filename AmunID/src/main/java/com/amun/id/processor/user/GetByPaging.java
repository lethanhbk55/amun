package com.amun.id.processor.user;

import java.util.List;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.model.UserModel;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.user.IDUser;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

@CommandProcessor(command = { "getByPaging" })
public class GetByPaging extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		int limit = request.getInteger(F.LIMIT);
		int skip = request.getInteger(F.SKIP);
		UserModel userModel = getContext().getModelFactory().newModel(UserModel.class);
		List<IDUser> users = userModel.findByPaging(skip, limit);
		PuArray array = new PuArrayList();
		for (IDUser idUser : users) {
			array.addFrom(idUser.toPuObject());
		}
		long count = userModel.count();
		PuObject data = new PuObject();
		data.setLong(F.COUNT, count);
		data.setPuArray(F.ITEMS, array);

		PuObject result = new PuObject();
		result.setInteger(F.STATUS, 0);
		result.setPuObject(F.DATA, data);
		return result;
	}

}
