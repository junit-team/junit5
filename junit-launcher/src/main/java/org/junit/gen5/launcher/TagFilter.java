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

import java.util.List;

import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestTag;

public class TagFilter {
	public static PostDiscoveryFilter includeTags(String... tagNames) {
		return includeTags(asList(tagNames));
	}

	public static PostDiscoveryFilter includeTags(List<String> includeTags) {
		// @formatter:off
        return descriptor -> FilterResult.includedIf(
				descriptor.getTags().stream()
					.map(TestTag::getName)
					.anyMatch(includeTags::contains));
        // @formatter:on
	}

	public static PostDiscoveryFilter excludeTags(String... tagNames) {
		return excludeTags(asList(tagNames));
	}

	public static PostDiscoveryFilter excludeTags(List<String> excludeTags) {
		// @formatter:off
        return descriptor -> FilterResult.includedIf(
				descriptor.getTags().stream()
                	.map(TestTag::getName)
					.noneMatch(excludeTags::contains));
        // @formatter:on
	}
}
