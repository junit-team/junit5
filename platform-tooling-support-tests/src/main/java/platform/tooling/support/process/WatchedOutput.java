/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.process;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class WatchedOutput {

	private static final Charset CHARSET = Charset.forName(System.getProperty("native.encoding"));

	private final Thread thread;
	private final ByteArrayOutputStream stream;

	WatchedOutput(Thread thread, ByteArrayOutputStream stream) {
		this.thread = thread;
		this.stream = stream;
	}

	void join() throws InterruptedException {
		thread.join();
	}

	public String getStreamAsString() {
		return stream.toString(CHARSET);
	}
}
