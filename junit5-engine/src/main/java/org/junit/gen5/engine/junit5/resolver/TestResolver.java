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

import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * A {@link TestResolver} is responsible for resolving different kind of test representatives. Each resolver can
 * contribute to the list of children of the given parent. It may only resolve children, that accomplish the
 * {@link EngineDiscoveryRequest}. The children are returned as list.
 *
 * @since 5.0
 */
public interface TestResolver {
	void initialize(TestEngine testEngine, TestResolverRegistry testResolverRegistry);

	void resolveFor(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest);
}
