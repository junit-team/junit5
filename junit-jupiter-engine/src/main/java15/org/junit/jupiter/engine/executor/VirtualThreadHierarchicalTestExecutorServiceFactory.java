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

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

public class VirtualThreadHierarchicalTestExecutorServiceFactory {

	public static HierarchicalTestExecutorService create(ConfigurationParameters configurationParameters) {
		return new VirtualThreadHierarchicalTestExecutorService(configurationParameters);
	}
}
