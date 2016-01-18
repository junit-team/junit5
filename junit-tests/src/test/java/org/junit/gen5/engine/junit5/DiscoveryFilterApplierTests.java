/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;
import org.junit.gen5.launcher.DiscoveryRequestBuilder;

/**
 * Microtests for {@link DiscoveryFilterApplier}
 */
class DiscoveryFilterApplierTests {

	@Test
	void applyClassFilter() {
		DiscoveryFilterApplier applier = new DiscoveryFilterApplier();

		EngineDiscoveryRequest request = new DiscoveryRequestBuilder().filter(
			ClassFilter.byNamePattern(".*\\$MatchingClass")).build();
		JUnit5EngineDescriptor engineDescriptor = createEngineDescriptor();

		applier.apply(request, engineDescriptor);

		List<String> includedDescriptors = engineDescriptor.allDescendants().stream().map(
			descriptor -> descriptor.getUniqueId()).collect(Collectors.toList());
		Assertions.assertEquals(2, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains("matching"));
		Assertions.assertTrue(includedDescriptors.contains("nested"));
	}

	private JUnit5EngineDescriptor createEngineDescriptor() {
		JUnit5EngineDescriptor descriptor = new JUnit5EngineDescriptor(new JUnit5TestEngine());
		ClassTestDescriptor matchingClass = new ClassTestDescriptor("matching", MatchingClass.class);
		descriptor.addChild(matchingClass);
		NestedClassTestDescriptor nestedClass = new NestedClassTestDescriptor("nested",
			MatchingClass.NestedClass.class);
		matchingClass.addChild(nestedClass);
		ClassTestDescriptor otherClass = new ClassTestDescriptor("other", OtherClass.class);
		descriptor.addChild(otherClass);
		return descriptor;
	}

	private static class MatchingClass {
		@Nested
		class NestedClass {
		}
	}

	private static class OtherClass {
	}
}
