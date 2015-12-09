/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.resolver;

import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * A {@link TestResolver} is responsible for resolving different kind of test representatives.
 * Each resolver can contribute to the list of children of the given parent. It may only resolve
 * children, that accomplish the {@link TestPlanSpecification}. The children are returned as list.
 */
@FunctionalInterface
public interface TestResolver {
	List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification);
}
