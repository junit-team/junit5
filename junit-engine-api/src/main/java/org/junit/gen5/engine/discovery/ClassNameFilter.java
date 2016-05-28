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

import org.junit.gen5.engine.FilterResult;

/**
 * @since 5.0
 */
class ClassNameFilter implements ClassFilter {

	private final Pattern pattern;

	ClassNameFilter(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public FilterResult apply(Class<?> testClass) {
		return includedIf(pattern.matcher(testClass.getName()).matches(), //
			() -> "Test class matches name pattern: " + pattern, //
			() -> "Test class does not match name pattern: " + pattern);
	}

	@Override
	public String toString() {
		return "Filter class names with regular expression: " + pattern;
	}

}
