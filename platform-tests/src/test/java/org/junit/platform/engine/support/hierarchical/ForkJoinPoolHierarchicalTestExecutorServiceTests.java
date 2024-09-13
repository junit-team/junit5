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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;

class ForkJoinPoolHierarchicalTestExecutorServiceTests {

	@Test
	void exceptionsFromInvalidConfigurationAreNotSwallowed() {
		var configuration = new DefaultParallelExecutionConfiguration(2, 1, 1, 1, 0, __ -> true);

		JUnitException exception = assertThrows(JUnitException.class, () -> {
			try (var pool = new ForkJoinPoolHierarchicalTestExecutorService(configuration)) {
				assertNotNull(pool, "we won't get here");
			}
		});

		assertThat(exception).hasMessage("Failed to create ForkJoinPool");
		assertThat(exception).rootCause().isInstanceOf(IllegalArgumentException.class);
	}

}
