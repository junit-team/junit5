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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.platform.commons.JUnitException;

class StreamInterceptor extends PrintStream {

	private final PrintStream originalStream;
	private final Consumer<PrintStream> unregisterAction;
	private final int maxNumberOfBytesPerThread;

	private ThreadLocal<ByteArrayOutputStream> output = ThreadLocal.withInitial(ByteArrayOutputStream::new);
	private ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);

	static Optional<StreamInterceptor> registerStdout(int maxNumberOfBytesPerThread) {
		return register(System.out, System::setOut, maxNumberOfBytesPerThread);
	}

	static Optional<StreamInterceptor> registerStderr(int maxNumberOfBytesPerThread) {
		return register(System.err, System::setErr, maxNumberOfBytesPerThread);
	}

	static Optional<StreamInterceptor> register(PrintStream originalStream, Consumer<PrintStream> streamSetter,
			int maxNumberOfBytesPerThread) {
		if (originalStream instanceof StreamInterceptor) {
			return Optional.empty();
		}
		StreamInterceptor interceptor = new StreamInterceptor(originalStream, streamSetter, maxNumberOfBytesPerThread);
		streamSetter.accept(interceptor);
		return Optional.of(interceptor);
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

	String consume() {
		if (active.get()) {
			ByteArrayOutputStream threadOutput = output.get();
			active.set(false);
			try {
				return threadOutput.toString(UTF_8.toString());
			}
			catch (UnsupportedEncodingException e) {
				throw new JUnitException("UTF_8 apparently isn't a supported encoding", e);
			}
			finally {
				threadOutput.reset();
			}
		}
		return "";
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
