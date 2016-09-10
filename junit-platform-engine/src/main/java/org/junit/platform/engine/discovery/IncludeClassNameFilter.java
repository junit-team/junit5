/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.engine.FilterResult.includedIf;

import java.util.regex.Pattern;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.FilterResult;

/**
 * {@link ClassNameFilter} that matches fully qualified class names against a
 * pattern in the form of a regular expression.
 *
 * <p>If the fully qualified name of a class matches against the pattern, the
 * class will be included.
 *
 * @since 1.0
 */
class IncludeClassNameFilter implements ClassNameFilter {

	private final Pattern pattern;

	IncludeClassNameFilter(String pattern) {
		Preconditions.notBlank(pattern, "pattern must not be null or blank");
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public FilterResult apply(String className) {
		return includedIf(this.pattern.matcher(className).matches(), //
			() -> String.format("Class name [%s] matches pattern: %s", className, this.pattern), //
			() -> String.format("Class name [%s] does not match pattern: %s", className, this.pattern));
	}

	@Override
	public String toString() {
		return "Includes class names with regular expression: " + this.pattern;
	}

}
