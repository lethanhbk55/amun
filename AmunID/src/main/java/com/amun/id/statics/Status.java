package com.amun.id.statics;

public enum Status {
	SUCCESS(0),
	UNKNOWN(1),
	REFRESH_TOKEN_NOT_FOUND(400),
	INVALID_IP_ADDRESS(201),
	USERNAME_EXISTS(202),
	DISPLAY_NAME_EXISTS(203),
	BAD_WORD_FILTER(204),
	PASSWORD_INVALID(205),
	NAME_INVALID(206);

	private int code;

	private Status(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}
