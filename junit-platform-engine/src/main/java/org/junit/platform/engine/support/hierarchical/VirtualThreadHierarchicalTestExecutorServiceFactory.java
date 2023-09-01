/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;

@API(status = EXPERIMENTAL, since = "1.10.1")
public class VirtualThreadHierarchicalTestExecutorServiceFactory {

	public static HierarchicalTestExecutorService create(
			@SuppressWarnings("unused") ConfigurationParameters configurationParameters) {
		throw new IllegalArgumentException("The virtual executor is only supported on Java 21 and above");
	}

	private VirtualThreadHierarchicalTestExecutorServiceFactory() {
		throw new AssertionError();
	}
}
