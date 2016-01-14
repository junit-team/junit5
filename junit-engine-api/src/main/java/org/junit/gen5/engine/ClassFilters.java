/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.List;

public class ClassFilters {

	private ClassFilters() {
	}

	public static ClassFilter classNameMatches(String regex) {
		return new ClassNameFilter(regex);
	}

	public static ClassFilter allOf(ClassFilter... filters) {
		return allOf(asList(filters));
	}

	public static ClassFilter allOf(List<ClassFilter> filters) {
		if (filters.isEmpty()) {
			return anyClass();
		}
		if (filters.size() == 1) {
			return filters.get(0);
		}
		return new PredicateBasedClassFilter(
			testClass -> filters.stream().allMatch(filter -> filter.acceptClass(testClass)),
			() -> filters.stream().map(ClassFilter::getDescription).collect(joining(") and (", "(", ")")));
	}

	public static ClassFilter anyClass() {
		return new PredicateBasedClassFilter(c -> true, () -> "Any class");
	}

}
