package com.amun.id.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.RandomStringUtils;

public class StringUtils {
	public static final String SALT_SAMPLE = "}|:'vwxyzABCDabcNOu345enopkl90~!@#$%mWXYZ6*()d12<>?IJKLM_+G78^&PQRSqrstH{EFTUVfghij";
	public static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	public static boolean isValidPhone(String phone) {
		Pattern pattern = Pattern.compile("\\d{10,11}");
		Matcher matcher = pattern.matcher(phone);
		if (!matcher.matches()) {
			System.out.println("phone is not match");
			return false;
		}

		if (phone.startsWith("09") && phone.length() == 10) {
			return true;
		}

		if (phone.startsWith("01") && phone.length() == 11) {
			return true;
		}

		return false;
	}

	public static boolean isValidIPAdress(String ipAddress) {
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	public static String randomString(int length) {
		return RandomStringUtils.random(length, SALT_SAMPLE);
	}

	public static boolean containsLetterAndDigit(String text) {
		boolean hasDigit = text.matches(".*\\d+.*");
		boolean hasLetter = text.matches(".*\\[a-zA-Z]+.*");
		return hasDigit && hasLetter;
	}
}
