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
import static org.junit.gen5.engine.FilterResult.included;

import java.util.List;

import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.FilterResult;

public class AllClassFilters implements ClassFilter {
	private List<ClassFilter> classFilters;

	public AllClassFilters(List<ClassFilter> classFilters) {
		this.classFilters = classFilters;
	}

	@Override
	public FilterResult filter(Class<?> testClass) {
		if (classFilters == null) {
			return included("No filters to be applied on test class");
		}
		else {
			// @formatter:off
			return FilterResult.includedIf(
				classFilters.stream()
						.map(filter -> filter.filter(testClass))
						.allMatch(FilterResult::included));
			// @formatter:on
		}
	}

	@Override
	public String toString() {
		return classFilters.stream().map(ClassFilter::toString).collect(joining(") and (", "(", ")"));
	}
}
