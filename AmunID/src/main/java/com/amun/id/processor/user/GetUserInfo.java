package com.amun.id.processor.user;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Status;
import com.amun.id.user.IDUser;
import com.hazelcast.core.IMap;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

@CommandProcessor(command = { "getAmunUserInfo", "getUserInfo" })
public class GetUserInfo extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		String username = request.getString(F.USERNAME);
		IMap<String, IDUser> mapstore = getContext().getHazelcast().getMap(IDUser.ID_USER_MAP_KEY);
		IDUser user = mapstore.get(username);
		if (user != null) {
			PuObject result = new PuObject();
			result.setInteger(F.STATUS, Status.SUCCESS.getCode());
			result.setPuObject(F.DATA, user.toPuObject());
			return result;
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.UNKNOWN.getCode()));
	}

}
