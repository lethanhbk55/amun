package com.amun.id.processor.user;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;

import org.bson.Document;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
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
				String salt = userDocument.getString(F.SALT);
				String hash = SHAEncryptor.sha512Hex(password + salt);

				if (hash.equals(userDocument.getString(F.PASSWORD))) {
					PuObject info = new PuObject();
					info.setString(F.USERNAME, userDocument.getString(F.USERNAME));
					info.setString(F.USER_ID, userDocument.getString(F.USER_ID));
					info.setLong(F.TIMESTAMP, System.currentTimeMillis());

					String signature;
					try {
						signature = Base64.getEncoder()
								.encodeToString(getContext().getSignatureHelper().sign(info.toJSON()));
						return PuObject.fromObject(new MapTuple<>(F.STATUS, 0, F.INFO, info, F.SIGNATURE, signature,
								F.MESSAGE, "login successful"));
					} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
						throw new ExecuteProcessorException(e);
					}
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

		return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.MESSAGE, message));
	}

}
