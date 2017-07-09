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

import static java.util.stream.Collectors.toList;

import java.util.List;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;

public class TagExpressionFilter {
	public static PostDiscoveryFilter parse(String tagExpression) {
		Expression expression = new TagExpressionParser().parse(tagExpression);
		return descriptor -> FilterResult.includedIf(expression.evaluate(trimmedTagsOf(descriptor)));
	}

	private static List<String> trimmedTagsOf(TestDescriptor descriptor) {
		// @formatter:off
        return descriptor.getTags().stream()
                .map(TestTag::getName)
                .map(String::trim)
                .collect(toList());
        // @formatter:on
	}
}
