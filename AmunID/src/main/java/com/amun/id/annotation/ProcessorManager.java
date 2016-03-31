package com.amun.id.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amun.id.UserHandler;
import com.amun.id.exception.CommandNotFoundException;
import com.amun.id.exception.ExecuteProcessorException;
import com.amun.id.processor.AbstractProcessor;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public class ProcessorManager {
	private Map<String, AbstractProcessor> processors = new HashMap<>();

	public void init(UserHandler context) throws Exception {
		Map<String, Class<?>> commandRouting = AnnotationLoader.getInstance().load();
		for (Entry<String, Class<?>> entry : commandRouting.entrySet()) {
			Object instance = entry.getValue().newInstance();
			if (instance instanceof AbstractProcessor) {
				((AbstractProcessor) instance).setContext(context);
				processors.put(entry.getKey(), (AbstractProcessor) instance);
			}
		}
	}

	public PuElement processCommand(String command, PuObjectRO request)
			throws CommandNotFoundException, ExecuteProcessorException {
		if (processors.containsKey(command)) {
			return processors.get(command).execute(request);
		}
		throw new CommandNotFoundException();
	}
}
