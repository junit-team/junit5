/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;

/**
 * @since 5.5
 */
class UninterruptibleInvocation implements Invocation<Void> {

	private final long duration;
	private final TimeUnit unit;

	UninterruptibleInvocation(long duration, TimeUnit unit) {
		this.duration = duration;
		this.unit = unit;
	}

	@Override
	public Void proceed() {
		long startTime = System.nanoTime();
		while (true) {
			assertThat(IntStream.range(1, 1_000_000).sum()).isGreaterThan(0);
			long elapsedTime = System.nanoTime() - startTime;
			if (elapsedTime > NANOSECONDS.convert(duration, unit)) {
				return null;
			}
		}
	}

}
