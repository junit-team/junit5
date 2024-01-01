/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForkJoinPoolHierarchicalTestExecutorServiceTests {

	@Mock
	ParallelExecutionConfiguration configuration;

	@Test
	void exceptionsFromInvalidConfigurationAreNotSwallowed() {
		when(configuration.getParallelism()).thenReturn(2);
		when(configuration.getMaxPoolSize()).thenReturn(1); // invalid, should be > parallelism
		when(configuration.getCorePoolSize()).thenReturn(1);
		when(configuration.getMinimumRunnable()).thenReturn(1);
		when(configuration.getSaturatePredicate()).thenReturn(__ -> true);
		when(configuration.getKeepAliveSeconds()).thenReturn(0);

		JUnitException exception = assertThrows(JUnitException.class,
			() -> new ForkJoinPoolHierarchicalTestExecutorService(configuration));
		assertThat(exception).hasMessage("Failed to create ForkJoinPool");
		assertThat(exception).rootCause().isInstanceOf(IllegalArgumentException.class);
	}

}
