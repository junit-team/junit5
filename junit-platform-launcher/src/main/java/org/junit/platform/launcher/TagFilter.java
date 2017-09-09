/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestTag;

/**
 * Factory methods for creating {@link PostDiscoveryFilter PostDiscoveryFilters}
 * based on <em>included</em> and <em>excluded</em> tags.
 *
 * @since 1.0
 * @see #includeTags(String...)
 * @see #excludeTags(String...)
 */
@API(status = STABLE, since = "1.0")
public final class TagFilter {

	///CLOVER:OFF
	private TagFilter() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 * @throws PreconditionViolationException if the supplied tags array is
	 * {@code null} or empty, or if any individual tag is not syntactically
	 * valid
	 * @see #includeTags(List)
	 * @see TestTag#isValid(String)
	 */
	public static PostDiscoveryFilter includeTags(String... tags) throws PreconditionViolationException {
		Preconditions.notNull(tags, "tags array must not be null");
		return includeTags(asList(tags));
	}

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>included</em> tags.
	 *
	 * @param tags the included tags; never {@code null} or empty
	 * @throws PreconditionViolationException if the supplied tags list is
	 * {@code null} or empty, or if any individual tag is not syntactically
	 * valid
	 * @see #includeTags(String...)
	 * @see TestTag#isValid(String)
	 */
	public static PostDiscoveryFilter includeTags(List<String> tags) throws PreconditionViolationException {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		List<TestTag> activeTags = toTestTags(tags);
		return descriptor -> FilterResult.includedIf(descriptor.getTags().stream().anyMatch(activeTags::contains));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 * @throws PreconditionViolationException if the supplied tags array is
	 * {@code null} or empty, or if any individual tag is not syntactically
	 * valid
	 * @see #excludeTags(List)
	 * @see TestTag#isValid(String)
	 */
	public static PostDiscoveryFilter excludeTags(String... tags) throws PreconditionViolationException {
		Preconditions.notNull(tags, "tags array must not be null");
		return excludeTags(asList(tags));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Note: each tag will be {@linkplain String#trim() trimmed}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>excluded</em> tags.
	 *
	 * @param tags the excluded tags; never {@code null} or empty
	 * @throws PreconditionViolationException if the supplied tags list is
	 * {@code null} or empty, or if any individual tag is not syntactically
	 * valid
	 * @see #excludeTags(String...)
	 * @see TestTag#isValid(String)
	 */
	public static PostDiscoveryFilter excludeTags(List<String> tags) throws PreconditionViolationException {
		Preconditions.notEmpty(tags, "tags list must not be null or empty");
		List<TestTag> activeTags = toTestTags(tags);
		return descriptor -> FilterResult.includedIf(descriptor.getTags().stream().noneMatch(activeTags::contains));
	}

	private static List<TestTag> toTestTags(List<String> tags) {
		return tags.stream().map(TestTag::create).collect(toList());
	}

}
