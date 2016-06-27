package com.amun.id.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.amun.id.bean.BadWordBean;
import com.amun.id.statics.F;
import com.nhb.common.db.models.AbstractModel;

public class BadWordModel extends AbstractModel {

	public void insert(List<BadWordBean> beans) {
		List<Document> docs = new ArrayList<>();
		for (BadWordBean bean : beans) {
			Document doc = new Document();
			bean.writeDocument(doc);
			docs.add(doc);
		}
		getMongoClient().getDatabase(F.AMUN_ID).getCollection(F.BAD_WORD).insertMany(docs);
	}
}
