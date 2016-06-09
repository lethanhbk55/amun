package com.amun.id.processor.user;

import org.bson.Document;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.accesstoken.AccessTokenManager;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.exception.SignDataException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.Platform;
import com.amun.id.statics.Status;
import com.amun.id.user.IDUser;
import com.amun.id.utils.Counters;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

@CommandProcessor(command = "loginQuickPlay")
public class LoginQuickPlay extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		if (!request.variableExists(F.DEVICE_ID) || !request.variableExists(F.PLATFORM_ID)) {
			throw new ExecuteProcessorException("missing deviceId or platformId params");
		}

		String deviceId = request.getString(F.DEVICE_ID);
		int platformId = request.getInteger(F.PLATFORM_ID);
		MongoCollection<Document> collection = getContext().getDatabase().getCollection(F.USER);
		FindIterable<Document> found = collection.find(new Document(F.DEVICE_ID, deviceId));
		Counters counters = this.getContext().getModelFactory().newModel(Counters.class);

		String prefix = "ID";
		Platform platform = Platform.fromId(platformId);
		if (platform != null) {
			prefix = platform.getAlias();
		}

		IDUser user = new IDUser();

		if (found.first() == null) {
			user.autoUuid();
			user.setUsername(prefix + "_" + counters.generateId(counters.getNextDeviceId()));
			user.setIpAddress(request.getString(F.IP_ADDRESS));
			user.setGender(0);
			user.autoRefreshToken();
			user.setRefreshTokenExpireIn(System.currentTimeMillis() + AccessTokenManager.ACCESS_TOKEN_LIVE_TIME);
			user.generateCustomerId(counters.getNextCustomerId());
			user.setDeviceId(deviceId);
			IDUser oldUser = (IDUser) this.getContext().getHazelcast().getMap(IDUser.ID_USER_MAP_KEY)
					.putIfAbsent(user.getUsername(), user);
			if (oldUser != null) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.USERNAME_EXISTS));
			}
		} else {
			Document document = found.first();
			user.readDocument(document);
		}

		PuObject info = new PuObject();
		info.setString(F.USER_ID, user.getUserId());
		info.setString(F.USERNAME, user.getUsername());
		info.setLong(F.TIMESTAMP, System.currentTimeMillis());

		String signature = null;
		try {
			signature = getContext().signData(info.toJSON());
		} catch (SignDataException e) {
			throw new ExecuteProcessorException(e);
		}

		AccessTokenInfo token = new AccessTokenInfo(user.getUserId(), user.getUsername());
		getContext().getAccessTokenManager().removeByUsername(user.getUsername());
		getContext().getAccessTokenManager().addAccessToken(token);
		PuObject data = new PuObject();
		data.setString(F.ACCESS_TOKEN, token.getAccessToken());
		data.setString(F.REFRESH_TOKEN, user.getRefreshToken());
		data.setLong(F.EXPIRE_IN, token.getLastTouch() + AccessTokenManager.ACCESS_TOKEN_LIVE_TIME);
		data.setPuObject(F.INFO, info);
		data.setString(F.SIGNATURE, signature);
		PuObject result = new PuObject();
		result.setInteger(F.STATUS, Status.SUCCESS.getCode());
		result.setPuObject(F.DATA, data);
		return result;
	}
}
