package com.amun.id.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

public class AnnotationLoader {
	private static final String BASE_PACKAGE_NAME = "com.amun.id";
	private static AnnotationLoader instance;

	private AnnotationLoader() {

	}

	public static AnnotationLoader getInstance() {
		if (instance == null) {
			synchronized (AnnotationLoader.class) {
				if (instance == null) {
					instance = new AnnotationLoader();
				}
			}
		}
		return instance;
	}

	public Map<String, Class<?>> load() {
		Map<String, Class<?>> commandRouting = new HashMap<>();
		Reflections reflections = new Reflections(BASE_PACKAGE_NAME);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CommandProcessor.class);
		for (Class<?> clazz : classes) {
			CommandProcessor commands = clazz.getAnnotation(CommandProcessor.class);
			String[] values = commands.command();
			for (String value : values) {
				commandRouting.put(value, clazz);
			}
		}
		return commandRouting;
	}
}
