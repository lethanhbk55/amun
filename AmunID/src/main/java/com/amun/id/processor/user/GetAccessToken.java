package com.amun.id.processor.user;

import org.bson.Document;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.accesstoken.AccessTokenManager;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Status;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

@CommandProcessor(command = { "getAccessToken" })
public class GetAccessToken extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		if (request.variableExists(F.REFRESH_TOKEN)) {
			String refreshToken = request.getString(F.REFRESH_TOKEN);
			MongoCollection<Document> userCollection = getContext().getDatabase().getCollection(F.USER);
			Document doc = new Document(F.REFRESH_TOKEN, refreshToken);
			FindIterable<Document> found = userCollection.find(doc);
			Document first = found.first();
			if (first != null) {
				String userId = first.getString(F.USER_ID);
				String username = first.getString(F.USERNAME);
				AccessTokenInfo token = new AccessTokenInfo(userId, username);
				getContext().getAccessTokenManager().removeByUsername(username);
				getContext().getAccessTokenManager().addAccessToken(token);
				PuObject data = new PuObject();
				data.setString(F.ACCESS_TOKEN, token.getAccessToken());
				data.setLong(F.EXPIRE_IN, token.getLastTouch() + AccessTokenManager.ACCESS_TOKEN_LIVE_TIME);

				PuObject result = new PuObject();
				result.setPuObject(F.DATA, data);
				result.setInteger(F.STATUS, Status.SUCCESS.getCode());
				return result;
			}
			return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.REFRESH_TOKEN_NOT_FOUND));
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.UNKNOWN, F.DATA, "missing refresh token param"));
	}

}
