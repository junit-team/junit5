/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification.dsl;

import java.util.Arrays;
import java.util.List;

import org.junit.gen5.engine.DescriptorFilter;
import org.junit.gen5.engine.TestTag;

public class TagFilterBuilder {
	public static DescriptorFilter includeTags(String... tagNames) {
		return includeTags(Arrays.asList(tagNames));
	}

	public static DescriptorFilter includeTags(List<String> includeTags) {
		// @formatter:off
        return descriptor -> descriptor.getTags().stream()
                .map(TestTag::getName)
                .anyMatch(includeTags::contains);
        // @formatter:on
	}

	public static DescriptorFilter excludeTags(String... tagNames) {
		return excludeTags(Arrays.asList(tagNames));
	}

	public static DescriptorFilter excludeTags(List<String> includeTags) {
		// @formatter:off
        return descriptor -> descriptor.getTags().stream()
                .map(TestTag::getName)
                .allMatch(tagName -> !includeTags.contains(tagName));
        // @formatter:on
	}
}
