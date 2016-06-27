package com.amun.id.processor.user;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Status;
import com.amun.id.user.IDUser;
import com.amun.id.utils.StringUtils;
import com.hazelcast.core.IMap;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

@CommandProcessor(command = { "changePass" })
public class ChangePassword extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		String accessToken = request.getString(F.ACCESS_TOKEN);
		String newPassword = request.getString(F.NEW_PASSWORD);
		AccessTokenInfo info = getContext().getAccessTokenManager().getAccessToken(accessToken);
		if (info != null) {
			String username = info.getUsername();
			IMap<String, IDUser> mapstore = getContext().getHazelcast().getMap(IDUser.ID_USER_MAP_KEY);
			IDUser idUser = mapstore.get(username);
			if (idUser != null) {
				String salt = StringUtils.randomString(8);
				String password = SHAEncryptor.sha512Hex(newPassword + salt);
				idUser.setSalt(salt);
				idUser.setPassword(password);
				mapstore.replace(username, idUser);
				getContext().getAccessTokenManager().touchAccessToken(accessToken);
				return PuObject.fromObject(new MapTuple<>(F.STATUS, 0));
			}
			return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.USER_NOT_REGISTED.getCode()));
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.ACCESS_TOKEN_INVALID.getCode()));
	}

}
