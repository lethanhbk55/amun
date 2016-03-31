package com.amun.id.processor.user;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.utils.StringUtils;
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

		if (request.variableExists(F.USERNAME) && request.variableExists(F.PASSWORD) && request.variableExists(F.PHONE)
				&& request.variableExists(F.IP_ADDRESS)) {
			String username = request.getString(F.USERNAME);
			String password = request.getString(F.PASSWORD);
			String phone = request.getString(F.PHONE);
			String ipAddress = request.getString(F.IP_ADDRESS);

			if (!StringUtils.isValidPhone(phone)) {
				status = 200;
				message = "phone number is invalid";
			} else if (!StringUtils.isValidIPAdress(ipAddress) && !allowIPs.contains(ipAddress)) {
				status = 201;
				message = "ip address `" + ipAddress + "` is invalid";
			} else {
				Document user = new Document();
				user.put(F.USERNAME, username);
				try {
					long count = getContext().getDatabase().getCollection(F.USER).count(user);
					if (count > 0) {
						status = 202;
						message = "user `" + username + "` was already exists";
						return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.MESSAGE, message));
					}
				} catch (Exception e) {
					throw new ExecuteProcessorException(e);
				}

				String salt = StringUtils.randomString(8);
				String userId = UUID.randomUUID().toString();

				Document document = new Document();
				document.put(F.USER_ID, userId);
				document.put(F.USERNAME, username);
				document.put(F.SALT, salt);
				document.put(F.PASSWORD, SHAEncryptor.sha512Hex(password + salt));
				document.put(F.CREATED_TIME, System.currentTimeMillis());
				document.put(F.IP_ADDRESS, request.getString(F.IP_ADDRESS));

				try {
					getContext().getDatabase().getCollection(F.USER).insertOne(document);
				} catch (Exception e) {
					throw new ExecuteProcessorException(e);
				}

				if (alsoLogin) {
					PuObject info = new PuObject();
					info.setString(F.USER_ID, userId);
					info.setString(F.USERNAME, username);
					info.setLong(F.TIMESTAMP, System.currentTimeMillis());

					try {
						String signature = Base64.getEncoder()
								.encodeToString(getContext().getSignatureHelper().sign(info.toJSON()));
						return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.MESSAGE, "register successful", F.INFO,
								info, F.SIGNATURE, signature));
					} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
						throw new ExecuteProcessorException(e);
					}
				}

				status = 0;
				message = "register successful";
			}
		}

		return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.MESSAGE, message));
	}

}
