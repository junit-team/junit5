/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @since 5.0
 */
public interface TestResolverRegistry {
	TestDescriptor fetchParent(DiscoverySelector selector, TestDescriptor root);

	void notifyResolvers(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest);

	void register(TestResolver testResolver);
}
