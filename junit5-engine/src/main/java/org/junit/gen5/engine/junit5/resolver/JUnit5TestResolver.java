/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import org.junit.gen5.engine.TestEngine;

public abstract class JUnit5TestResolver implements TestResolver {
	private TestEngine testEngine;
	private TestResolverRegistry testResolverRegistry;

	@Override
	public void setTestEngine(TestEngine testEngine) {
		this.testEngine = testEngine;
	}

	@Override
	public void setTestResolverRegistry(TestResolverRegistry testResolverRegistry) {
		this.testResolverRegistry = testResolverRegistry;
	}

	public TestEngine getTestEngine() {
		return testEngine;
	}

	public TestResolverRegistry getTestResolverRegistry() {
		return testResolverRegistry;
	}
}
