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

import java.util.List;

import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

public abstract class JUnit5TestResolver implements TestResolver {
	private TestEngine testEngine;
	private TestResolverRegistry testResolverRegistry;

	public TestEngine getTestEngine() {
		return testEngine;
	}

	public TestResolverRegistry getTestResolverRegistry() {
		return testResolverRegistry;
	}

	@Override
	public void initialize(TestEngine testEngine, TestResolverRegistry testResolverRegistry) {
		this.testEngine = testEngine;
		this.testResolverRegistry = testResolverRegistry;
	}

	protected <T extends TestDescriptor> void addChildrenAndNotify(TestDescriptor parent, List<T> children,
			EngineDiscoveryRequest discoveryRequest) {
		for (T child : children) {
			addChildAndNotify(parent, child, discoveryRequest);
		}
	}

	protected <T extends TestDescriptor> void addChildAndNotify(TestDescriptor parent, T child,
			EngineDiscoveryRequest discoveryRequest) {
		parent.addChild(child);
		getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
	}
}
