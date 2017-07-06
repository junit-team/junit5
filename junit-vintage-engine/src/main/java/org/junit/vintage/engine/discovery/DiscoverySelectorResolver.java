/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import org.junit.platform.engine.EngineDiscoveryRequest;

/**
 * @since 4.12
 */
interface DiscoverySelectorResolver {

	IsPotentialJUnit4TestClass classTester = new IsPotentialJUnit4TestClass();

	void resolve(EngineDiscoveryRequest request, TestClassCollector collector);
}
