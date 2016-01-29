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
 * The {@code TestResolverRegistry} is the central registration for all
 * {@link TestResolver}s. It operates as a registry and as a communication
 * bus between the different resolvers.
 *
 * @since 5.0
 */
public interface TestResolverRegistry {
	void register(TestResolver testResolver);

	void notifyResolvers(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest);

	void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest);

	TestDescriptor fetchParent(DiscoverySelector selector, TestDescriptor root);
}
