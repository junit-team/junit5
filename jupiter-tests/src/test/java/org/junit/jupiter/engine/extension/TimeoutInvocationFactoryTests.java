/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.engine.extension.TimeoutInvocationFactory.SingleThreadExecutorResource;
import org.junit.jupiter.engine.extension.TimeoutInvocationFactory.TimeoutInvocationParameters;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("TimeoutInvocationFactory")
@ExtendWith(MockitoExtension.class)
class TimeoutInvocationFactoryTests {

	@Spy
	private final Store store = new NamespaceAwareStore(new NamespacedHierarchicalStore<>(null),
		ExtensionContext.Namespace.create(TimeoutInvocationFactoryTests.class));
	@Mock
	private Invocation<String> invocation;
	@Mock
	private TimeoutDuration timeoutDuration;
	private TimeoutInvocationFactory timeoutInvocationFactory;
	private TimeoutInvocationParameters<String> parameters;

	@BeforeEach
	void setUp() {
		parameters = new TimeoutInvocationParameters<>(invocation, timeoutDuration, () -> "description");
		timeoutInvocationFactory = new TimeoutInvocationFactory(store);
	}

	@Test
	@DisplayName("throws exception when null store is provided on create")
	void shouldThrowExceptionWhenInstantiatingWithNullStore() {
		assertThatThrownBy(() -> new TimeoutInvocationFactory(null)) //
				.hasMessage("store must not be null");
	}

	@Test
	@DisplayName("throws exception when null timeout thread mode is provided on create")
	void shouldThrowExceptionWhenNullTimeoutThreadModeIsProvidedWhenCreate() {
		assertThatThrownBy(() -> timeoutInvocationFactory.create(null, parameters)) //
				.hasMessage("thread mode must not be null");
	}

	@Test
	@DisplayName("throws exception when null timeout invocation parameters is provided on create")
	void shouldThrowExceptionWhenNullTimeoutInvocationParametersIsProvidedWhenCreate() {
		assertThatThrownBy(() -> timeoutInvocationFactory.create(ThreadMode.SAME_THREAD, null)) //
				.hasMessage("timeout invocation parameters must not be null");
	}

	@Test
	@DisplayName("creates timeout invocation for SAME_THREAD thread mode")
	void shouldCreateTimeoutInvocationForSameThreadTimeoutThreadMode() {
		var invocation = timeoutInvocationFactory.create(ThreadMode.SAME_THREAD, parameters);
		assertThat(invocation).isInstanceOf(SameThreadTimeoutInvocation.class);
		verify(store).getOrComputeIfAbsent(SingleThreadExecutorResource.class);
	}

	@Test
	@DisplayName("creates timeout invocation for SEPARATE_THREAD thread mode")
	void shouldCreateTimeoutInvocationForSeparateThreadTimeoutThreadMode() {
		var invocation = timeoutInvocationFactory.create(ThreadMode.SEPARATE_THREAD, parameters);
		assertThat(invocation).isInstanceOf(SeparateThreadTimeoutInvocation.class);
	}

}
