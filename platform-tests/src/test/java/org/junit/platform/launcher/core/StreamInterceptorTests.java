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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class StreamInterceptorTests {

	private PrintStream targetStream = new PrintStream(new ByteArrayOutputStream());

	@Test
	void interceptsWriteOperationsToStreamPerThread() {
		StreamInterceptor streamInterceptor = StreamInterceptor.register(targetStream,
			newStream -> this.targetStream = newStream, 3).orElseThrow(RuntimeException::new);
		try {
			// @formatter:off
			IntStream.range(0, 1000)
					.parallel()
					.peek(i -> targetStream.println(i))
					.mapToObj(String::valueOf)
					.peek(i -> streamInterceptor.capture())
					.peek(i -> targetStream.println(i))
					.forEach(i -> assertEquals(i, streamInterceptor.consume().trim()));
			// @formatter:on
		}
		finally {
			streamInterceptor.unregister();
		}
	}

	@Test
	void handlesNestedCaptures() {
		StreamInterceptor streamInterceptor = StreamInterceptor.register(targetStream,
			newStream -> this.targetStream = newStream, 100).orElseThrow(RuntimeException::new);

		String outermost, inner, innermost;

		streamInterceptor.capture();
		streamInterceptor.print("before outermost - ");
		{
			streamInterceptor.capture();
			streamInterceptor.print("before inner - ");
			{
				streamInterceptor.capture();
				streamInterceptor.print("innermost");
				innermost = streamInterceptor.consume();
			}
			streamInterceptor.print("after inner");
			inner = streamInterceptor.consume();
		}
		streamInterceptor.print("after outermost");
		outermost = streamInterceptor.consume();

		assertAll(//
			() -> assertEquals("before outermost - after outermost", outermost), //
			() -> assertEquals("before inner - after inner", inner), //
			() -> assertEquals("innermost", innermost) //
		);
	}
}
