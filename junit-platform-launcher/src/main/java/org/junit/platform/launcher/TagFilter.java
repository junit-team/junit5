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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.List;
import java.util.stream.Stream;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;

/**
 * Factory methods for creating {@link PostDiscoveryFilter PostDiscoveryFilters}
 * based on <em>included</em> and <em>excluded</em> tags.
 *
 * @since 1.0
 * @see #includeTags(String...)
 * @see #excludeTags(String...)
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
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}, and any
	 * blank tag will be ignored.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 * @see #includeTags(List)
	 */
	public static PostDiscoveryFilter includeTags(String... tags) {
		Preconditions.notNull(tags, "tags array must not be null");
		return includeTags(asList(tags));
	}

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}, and any
	 * blank tag will be ignored.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 * @see #includeTags(String...)
	 */
	public static PostDiscoveryFilter includeTags(List<String> tags) {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		Preconditions.containsNoNullElements(tags, "individual tags must not be null");
		List<String> trimmedTags = trimmedCopyOf(tags);
		return descriptor -> FilterResult.includedIf(trimmedTagsOf(descriptor).anyMatch(trimmedTags::contains));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}, and any
	 * blank tag will be ignored.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 * @see #excludeTags(List)
	 */
	public static PostDiscoveryFilter excludeTags(String... tags) {
		Preconditions.notNull(tags, "tags array must not be null");
		return excludeTags(asList(tags));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}, and any
	 * blank tag will be ignored.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 * @see #excludeTags(String...)
	 */
	public static PostDiscoveryFilter excludeTags(List<String> tags) {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		Preconditions.containsNoNullElements(tags, "individual tags must not be null");
		List<String> trimmedTags = trimmedCopyOf(tags);
		return descriptor -> FilterResult.includedIf(trimmedTagsOf(descriptor).noneMatch(trimmedTags::contains));
	}

	private static List<String> trimmedCopyOf(List<String> tags) {
		// @formatter:off
		return tags.stream()
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.collect(toList());
		// @formatter:on
	}

	private static Stream<String> trimmedTagsOf(TestDescriptor descriptor) {
		// @formatter:off
		return descriptor.getTags().stream()
				.map(TestTag::getName)
				.filter(StringUtils::isNotBlank)
				.map(String::trim);
		// @formatter:on
	}

}
