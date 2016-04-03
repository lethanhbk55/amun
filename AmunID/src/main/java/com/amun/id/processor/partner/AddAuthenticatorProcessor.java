package com.amun.id.processor.partner;

import java.util.UUID;

import org.bson.Document;

import com.amun.id.annotation.CommandProcessor;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.amun.id.statics.F;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

@CommandProcessor(command = { "addAuthenticator" })
public class AddAuthenticatorProcessor extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		if (request.variableExists(F.PARTNER_NAME) && request.variableExists(F.PUBLIC_KEY)) {
			String partnerName = request.getString(F.PARTNER_NAME);
			// String publicKeyBase64 = request.getString(F.PUBLIC_KEY);

			MongoCollection<Document> collection = getContext().getDatabase().getCollection(F.AUTHENTICATOR);
			long count = collection.count(new BasicDBObject(F.PARTNER_NAME, partnerName));
			if (count > 0) {
				return PuObject.fromObject(new MapTuple<>(F.STATUS, 300, F.MESSAGE, "partner name already exists"));
			}

			String authenticatorId = UUID.randomUUID().toString();
			byte[] publicKey = request.getRaw(F.PUBLIC_KEY);

			Document document = new Document();
			document.put(F.AUTHENTICATOR_ID, authenticatorId);
			document.put(F.PARTNER_NAME, partnerName);
			document.put(F.PUBLIC_KEY, publicKey);

			collection.insertOne(document);

			return PuObject.fromObject(
					new MapTuple<>(F.STATUS, 0, F.MESSAGE, "register succesful", F.AUTHENTICATOR_ID, authenticatorId));
		}
		return new PuValue("paramter're missing");
	}

}
