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

import java.util.regex.Pattern;

import org.junit.gen5.engine.ClassFilter;

public class ClassNameFilter implements ClassFilter {
	private final Pattern pattern;

	public ClassNameFilter(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	@Override
	public boolean acceptClass(Class<?> testClass) {
		return pattern.matcher(testClass.getName()).matches();
	}

	@Override
	public String getDescription() {
		return "Filter class names with regular expression: " + pattern;
	}
}
