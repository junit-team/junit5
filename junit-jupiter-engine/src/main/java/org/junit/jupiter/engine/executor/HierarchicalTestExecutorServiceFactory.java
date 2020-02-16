/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.executor;

import static org.junit.jupiter.engine.config.JupiterConfiguration.ParallelExecutor.VIRTUAL;

import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

public class HierarchicalTestExecutorServiceFactory {

	public static HierarchicalTestExecutorService create(JupiterConfiguration configuration,
			ConfigurationParameters configurationParameters) {
		if (configuration.getParallelExecutor() == VIRTUAL) {
			throw new IllegalArgumentException("The virtual executor is only supported on Java 15 and above");
		}
		return new ForkJoinPoolHierarchicalTestExecutorService(configurationParameters);
	}
}
