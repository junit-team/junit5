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

import static java.util.Arrays.asList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.List;
import java.util.stream.Stream;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;

/**
 * Factory methods for creating {@link PostDiscoveryFilter PostDiscoveryFilters}
 * based on <em>included</em> and <em>excluded</em> tags.
 *
 * @since 1.0
 */
@API(Experimental)
public final class TagFilter {

	///CLOVER:OFF
	private TagFilter() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 */
	public static PostDiscoveryFilter includeTags(String... tags) {
		Preconditions.notNull(tags, "tags array must not be null");
		return includeTags(asList(tags));
	}

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 */
	public static PostDiscoveryFilter includeTags(List<String> tags) {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		Preconditions.containsNoNullElements(tags, "individual tags must not be null");
		return descriptor -> FilterResult.includedIf(trimmedTagsOf(descriptor).anyMatch(tags::contains));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 */
	public static PostDiscoveryFilter excludeTags(String... tags) {
		Preconditions.notNull(tags, "tags array must not be null");
		return excludeTags(asList(tags));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 */
	public static PostDiscoveryFilter excludeTags(List<String> tags) {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		Preconditions.containsNoNullElements(tags, "individual tags must not be null");
		return descriptor -> FilterResult.includedIf(trimmedTagsOf(descriptor).noneMatch(tags::contains));
	}

	private static Stream<String> trimmedTagsOf(TestDescriptor descriptor) {
		// @formatter:off
		return descriptor.getTags().stream()
				.map(TestTag::getName)
				.map(String::trim);
		// @formatter:on
	}

}
