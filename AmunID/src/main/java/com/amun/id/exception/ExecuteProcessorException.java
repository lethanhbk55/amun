package com.amun.id.exception;

public class ExecuteProcessorException extends Exception {

	private static final long serialVersionUID = 2493944017731568441L;

	public ExecuteProcessorException(String meessage) {
		super(meessage);
	}

	public ExecuteProcessorException(Throwable e) {
		super("execute processor error", e);
	}
}
