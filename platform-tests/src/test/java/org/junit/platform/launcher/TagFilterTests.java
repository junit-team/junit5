/*
 * Copyright 2015-2017 the original author or authors.
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

	private static final TestDescriptor classWithTag1 = classTestDescriptor("class1", ClassWithTag1.class);
	private static final TestDescriptor classWithTag1AndWhitespace = classTestDescriptor("class1-whitespace",
		ClassWithTag1AndWhitespace.class);
	private static final TestDescriptor classWithTag2 = classTestDescriptor("class2", ClassWithTag2.class);
	private static final TestDescriptor classWithBothTags = classTestDescriptor("class12", ClassWithBothTags.class);
	private static final TestDescriptor classWithDifferentTags = classTestDescriptor("classX",
		ClassWithDifferentTags.class);
	private static final TestDescriptor classWithNoTags = classTestDescriptor("class", ClassWithNoTags.class);

	@Test
	void includeSingleTag() throws Exception {
		includeSingleTag(includeTags("tag1", "   "));
	}

	@Test
	void includeSingleTagAndWhitespace() throws Exception {
		includeSingleTag(includeTags("   ", "\t \n tag1  "));
	}

	@Test
	void includeMultipleTags() throws Exception {
		PostDiscoveryFilter filter = includeTags("tag1", "  tag2  ", "     ");

		assertTrue(filter.apply(classWithBothTags).included());
		assertTrue(filter.apply(classWithTag1).included());
		assertTrue(filter.apply(classWithTag1AndWhitespace).included());
		assertTrue(filter.apply(classWithTag2).included());

		assertTrue(filter.apply(classWithDifferentTags).excluded());
		assertTrue(filter.apply(classWithNoTags).excluded());
	}

	@Test
	void excludeSingleTag() throws Exception {
		excludeSingleTag(excludeTags("tag1", "    "));
	}

	@Test
	void excludeSingleTagAndWhitespace() throws Exception {
		excludeSingleTag(excludeTags("\t \n tag1  ", "      "));
	}

	@Test
	void excludeMultipleTags() throws Exception {
		PostDiscoveryFilter filter = excludeTags("tag1", "  tag2  ", "     ");

		assertTrue(filter.apply(classWithTag1).excluded());
		assertTrue(filter.apply(classWithTag1AndWhitespace).excluded());
		assertTrue(filter.apply(classWithBothTags).excluded());
		assertTrue(filter.apply(classWithTag2).excluded());

		assertTrue(filter.apply(classWithDifferentTags).included());
		assertTrue(filter.apply(classWithNoTags).included());
	}

	private void includeSingleTag(PostDiscoveryFilter filter) {
		assertTrue(filter.apply(classWithTag1).included());
		assertTrue(filter.apply(classWithTag1AndWhitespace).included());
		assertTrue(filter.apply(classWithBothTags).included());

		assertTrue(filter.apply(classWithTag2).excluded());
		assertTrue(filter.apply(classWithDifferentTags).excluded());
		assertTrue(filter.apply(classWithNoTags).excluded());
	}

	private void excludeSingleTag(PostDiscoveryFilter filter) {
		assertTrue(filter.apply(classWithTag1).excluded());
		assertTrue(filter.apply(classWithTag1AndWhitespace).excluded());
		assertTrue(filter.apply(classWithBothTags).excluded());

		assertTrue(filter.apply(classWithTag2).included());
		assertTrue(filter.apply(classWithDifferentTags).included());
		assertTrue(filter.apply(classWithNoTags).included());
	}

	// -------------------------------------------------------------------------

	@Retention(RetentionPolicy.RUNTIME)
	@Tag("tag1")
	private @interface Tag1 {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Tag("tag2")
	private @interface Tag2 {
	}

	@Tag1
	private static class ClassWithTag1 {
	}

	@Tag("   tag1  \t    ")
	private static class ClassWithTag1AndWhitespace {
	}

	@Tag2
	private static class ClassWithTag2 {
	}

	@Tag1
	@Tag2
	private static class ClassWithBothTags {
	}

	@Tag("foo")
	@Tag("bar")
	private static class ClassWithDifferentTags {
	}

	@Tag("   ") // intentionally "blank"
	private static class ClassWithNoTags {
	}

	private static TestDescriptor classTestDescriptor(String uniqueId, Class<?> testClass) {
		UniqueId rootUniqueId = UniqueId.root("class", uniqueId);
		return new DemoClassTestDescriptor(rootUniqueId, testClass);
	}

}
