package com.amun.id.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.amun.id.statics.F;
import com.amun.id.user.IDUser;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.nhb.common.db.models.AbstractModel;

public class UserModel extends AbstractModel {

	public IDUser findByRefreshToken(String refreshToken) {
		Document doc = new Document(F.REFRESH_TOKEN, refreshToken);
		MongoCollection<Document> collection = getCollection();
		FindIterable<Document> found = collection.find(doc);
		if (found.first() != null) {
			IDUser user = new IDUser();
			user.readDocument(found.first());
			return user;
		}
		return null;
	}

	private MongoCollection<Document> getCollection() {
		return getMongoClient().getDatabase(F.AMUN_ID).getCollection(F.USER);
	}

	public int countDeviceId(String deviceId) {
		Document doc = new Document(F.DEVICE_ID, deviceId);
		return (int) getCollection().count(doc);
	}

	public List<IDUser> findByPaging(int skip, int limit) {
		FindIterable<Document> found = getCollection().find().limit(limit).skip(skip).sort(new Document(F._ID, -1));
		List<IDUser> users = new ArrayList<>();
		found.forEach((Block<Document>) x -> {
			IDUser user = new IDUser();
			user.readDocument(x);
			users.add(user);
		});
		return users;
	}

	public long count() {
		return getCollection().count();
	}
}
