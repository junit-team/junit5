/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static org.junit.gen5.engine.FilterResult.includedIf;

import java.util.regex.Pattern;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.FilterResult;

/**
 * {@link ClassFilter} that matches fully qualified class names against a
 * pattern in the form of a regular expression.
 *
 * @since 5.0
 */
class ClassNameFilter implements ClassFilter {

	private final Pattern pattern;

	ClassNameFilter(String pattern) {
		Preconditions.notBlank(pattern, "pattern must not be null or empty");
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public FilterResult apply(Class<?> clazz) {
		String name = clazz.getName();
		return includedIf(pattern.matcher(name).matches(), //
			() -> String.format("Class name [%s] matches pattern: %s", name, pattern), //
			() -> String.format("Class name [%s] does not match pattern: %s", name, pattern));
	}

	@Override
	public String toString() {
		return "Filter class names with regular expression: " + pattern;
	}

}
