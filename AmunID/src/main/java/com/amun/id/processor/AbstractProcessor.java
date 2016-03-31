package com.amun.id.processor;

import com.amun.id.UserHandler;
import com.amun.id.exception.ExecuteProcessorException;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public abstract class AbstractProcessor extends BaseLoggable {

	public abstract PuElement execute(PuObjectRO request) throws ExecuteProcessorException;

	private UserHandler context;

	public UserHandler getContext() {
		return context;
	}

	public void setContext(UserHandler context) {
		this.context = context;
	}

}
