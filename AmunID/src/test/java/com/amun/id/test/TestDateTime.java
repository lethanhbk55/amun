package com.amun.id.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestDateTime {

	public static void main(String[] args) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.add(Calendar.DATE, 90);
		long timstamp = calendar.getTimeInMillis();

		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date(timstamp);
		System.out.println(format.format(date));
	}
}
