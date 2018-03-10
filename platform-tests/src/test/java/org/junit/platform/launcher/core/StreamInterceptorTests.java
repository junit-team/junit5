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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class StreamInterceptorTests {

	PrintStream targetStream = new PrintStream(new ByteArrayOutputStream());

	@Test
	void interceptsWriteOperationsToStreamPerThread() {
		StreamInterceptor streamInterceptor = StreamInterceptor.register(targetStream,
			newStream -> this.targetStream = newStream, 3);
		try {
			// @formatter:off
			IntStream.range(0, 1000)
					.parallel()
					.peek(i -> targetStream.println(i))
					.mapToObj(String::valueOf)
					.peek(i -> streamInterceptor.capture())
					.peek(i -> targetStream.println(i))
					.forEach(i -> {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						try {
							streamInterceptor.consume(out);
						} catch (IOException e) {
							throw new RuntimeException("Could not consume stream", e);
						}
						String output = out.toString().trim();
						assertEquals(i, output);
					});
			// @formatter:on
		}
		finally {
			streamInterceptor.unregister();
		}
	}
}
