/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.engine.extension.TimeoutInvocationFactory.ExecutorResource;
import org.junit.jupiter.engine.extension.TimeoutInvocationFactory.TimeoutInvocationParameters;

@DisplayName("TimeoutInvocationFactory")
class TimeoutInvocationFactoryTest {

	private final Store store = spy(new NamespaceAwareStore(new ExtensionValuesStore(null),
		ExtensionContext.Namespace.create(TimeoutInvocationFactoryTest.class)));
	private final Invocation<String> invocation = mock(InvocationWithStringResult.class);
	private final TimeoutDuration timeoutDuration = mock(TimeoutDuration.class);
	private TimeoutInvocationFactory timeoutInvocationFactory;
	private TimeoutInvocationParameters<String> timeoutInvocationParameters;

	@BeforeEach
	void setUp() {
		;
		timeoutInvocationParameters = new TimeoutInvocationParameters<>(invocation, timeoutDuration,
			() -> "description");
		timeoutInvocationFactory = new TimeoutInvocationFactory(store);
	}

	@Test
	@DisplayName("throws exception when null store is provided on create")
	void shouldThrowExceptionWhenInstantiatingWithNullStore() {
		assertThatThrownBy(() -> new TimeoutInvocationFactory(null)).hasMessage("store must not be null");
	}

	@Test
	@DisplayName("throws exception when null timeout thread mode is provided on create")
	void shouldThrowExceptionWhenNullTimeoutThreadModeIsProvidedWhenCreate() {
		assertThatThrownBy(() -> timeoutInvocationFactory.create(null, timeoutInvocationParameters)).hasMessage(
			"thread mode must not be null");
	}

	@Test
	@DisplayName("throws exception when null timeout invocation parameters is provided on create")
	void shouldThrowExceptionWhenNullTimeoutInvocationParametersIsProvidedWhenCreate() {
		assertThatThrownBy(() -> timeoutInvocationFactory.create(ThreadMode.SAME_THREAD, null)).hasMessage(
			"timeout invocation parameters must not be null");
	}

	@Test
	@DisplayName("creates timeout invocation for SAME_THREAD thread mode")
	void shouldCreateTimeoutInvocationForSameThreadTimeoutThreadMode() {
		Invocation<String> invocation = timeoutInvocationFactory.create(ThreadMode.SAME_THREAD,
			timeoutInvocationParameters);
		assertThat(invocation).isInstanceOf(SameThreadTimeoutInvocation.class);
		verify(store).getOrComputeIfAbsent(ExecutorResource.class);
	}

	@Test
	@DisplayName("creates timeout invocation for SEPARATE_THREAD thread mode")
	void shouldCreateTimeoutInvocationForSeparateThreadTimeoutThreadMode() {
		Invocation<String> invocation = timeoutInvocationFactory.create(ThreadMode.SEPARATE_THREAD,
			timeoutInvocationParameters);
		assertThat(invocation).isInstanceOf(SeparateThreadTimeoutInvocation.class);
		verify(store, never()).getOrComputeIfAbsent(ExecutorResource.class);
	}

	private interface InvocationWithStringResult extends Invocation<String> {

	}
}
