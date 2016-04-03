package com.amun.id.processor.user;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.amun.id.utils.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.encrypt.rsa.KeyPairHelper;
import com.nhb.common.encrypt.rsa.SignatureHelper;
import com.nhb.common.encrypt.sha.SHAEncryptor;

@CommandProcessor(command = { "changePassword", "changePass" })
public class ChangePasswordProcessor extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		int status = 1;
		String message = "";

		if (request.variableExists(F.AUTHENTICATOR_ID) && request.variableExists(F.SIGNATURE)
				&& request.variableExists(F.INFO)) {
			String authenticatorId = request.getString(F.AUTHENTICATOR_ID);
			String signature = request.getString(F.SIGNATURE);
			PuObject info = request.getPuObject(F.INFO);

			MongoCollection<Document> collection = getContext().getDatabase().getCollection(F.AUTHENTICATOR);
			Document document = collection.find(new BasicDBObject(F.AUTHENTICATOR_ID, authenticatorId)).first();
			if (document != null) {
				KeyPairHelper keyPairHelper = new KeyPairHelper();
				Binary binary = (Binary) document.get(F.PUBLIC_KEY);
				ByteArrayInputStream inputStream = new ByteArrayInputStream(binary.getData());
				try {
					keyPairHelper.loadPublicKey(inputStream);
				} catch (IOException | GeneralSecurityException e) {
					throw new ExecuteProcessorException(e);
				}
				String newPassword = info.getString(F.NEW_PASSWORD);
				String username = info.getString(F.USERNAME);
				SignatureHelper signatureHelper = new SignatureHelper();
				signatureHelper.setKeyPairHelper(keyPairHelper);
				try {
					boolean verify = signatureHelper.verify(info.toJSON(), signature);
					if (verify) {
						MongoCollection<Document> userCollection = getContext().getDatabase().getCollection(F.USER);
						Document found = userCollection.find(new BasicDBObject(F.USERNAME, username)).first();
						if (found != null) {
							ObjectId id = found.getObjectId(F._ID);
							String salt = StringUtils.randomString(8);
							String password = SHAEncryptor.sha512Hex(newPassword + salt);
							Bson where = new BasicDBObject(F._ID, id);
							Bson set = new BasicDBObject("$set", new BasicDBObject(F.SALT, salt)
									.append(F.PASSWORD, password).append(F.LAST_MODIFY, System.currentTimeMillis()));
							UpdateOptions options = new UpdateOptions();
							options.upsert(true);
							long modifiedCount = userCollection.updateOne(where, set, options).getModifiedCount();
							if (modifiedCount > 0) {
								status = 0;
								message = "change password successful";
							} else {
								status = 304;
								message = "change password uncorrected";
							}
						} else {
							status = 301;
							message = "username hasn't been register";
						}
					} else {
						status = 302;
						message = "signature is invalid";
					}
				} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
					throw new ExecuteProcessorException(e);
				}
			} else {
				status = 303;
				message = "authenticatorId cannot be found";
			}
		} else {
			message = "paramter're missing";
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, status, F.MESSAGE, message));
	}

}
