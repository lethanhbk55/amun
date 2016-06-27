package com.amun.id.bean;

import org.bson.Document;

import com.amun.id.statics.F;
import com.nhb.common.db.beans.AbstractBean;

public class BadWordBean extends AbstractBean {

	private static final long serialVersionUID = -3484636224565601212L;

	private String word;

	public void writeDocument(Document document) {
		document.put(F.WORD, this.word);
	}

	public void readDocument(Document doc) {
		this.word = doc.getString(F.WORD);
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}
