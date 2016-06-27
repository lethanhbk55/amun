package com.amun.id.processor.user;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.model.UserModel;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Status;
import com.amun.id.user.IDUser;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

@CommandProcessor(command = "refreshToken")
public class RefreshAccessToken extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		if (request.variableExists(F.REFRESH_TOKEN)) {
			String refreshToken = request.getString(F.REFRESH_TOKEN);
			UserModel model = getContext().getModelFactory().newModel(UserModel.class);
			IDUser user = model.findByRefreshToken(refreshToken);
			if (user != null) {
				AccessTokenInfo info = new AccessTokenInfo(user.getUserId(), user.getUsername());
				getContext().getAccessTokenManager().addAccessToken(info);
				return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.DATA, info.toPuObject()));
			}
			return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.REFRESH_TOKEN_NOT_FOUND.getCode()));
		}
		throw new ExecuteProcessorException("missing refresh token param");
	}

}
