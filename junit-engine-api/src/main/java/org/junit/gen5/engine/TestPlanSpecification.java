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
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * @since 5.0
 */
public interface TestPlanSpecification extends Iterable<TestPlanSpecification> {

	static TestPlanSpecification forClassName(String className) {
		return new ClassNameSpecification(className);
	}

	static TestPlanSpecification forUniqueId(String uniqueId) {
		return new UniqueIdSpecification(uniqueId);
	}

	static TestPlanSpecification build(TestPlanSpecification... elements) {
		return new CompositeSpecification(elements);
	}

	static TestPlanSpecification forClassNames(String... classNames) {
		List<TestPlanSpecification> elements = stream(classNames).map(ClassNameSpecification::new).collect(toList());
		return new CompositeSpecification(elements);
	}

}