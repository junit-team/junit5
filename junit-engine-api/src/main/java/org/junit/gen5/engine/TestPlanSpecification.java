/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @since 5.0
 */
public final class TestPlanSpecification implements Iterable<TestPlanSpecificationElement> {

	public static TestPlanSpecificationElement forClassName(String className) {
		return new ClassNameSpecification(className);
	}

	public static List<TestPlanSpecificationElement> forClassNames(String... classNames) {
		return stream(classNames).map(ClassNameSpecification::new).collect(toList());
	}

	public static TestPlanSpecificationElement forUniqueId(String uniqueId) {
		return new UniqueIdSpecification(uniqueId);
	}

	public static TestPlanSpecification build(TestPlanSpecificationElement... elements) {
		return build(Arrays.asList(elements));
	}

	public static TestPlanSpecification build(List<TestPlanSpecificationElement> elements) {
		return new TestPlanSpecification(elements);
	}

	private final List<TestPlanSpecificationElement> elements;

	public TestPlanSpecification(List<TestPlanSpecificationElement> elements) {
		this.elements = elements;
	}

	@Override
	public Iterator<TestPlanSpecificationElement> iterator() {
		return unmodifiableList(elements).iterator();
	};

}