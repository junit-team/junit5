/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestPlanSpecificationElement;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestDescriptorResolverRegistry {

	private final Map<Class<? extends TestPlanSpecificationElement>, TestDescriptorResolver<? extends TestPlanSpecificationElement, ?>> resolvers = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends TestPlanSpecificationElement> TestDescriptorResolver<T, ?> forType(Class<T> type) {
		Preconditions.notNull(type, "TestPlanSpecificationElement type must not be null");
		if (this.resolvers.containsKey(type)) {
			return (TestDescriptorResolver<T, ?>) resolvers.get(type);
		}

		// else
		throw new IllegalArgumentException(
			"There is no TestDescriptorResolver registered for TestPlanSpecificationElement type: " + type.getName());
	}

	public <T extends TestPlanSpecificationElement> void addResolver(Class<T> element,
			TestDescriptorResolver<T, ?> resolver) {

		this.resolvers.put(element, resolver);
	}

}
