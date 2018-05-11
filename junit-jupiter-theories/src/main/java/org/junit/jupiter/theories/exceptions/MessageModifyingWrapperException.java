/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.exceptions;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apiguardian.api.API;

/**
 * Special wrapper exception used to modify the message of an exception without modifying its stack trace.
 */
@API(status = INTERNAL, since = "5.3")
public class MessageModifyingWrapperException extends Throwable {
	private static final long serialVersionUID = -5452185844058139L;

	private final String additionalDetails;
	private final Throwable wrapped;

	/**
	 * Constructor.
	 *
	 * @param additionalDetails additional details to prepend to the wrapped exception's message
	 * @param wrapped the wrapped exception
	 */
	public MessageModifyingWrapperException(String additionalDetails, Throwable wrapped) {
		this.additionalDetails = additionalDetails;
		this.wrapped = wrapped;
	}

	@Override
	public String getMessage() {
		return additionalDetails + wrapped.getMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return additionalDetails + wrapped.getLocalizedMessage();
	}

	@Override
	public synchronized Throwable getCause() {
		return wrapped.getCause();
	}

	@Override
	public String toString() {
		return additionalDetails + wrapped.toString();
	}

	@Override
	public void printStackTrace() {
		wrapped.printStackTrace();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		wrapped.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		wrapped.printStackTrace(s);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return wrapped.getStackTrace();
	}

	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {
		wrapped.setStackTrace(stackTrace);
	}
}
