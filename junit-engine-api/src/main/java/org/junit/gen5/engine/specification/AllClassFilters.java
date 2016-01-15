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

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.junit.gen5.engine.ClassFilter;

public class AllClassFilters implements ClassFilter {
	private List<ClassFilter> classFilters;

	public AllClassFilters(List<ClassFilter> classFilters) {
		this.classFilters = classFilters;
	}

	@Override
	public boolean acceptClass(Class<?> testClass) {
		if (classFilters == null) {
			return true;
		}
		else {
			return classFilters.stream().allMatch(filter -> filter.acceptClass(testClass));
		}
	}

	@Override
	public String getDescription() {
		return classFilters.stream().map(ClassFilter::getDescription).collect(joining(") and (", "(", ")"));
	}
}
