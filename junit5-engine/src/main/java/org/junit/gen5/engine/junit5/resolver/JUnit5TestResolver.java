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

import static org.junit.gen5.engine.junit5.resolver.UniqueId.from;

import java.util.Optional;
import java.util.function.Function;

import org.junit.gen5.engine.TestDescriptor;

/**
 * @since 5.0
 */
abstract class JUnit5TestResolver implements TestResolver {
	protected static <T extends TestDescriptor> T fetchFromTreeOrCreateNew( //
			TestDescriptor parent, UniqueId appender, Function<UniqueId, T> newInstance) {
		UniqueId uniqueId = from(parent.getUniqueId()).append(appender);
		Optional<? extends TestDescriptor> descriptor = parent.findByUniqueId(uniqueId.toString());
		if (descriptor.isPresent()) {
			return (T) descriptor.get();
		}
		else {
			return newInstance.apply(uniqueId);
		}
	}

	private TestResolverRegistry testResolverRegistry;

	public TestResolverRegistry getTestResolverRegistry() {
		return testResolverRegistry;
	}

	@Override
	public void bindTestResolveryRegistry(TestResolverRegistry testResolverRegistry) {
		this.testResolverRegistry = testResolverRegistry;
	}
}
