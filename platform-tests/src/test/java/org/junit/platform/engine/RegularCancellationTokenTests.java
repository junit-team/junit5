/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.test.ConcurrencyTestingUtils.executeConcurrently;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegularCancellationTokenTests {

	CancellationToken token = new RegularCancellationToken();

	@Test
	void newTokenIsNotCancelled() {
		assertThat(token.isCancellationRequested()).isFalse();
	}

	@Test
	void listenerIsNotifiedWhenTokenIsCancelled() {
		CancellationToken.Listener listener = mock();

		token.addListener(listener);
		token.cancel();

		verify(listener).cancellationRequested(same(token));
	}

	@Test
	void listenerIsNotifiedOfPriorCancellation() {
		CancellationToken.Listener listener = mock();

		token.cancel();
		token.addListener(listener);

		verify(listener).cancellationRequested(same(token));
	}

	@Test
	void removedListenersAreNotNotifiedOfCancellation() {
		CancellationToken.Listener listener = mock();

		token.addListener(listener);
		token.removeListener(listener);
		token.cancel();

		verifyNoInteractions(listener);
	}

	@Test
	void registrationIsThreadSafe() throws Exception {
		int numThreads = 100;
		var listeners = executeConcurrently(numThreads, () -> {
			var listener = mock(CancellationToken.Listener.class);
			token.addListener(listener);
			Thread.sleep(1);
			token.removeListener(listener);
			return listener;
		});

		token.cancel();

		assertThat(listeners).hasSize(numThreads);
		listeners.forEach(Mockito::verifyNoInteractions);
	}

	@Test
	void cancellationIsThreadSafe() throws Exception {
		int numThreads = 100;
		var cancellationCounter = new AtomicInteger(numThreads / 2);
		var cancelled = new AtomicBoolean();
		var listeners = executeConcurrently(numThreads, () -> {
			var listener = mock(CancellationToken.Listener.class);
			var value = cancellationCounter.decrementAndGet();
			if (value == 0) {
				token.cancel();
				cancelled.set(true);
			}
			if (cancelled.get()) {
				assertTrue(token.isCancellationRequested());
			}
			token.addListener(listener);
			return listener;
		});

		assertThat(listeners).hasSize(numThreads);
		listeners.forEach(listener -> verify(listener).cancellationRequested(same(token)));
	}
}
