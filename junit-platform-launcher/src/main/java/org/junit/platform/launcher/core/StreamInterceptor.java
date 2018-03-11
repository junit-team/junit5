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
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Consumer;

class StreamInterceptor extends PrintStream {

	private final PrintStream originalStream;
	private final Consumer<PrintStream> unregisterAction;
	private final int maxNumberOfBytesPerThread;

	private ThreadLocal<Deque<ByteArrayOutputStream>> output = ThreadLocal.withInitial(ArrayDeque::new);

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

	private StreamInterceptor(PrintStream originalStream, Consumer<PrintStream> unregisterAction,
			int maxNumberOfBytesPerThread) {
		super(originalStream);
		this.originalStream = originalStream;
		this.unregisterAction = unregisterAction;
		this.maxNumberOfBytesPerThread = maxNumberOfBytesPerThread;
	}

	void capture() {
		output.get().addFirst(new ByteArrayOutputStream());
	}

	String consume() {
		ByteArrayOutputStream out = output.get().pollFirst();
		return out == null ? "" : out.toString();
	}

	void unregister() {
		unregisterAction.accept(originalStream);
	}

	@Override
	public void write(int b) {
		ByteArrayOutputStream out = output.get().peekFirst();
		if (out != null) {
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
		ByteArrayOutputStream out = output.get().peekFirst();
		if (out != null) {
			int actualLength = Math.max(0, Math.min(len, maxNumberOfBytesPerThread - out.size()));
			if (actualLength > 0) {
				out.write(buf, off, actualLength);
			}
		}
		super.write(buf, off, len);
	}

}
