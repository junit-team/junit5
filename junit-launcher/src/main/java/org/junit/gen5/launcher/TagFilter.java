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

import static java.util.Arrays.asList;
import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.List;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestTag;

/**
 * Factory methods for creating {@link PostDiscoveryFilter PostDiscoveryFilters}
 * based on <em>include</em> and <em>exclude</em> tags.
 *
 * @since 5.0
 */
@API(Experimental)
public final class TagFilter {

	private TagFilter() {
		/* no-op */
	}

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>include</em> tags.
	 */
	public static PostDiscoveryFilter includeTags(String... tags) {
		return includeTags(asList(tags));
	}

	/**
	 * Create an <em>include</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are tagged with
	 * at least one of the supplied <em>include</em> tags.
	 */
	public static PostDiscoveryFilter includeTags(List<String> tags) {
		// @formatter:off
        return descriptor -> FilterResult.includedIf(
				descriptor.getTags().stream()
					.map(TestTag::getName)
					.map(String::trim)
					.anyMatch(tags::contains));
        // @formatter:on
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>exclude</em> tags.
	 */
	public static PostDiscoveryFilter excludeTags(String... tags) {
		return excludeTags(asList(tags));
	}

	/**
	 * Create an <em>exclude</em> filter based on the supplied {@code tags}.
	 *
	 * <p>Containers and tests will only be executed if they are <em>not</em>
	 * tagged with any of the supplied <em>exclude</em> tags.
	 */
	public static PostDiscoveryFilter excludeTags(List<String> tags) {
		// @formatter:off
        return descriptor -> FilterResult.includedIf(
				descriptor.getTags().stream()
					.map(TestTag::getName)
					.map(String::trim)
					.noneMatch(tags::contains));
        // @formatter:on
	}

}
