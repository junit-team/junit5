/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

import org.junit.platform.commons.JUnitException;

class StreamInterceptor extends PrintStream {

	private final PrintStream originalStream;
	private final Consumer<PrintStream> unregisterAction;
	private final int maxNumberOfBytesPerThread;

	private ThreadLocal<ByteArrayOutputStream> output = ThreadLocal.withInitial(ByteArrayOutputStream::new);
	private ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);

	static StreamInterceptor registerStdout(int maxNumberOfBytesPerThread) {
		return register(System.out, System::setOut, maxNumberOfBytesPerThread);
	}

	static StreamInterceptor registerStderr(int maxNumberOfBytesPerThread) {
		return register(System.err, System::setErr, maxNumberOfBytesPerThread);
	}

	static StreamInterceptor register(PrintStream originalStream, Consumer<PrintStream> streamSetter,
			int maxNumberOfBytesPerThread) {
		if (originalStream instanceof StreamInterceptor) {
			throw new JUnitException(StreamInterceptor.class.getName() + " is already registered");
		}
		StreamInterceptor interceptor = new StreamInterceptor(originalStream, streamSetter, maxNumberOfBytesPerThread);
		streamSetter.accept(interceptor);
		return interceptor;
	}

	StreamInterceptor(PrintStream originalStream, Consumer<PrintStream> unregisterAction,
			int maxNumberOfBytesPerThread) {
		super(originalStream);
		this.originalStream = originalStream;
		this.unregisterAction = unregisterAction;
		this.maxNumberOfBytesPerThread = maxNumberOfBytesPerThread;
	}

	void capture() {
		active.set(true);
	}

	void consume(OutputStream out) throws IOException {
		if (active.get()) {
			ByteArrayOutputStream threadOutput = output.get();
			threadOutput.writeTo(out);
			threadOutput.reset();
			active.set(false);
		}
	}

	void unregister() {
		unregisterAction.accept(originalStream);
	}

	@Override
	public void write(int b) {
		if (active.get()) {
			ByteArrayOutputStream out = output.get();
			if (out.size() < maxNumberOfBytesPerThread) {
				out.write(b);
			}
		}
		super.write(b);
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		if (active.get()) {
			ByteArrayOutputStream out = output.get();
			int actualLength = Math.max(0, Math.min(len, maxNumberOfBytesPerThread - out.size()));
			if (actualLength > 0) {
				out.write(buf, off, actualLength);
			}
		}
		super.write(buf, off, len);
	}

}
