package com.amun.id.test;

import com.amun.id.utils.StringUtils;

import junit.framework.TestCase;

public class ValidatePhoneNumberTestCase extends TestCase {

	public void testValidPhoneNumber() {
		assertEquals(true, StringUtils.isValidPhone("0987886025"));
		assertEquals(true, StringUtils.isValidPhone("01234567891"));

		assertEquals(false, StringUtils.isValidPhone("09878860251"));
		assertEquals(false, StringUtils.isValidPhone("0123456789"));
		assertEquals(false, StringUtils.isValidPhone("0342sfdjs1"));
		assertEquals(false, StringUtils.isValidPhone("098788602a"));
		assertEquals(false, StringUtils.isValidPhone("@#$%^&*()"));

	}

	public void testValidIpAddress() {
		assertEquals(true, StringUtils.isValidIPAdress("0.0.0.0"));
		assertEquals(true, StringUtils.isValidIPAdress("127.0.0.1"));
		assertEquals(true, StringUtils.isValidIPAdress("192.168.1.1"));
		assertEquals(true, StringUtils.isValidIPAdress("255.255.255.255"));
		assertEquals(false, StringUtils.isValidIPAdress("255.255.255.256"));
		assertEquals(false, StringUtils.isValidIPAdress("ládflkdsfjs"));
	}
}
