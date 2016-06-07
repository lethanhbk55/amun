package com.amun.id.processor.user;

import org.bson.Document;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.accesstoken.AccessTokenManager;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.exception.SignDataException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Status;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

@CommandProcessor(command = { "login", "log" })
public class LoginProcessor extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		MongoDatabase database = getContext().getDatabase();
		int status = 1;
		String message = "paramter're missing";

		if (request.variableExists(F.USERNAME) && request.variableExists(F.PASSWORD)) {
			String username = request.getString(F.USERNAME);
			String password = request.getString(F.PASSWORD);

			MongoCollection<Document> collection = database.getCollection(F.USER);
			Document document = new Document();
			document.put(F.USERNAME, username);
			FindIterable<Document> found = collection.find(document);
			Document userDocument = found.first();
			if (userDocument != null) {
				String userId = userDocument.getString(F.USER_ID);
				String refreshToken = userDocument.getString(F.REFRESH_TOKEN);
				String salt = userDocument.getString(F.SALT);
				String hash = SHAEncryptor.sha512Hex(password + salt);

				if (hash.equals(userDocument.getString(F.PASSWORD))) {
					PuObject info = new PuObject();
					info.setString(F.USERNAME, username);
					info.setString(F.USER_ID, userId);
					info.setLong(F.TIMESTAMP, System.currentTimeMillis());

					String signature = null;
					try {
						signature = getContext().signData(info.toJSON());
					} catch (SignDataException e) {
						throw new ExecuteProcessorException(e);
					}
					AccessTokenInfo token = new AccessTokenInfo(userId, username);
					getContext().getAccessTokenManager().removeByUsername(username);
					getContext().getAccessTokenManager().addAccessToken(token);
					PuObject data = new PuObject();
					data.setString(F.ACCESS_TOKEN, token.getAccessToken());
					data.setString(F.REFRESH_TOKEN, refreshToken);
					data.setLong(F.EXPIRE_IN, token.getLastTouch() + AccessTokenManager.ACCESS_TOKEN_LIVE_TIME);
					data.setPuObject(F.INFO, info);
					data.setString(F.SIGNATURE, signature);
					PuObject result = new PuObject();
					result.setInteger(F.STATUS, Status.SUCCESS.getCode());
					result.setPuObject(F.DATA, data);
					return result;
				} else {
					status = 100;
					message = "wrong password";
				}
			} else {
				status = 101;
				message = "user was not exists";
			}
		} else if (request.variableExists(F.FACEBOOK_ID)) {
			throw new ExecuteProcessorException(
					new UnsupportedOperationException("login by facebook hasn't supported yet"));
		}

		return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.DATA, message));
	}

}
