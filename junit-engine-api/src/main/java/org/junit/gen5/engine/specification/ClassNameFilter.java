/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification;

import static org.junit.gen5.engine.FilterResult.accepted;
import static org.junit.gen5.engine.FilterResult.filtered;

import java.util.regex.Pattern;

import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.FilterResult;

public class ClassNameFilter implements ClassFilter {
	private final Pattern pattern;

	public ClassNameFilter(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	@Override
	public FilterResult filter(Class<?> testClass) {
		if (pattern.matcher(testClass.getName()).matches()) {
			return accepted("TestClass matches name pattern");
		}
		else {
			return filtered("TestClass does not match name pattern");
		}
	}

	@Override
	public String toString() {
		return "Filter class names with regular expression: " + pattern;
	}
}
