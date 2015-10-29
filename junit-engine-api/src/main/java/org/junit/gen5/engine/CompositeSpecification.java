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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class CompositeSpecification implements TestPlanSpecification {

	private final List<TestPlanSpecification> elements;


	public CompositeSpecification(TestPlanSpecification[] elements) {
		this(Arrays.asList(elements));
	}

	public CompositeSpecification(List<TestPlanSpecification> elements) {
		this.elements = elements;
	}

	@Override
	public Iterator<TestPlanSpecification> iterator() {
		return elements.iterator();
	}

}