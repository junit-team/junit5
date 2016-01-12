/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;

public class TagFilterBuilder {
	public static Predicate<TestDescriptor> includeTags(String... tagNames) {
		return includeTags(Arrays.asList(tagNames));
	}

	public static Predicate<TestDescriptor> includeTags(List<String> includeTags) {
		// @formatter:off
        return descriptor -> descriptor.getTags().stream()
                .map(TestTag::getName)
                .anyMatch(includeTags::contains);
        // @formatter:on
	}

	public static Predicate<TestDescriptor> excludeTags(String... tagNames) {
		return excludeTags(Arrays.asList(tagNames));
	}

	public static Predicate<TestDescriptor> excludeTags(List<String> includeTags) {
		// @formatter:off
        return descriptor -> descriptor.getTags().stream()
                .map(TestTag::getName)
                .allMatch(tagName -> !includeTags.contains(tagName));
        // @formatter:on
	}
}
