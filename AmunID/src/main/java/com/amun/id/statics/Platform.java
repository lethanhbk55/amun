package com.amun.id.statics;

public enum Platform {
	IOS(1, "IP"),
	Android(2, "AD"),
	WP(3, "WP"),
	Web(4, "PC");

	private int id;
	private String alias;

	private Platform(int id, String alias) {
		this.id = id;
		this.alias = alias;
	}

	public int getId() {
		return this.id;
	}

	public String getAlias() {
		return this.alias;
	}

	public static Platform fromId(int id) {
		for (Platform platfrom : values()) {
			if (platfrom.getId() == id) {
				return platfrom;
			}
		}
		return null;
	}
}
