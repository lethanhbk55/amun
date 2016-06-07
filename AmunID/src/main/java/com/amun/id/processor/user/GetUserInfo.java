package com.amun.id.processor.user;

import org.bson.Document;

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

@CommandProcessor(command = { "getAmunUserInfo" })
public class GetUserInfo extends AbstractProcessor {

	@Override
	public PuElement execute(PuObjectRO request) throws ExecuteProcessorException {
		String username = request.getString(F.USERNAME);
		MongoCollection<Document> user = getContext().getDatabase().getCollection(F.USER);
		FindIterable<Document> found = user.find(new Document(F.USERNAME, username));
		if (found.first() != null) {
			Document document = found.first();
			PuObject data = new PuObject();
			data.setString(F.USERNAME, username);
			data.setString(F.USER_ID, document.getString(F.USER_ID));
			data.setString(F.DISPLAY_NAME, document.getString(F.DISPLAY_NAME));
			String avatar = "";
			if (document.containsKey(F.AVATAR)) {
				avatar = document.getString(F.AVATAR);
			}
			data.setString(F.AVATAR, avatar);
			data.setInteger(F.GENDER, document.getInteger(F.GENDER, 0));
			PuObject result = new PuObject();
			result.setInteger(F.STATUS, Status.SUCCESS.getCode());
			result.setPuObject(F.DATA, data);
			return result;
		}
		return PuObject.fromObject(new MapTuple<>(F.STATUS, Status.UNKNOWN.getCode()));
	}

}
