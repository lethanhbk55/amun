package com.amun.id.test;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.amun.id.annotation.AnnotationLoader;

import junit.framework.TestCase;

public class TestAnnotationLoader extends TestCase {

	@Test
	public void testLoadCommandAnnotation() {
		Map<String, Class<?>> commandRouting = AnnotationLoader.getInstance().load();
		for (Entry<String, Class<?>> entry : commandRouting.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue().getName());
		}
	}
}
