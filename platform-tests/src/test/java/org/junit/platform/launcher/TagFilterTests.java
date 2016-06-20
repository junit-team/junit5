/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.DemoClassTestDescriptor;

/**
 * Unit tests for {@link TagFilter}.
 *
 * @since 1.0
 */
class TagFilterTests {

	TestDescriptor testWithTag1 = classTestDescriptor("test1", ATestWithATag1.class);
	TestDescriptor testWithTag2 = classTestDescriptor("test2", ATestWithATag2.class);
	TestDescriptor testWithBothTags = classTestDescriptor("test12", ATestWithBothTags.class);
	TestDescriptor testWithNoTags = classTestDescriptor("test", ATestWithNoTags.class);

	@Test
	void includeSingleTag() throws Exception {
		PostDiscoveryFilter filter = includeTags("tag1");

		assertTrue(filter.apply(testWithTag1).included());
		assertTrue(filter.apply(testWithBothTags).included());

		assertTrue(filter.apply(testWithTag2).excluded());
		assertTrue(filter.apply(testWithNoTags).excluded());
	}

	@Test
	void includeMultipleTags() throws Exception {
		PostDiscoveryFilter filter = includeTags("tag1", "tag2");

		assertTrue(filter.apply(testWithBothTags).included());
		assertTrue(filter.apply(testWithTag1).included());
		assertTrue(filter.apply(testWithTag2).included());

		assertTrue(filter.apply(testWithNoTags).excluded());
	}

	@Test
	void excludeSingleTag() throws Exception {
		PostDiscoveryFilter filter = excludeTags("tag1");

		assertTrue(filter.apply(testWithTag1).excluded());
		assertTrue(filter.apply(testWithBothTags).excluded());

		assertTrue(filter.apply(testWithTag2).included());
		assertTrue(filter.apply(testWithNoTags).included());
	}

	@Test
	void excludeMultipleTags() throws Exception {
		PostDiscoveryFilter filter = excludeTags("tag1", "tag2");

		assertTrue(filter.apply(testWithTag1).excluded());
		assertTrue(filter.apply(testWithBothTags).excluded());
		assertTrue(filter.apply(testWithTag2).excluded());

		assertTrue(filter.apply(testWithNoTags).included());
	}

	@Tag("tag1")
	private static class ATestWithATag1 {
	}

	@Tag("tag2")
	private static class ATestWithATag2 {
	}

	@Tag("tag1")
	@Tag("tag2")
	private static class ATestWithBothTags {
	}

	private static class ATestWithNoTags {
	}

	private static TestDescriptor classTestDescriptor(String uniqueId, Class<?> testClass) {
		UniqueId rootUniqueId = UniqueId.root("class", uniqueId);
		return new DemoClassTestDescriptor(rootUniqueId, testClass);
	}

}
