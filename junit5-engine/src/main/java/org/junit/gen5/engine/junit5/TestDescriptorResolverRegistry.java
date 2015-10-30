/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.engine.TestPlanSpecificationElement;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
class TestDescriptorResolverRegistry {

	private final Map<Class<? extends TestPlanSpecificationElement>, TestDescriptorResolver> resolvers = new HashMap<>();

	public TestDescriptorResolver forType(Class<? extends TestPlanSpecificationElement> type) {
		if (resolvers.containsKey(type)) {
			return resolvers.get(type);
		}
		else {
			throw new UnsupportedOperationException(
				"There is no specification resolver registered for type: " + type.getName());
		}
	}

	public void addResolver(Class<? extends TestPlanSpecificationElement> element, TestDescriptorResolver resolver) {
		resolvers.put(element, resolver);
	}
}