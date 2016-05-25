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

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder;

/**
 * @since 5.0
 */
class TagFilterTests {

	ClassTestDescriptor testWithTag1 = (ClassTestDescriptor) TestDescriptorBuilder.classTestDescriptor("test1",
		ATestWithATag1.class).build();

	ClassTestDescriptor testWithTag2 = (ClassTestDescriptor) TestDescriptorBuilder.classTestDescriptor("test2",
		ATestWithATag2.class).build();

	ClassTestDescriptor testWithBothTags = (ClassTestDescriptor) TestDescriptorBuilder.classTestDescriptor("test12",
		ATestWithBothTags.class).build();

	ClassTestDescriptor testWithNoTags = (ClassTestDescriptor) TestDescriptorBuilder.classTestDescriptor("test",
		ATestWithNoTags.class).build();

	@Test
	void requireSingleTag() throws Exception {
		PostDiscoveryFilter requireSingleTag = TagFilter.requireTags("tag1");

		Assertions.assertTrue(requireSingleTag.filter(testWithTag1).included());
		Assertions.assertTrue(requireSingleTag.filter(testWithBothTags).included());

		Assertions.assertTrue(requireSingleTag.filter(testWithTag2).excluded());
		Assertions.assertTrue(requireSingleTag.filter(testWithNoTags).excluded());
	}

	@Test
	void requireAtLeastOneOfTwoTags() throws Exception {
		PostDiscoveryFilter requireSingleTag = TagFilter.requireTags("tag1", "tag2");

		Assertions.assertTrue(requireSingleTag.filter(testWithBothTags).included());
		Assertions.assertTrue(requireSingleTag.filter(testWithTag1).included());
		Assertions.assertTrue(requireSingleTag.filter(testWithTag2).included());

		Assertions.assertTrue(requireSingleTag.filter(testWithNoTags).excluded());
	}

	@Test
	void excludeSingleTag() throws Exception {
		PostDiscoveryFilter requireSingleTag = TagFilter.excludeTags("tag1");

		Assertions.assertTrue(requireSingleTag.filter(testWithTag1).excluded());
		Assertions.assertTrue(requireSingleTag.filter(testWithBothTags).excluded());

		Assertions.assertTrue(requireSingleTag.filter(testWithTag2).included());
		Assertions.assertTrue(requireSingleTag.filter(testWithNoTags).included());
	}

	@Test
	void excludeSeveralTags() throws Exception {
		PostDiscoveryFilter requireSingleTag = TagFilter.excludeTags("tag1", "tag2");

		Assertions.assertTrue(requireSingleTag.filter(testWithTag1).excluded());
		Assertions.assertTrue(requireSingleTag.filter(testWithBothTags).excluded());
		Assertions.assertTrue(requireSingleTag.filter(testWithTag2).excluded());

		Assertions.assertTrue(requireSingleTag.filter(testWithNoTags).included());
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
