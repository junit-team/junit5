/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.ExecutionMode;

class CachingJupiterConfigurationTests {

	private final JupiterConfiguration delegate = mock(JupiterConfiguration.class);
	private JupiterConfiguration cache = new CachingJupiterConfiguration(delegate);

	@Test
	void cachesDefaultExecutionMode() {
		when(delegate.getDefaultExecutionMode()).thenReturn(ExecutionMode.CONCURRENT);

		assertThat(cache.getDefaultExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);
		assertThat(cache.getDefaultExecutionMode()).isEqualTo(ExecutionMode.CONCURRENT);

		verify(delegate, times(1)).getDefaultExecutionMode();
		verifyNoMoreInteractions(delegate);
	}

	@Test
	void cachesDefaultTestInstanceLifecycle() {
		when(delegate.getDefaultTestInstanceLifecycle()).thenReturn(Lifecycle.PER_CLASS);

		assertThat(cache.getDefaultTestInstanceLifecycle()).isEqualTo(Lifecycle.PER_CLASS);
		assertThat(cache.getDefaultTestInstanceLifecycle()).isEqualTo(Lifecycle.PER_CLASS);

		verify(delegate, times(1)).getDefaultTestInstanceLifecycle();
		verifyNoMoreInteractions(delegate);
	}

	@Test
	void cachesDeactivateExecutionConditionsPattern() {
		when(delegate.getDeactivateExecutionConditionsPattern()).thenReturn(Optional.empty());

		assertThat(cache.getDeactivateExecutionConditionsPattern()).isEqualTo(Optional.empty());
		assertThat(cache.getDeactivateExecutionConditionsPattern()).isEqualTo(Optional.empty());

		verify(delegate, times(1)).getDeactivateExecutionConditionsPattern();
		verifyNoMoreInteractions(delegate);
	}

	@Test
	void cachesExtensionAutoDetectionEnabled() {
		when(delegate.isExtensionAutoDetectionEnabled()).thenReturn(true);

		assertThat(cache.isExtensionAutoDetectionEnabled()).isEqualTo(true);
		assertThat(cache.isExtensionAutoDetectionEnabled()).isEqualTo(true);

		verify(delegate, times(1)).isExtensionAutoDetectionEnabled();
		verifyNoMoreInteractions(delegate);
	}

	@Test
	void cachesParallelExecutionEnabled() {
		when(delegate.isParallelExecutionEnabled()).thenReturn(true);

		assertThat(cache.isParallelExecutionEnabled()).isEqualTo(true);
		assertThat(cache.isParallelExecutionEnabled()).isEqualTo(true);

		verify(delegate, times(1)).isParallelExecutionEnabled();
		verifyNoMoreInteractions(delegate);
	}

	@Test
	void doesNotCacheRawParameters() {
		when(delegate.getRawConfigurationParameter("foo")).thenReturn(Optional.of("bar")).thenReturn(
			Optional.of("baz"));

		assertThat(cache.getRawConfigurationParameter("foo")).isEqualTo(Optional.of("bar"));
		assertThat(cache.getRawConfigurationParameter("foo")).isEqualTo(Optional.of("baz"));

		verify(delegate, times(2)).getRawConfigurationParameter("foo");
		verifyNoMoreInteractions(delegate);
	}

}
