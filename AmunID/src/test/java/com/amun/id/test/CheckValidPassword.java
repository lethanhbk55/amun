package com.amun.id.test;

import com.amun.id.utils.StringUtils;

public class CheckValidPassword {

	public static void main(String[] args) {
		boolean valid = StringUtils.containsLetterAndDigit("123456");
		System.out.println(valid);
	}
}
