package com.amun.id.processor.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.amun.id.accesstoken.AccessTokenInfo;
import com.amun.id.accesstoken.AccessTokenManager;
import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.exception.SignDataException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.statics.RegisterType;
import com.amun.id.statics.Status;
import com.amun.id.user.IDUser;
import com.amun.id.utils.Counters;
import com.amun.id.utils.StringUtils;
import com.hazelcast.core.IMap;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.sha.SHAEncryptor;

@CommandProcessor(command = { "reg", "register" })
public class RegisterProcessor extends AbstractProcessor {
	private List<String> allowIPs = Arrays.asList(new String[] { "0:0:0:0:0:0:0:1", "localhost" });

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		int status = 1;
		String message = "paramter's missing";
		boolean alsoLogin = request.getBoolean(F.ALSO_LOGIN, false);
		IMap<String, IDUser> mapstore = getContext().getHazelcast().getMap(IDUser.ID_USER_MAP_KEY);
		Counters counters = getContext().getModelFactory().newModel(Counters.class);
		MongoCollection<Document> collection = getContext().getDatabase().getCollection(F.USER);

		if (request.variableExists(F.USERNAME) && request.variableExists(F.PASSWORD)
				&& request.variableExists(F.IP_ADDRESS) && request.variableExists(F.DEVICE_ID)
				&& request.variableExists(F.PLATFORM_ID)) {
			String username = request.getString(F.USERNAME);
			String password = request.getString(F.PASSWORD);
			String ipAddress = request.getString(F.IP_ADDRESS);
			String deviceId = request.getString(F.DEVICE_ID);
			int platformId = request.getInteger(F.PLATFORM_ID);

			if (username.length() < 6 || username.length() > 15) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.NAME_INVALID.getCode()));
			}

			MongoCollection<Document> illegalCollection = getContext().getDatabase().getCollection(F.BAD_WORD);
			FindIterable<Document> found = illegalCollection.find();

			List<String> words = new ArrayList<>();
			for (Document document : found) {
				words.add(document.getString(F.WORD));
			}

			for (String word : words) {
				if (username.contains(word)) {
					return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.BAD_WORD_FILTER));
				}
			}

			if (!StringUtils.containsLetterAndDigit(password) || password.length() < 6 || password.length() > 15) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.PASSWORD_INVALID.getCode()));
			}

			if (!StringUtils.isValidIPAdress(ipAddress) && !allowIPs.contains(ipAddress)) {
				status = Status.INVALID_IP_ADDRESS.getCode();
				message = "ip address `" + ipAddress + "` is invalid";
			} else {
				int imeiCount = (int) collection.count(new Document(F.DEVICE_ID, deviceId));
				String salt = StringUtils.randomString(8);
				String userId = UUID.randomUUID().toString();
				String refreshToken = UUID.randomUUID().toString() + "." + UUID.randomUUID().toString();
				refreshToken = refreshToken.replace("-", "");
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.add(Calendar.DATE, 90);
				long refreshTokenExpireIn = calendar.getTimeInMillis();

				IDUser user = new IDUser();
				user.setUserId(userId);
				user.setUsername(username);
				user.setSalt(salt);
				user.setPassword(SHAEncryptor.sha512Hex(password + salt));
				user.setRefreshToken(refreshToken);
				user.setIpAddress(ipAddress);
				user.setRefreshTokenExpireIn(refreshTokenExpireIn);
				user.setRegTime(System.currentTimeMillis());
				user.setDeviceId(deviceId);
				user.setPlatformId(platformId);
				user.setImeiCount(imeiCount);
				user.setRegisterType(RegisterType.NORMAL.getId());
				user.setOs(request.getString(F.OS, ""));

				IDUser oldUser = mapstore.putIfAbsent(user.getUsername(), user);
				if (oldUser != null) {
					status = Status.USERNAME_EXISTS.getCode();
					message = "user `" + username + "` was already exists";
					return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.DATA, message));
				}
				user.generateCustomerId(counters.getNextCustomerId());
				mapstore.replace(user.getUsername(), user);

				if (alsoLogin) {
					PuObject info = new PuObject();
					info.setString(F.USER_ID, userId);
					info.setString(F.USERNAME, username);
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

					PuObject result = new PuObject();
					result.setInteger(F.STATUS, Status.SUCCESS.getCode());

					PuObject data = new PuObject();
					data.setString(F.ACCESS_TOKEN, token.getAccessToken());
					data.setString(F.REFRESH_TOKEN, refreshToken);
					data.setString(F.EXPIRE_IN, token.getAccessToken() + AccessTokenManager.ACCESS_TOKEN_LIVE_TIME);
					data.setPuObject(F.INFO, info);
					data.setString(F.SIGNATURE, signature);
					result.setPuObject(F.DATA, data);
					return result;
				}

				status = 0;
				message = "register successful";
			}
		}

		return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.DATA, message));
	}

}
