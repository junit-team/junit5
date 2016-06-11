/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder.classTestDescriptor;
import static org.junit.gen5.launcher.TagFilter.excludeTags;
import static org.junit.gen5.launcher.TagFilter.includeTags;

import org.junit.gen5.api.Tag;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

/**
 * Unit tests for {@link TagFilter}.
 *
 * @since 5.0
 */
class TagFilterTests {

	ClassTestDescriptor testWithTag1 = classTestDescriptor("test1", ATestWithATag1.class).build();
	ClassTestDescriptor testWithTag2 = classTestDescriptor("test2", ATestWithATag2.class).build();
	ClassTestDescriptor testWithBothTags = classTestDescriptor("test12", ATestWithBothTags.class).build();
	ClassTestDescriptor testWithNoTags = classTestDescriptor("test", ATestWithNoTags.class).build();

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

}
