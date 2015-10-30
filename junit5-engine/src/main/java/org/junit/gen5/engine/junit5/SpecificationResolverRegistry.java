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

import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
class SpecificationResolverRegistry {

	static {
		RESOLVERS = new HashMap() {

			{
				put(ClassNameSpecification.class, new ClassNameSpecificationResolver());
				put(UniqueIdSpecification.class, new UniqueIdSpecificationResolver());
			}
		};
	}

	private static final Map<Class<? extends TestPlanSpecificationElement>, SpecificationResolver> RESOLVERS;

	public static SpecificationResolver forType(Class<? extends TestPlanSpecificationElement> type) {
		if (RESOLVERS.containsKey(type)) {
			return RESOLVERS.get(type);
		}
		else {
			throw new UnsupportedOperationException(
				"There is no specification resolver registered for type: " + type.getName());
		}

	}
}
