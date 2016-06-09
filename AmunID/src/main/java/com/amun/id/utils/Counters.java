package com.amun.id.utils;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.hashids.Hashids;

import com.amun.id.statics.F;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.nhb.common.db.models.AbstractModel;

public class Counters extends AbstractModel {
	private static final Hashids hashIds = new Hashids("amunIdSaltKey", 5, "1234567890abcdef");

	public synchronized int getNextCustomerId() {
		MongoCollection<Document> counters = getMongoClient().getDatabase(F.AMUN_ID).getCollection(F.COUNTERS);
		Bson where = new BasicDBObject(F._ID, F.CUSTOMER_ID);
		Bson update = new BasicDBObject("$inc", new BasicDBObject(F.SEQ, 1));
		Document result = counters.findOneAndUpdate(where, update, new FindOneAndUpdateOptions().upsert(true));
		int newId = result.getInteger(F.SEQ, 0);
		return newId;
	}

	public synchronized int getNextDeviceId() {
		MongoCollection<Document> counters = getMongoClient().getDatabase(F.AMUN_ID).getCollection(F.COUNTERS);
		Bson where = new BasicDBObject(F._ID, F.DEVICE_ID);
		Bson update = new BasicDBObject("$inc", new BasicDBObject(F.SEQ, 1));
		Document result = counters.findOneAndUpdate(where, update, new FindOneAndUpdateOptions().upsert(true));
		int newId = result.getInteger(F.SEQ, 0);
		return newId;
	}

	public int generateId(int seq) {
		String encode = hashIds.encode(seq);
		return Integer.parseInt(encode, 16);
	}
}
