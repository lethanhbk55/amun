package com.amun.id.statics;

public enum RegisterType {
	NORMAL(1),
	QUICK_PLAY(2),
	FACEBOOK(3);

	private int id;

	private RegisterType(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}
}
