/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

/**
 * @author Sam Brannen
 * @author Matthias Merdes
 * @since 5.0
 */
class MethodParameterResolverRegistry {

	private final Set<MethodParameterResolver> resolvers = new LinkedHashSet<>();

	/**
	 * Create a new {@link MethodParameterResolverRegistry} with global,
	 * default parameter resolvers pre-registered.
	 *
	 * @see TestNameParameterResolver
	 */
	MethodParameterResolverRegistry(Set<MethodParameterResolver> parentResolvers) {
		parentResolvers.stream().forEach(resolvers::add);
		addResolver(TestNameParameterResolver.class);
	}

	Set<MethodParameterResolver> getResolvers() {
		return Collections.unmodifiableSet(this.resolvers);
	}

	void addResolver(Class<? extends MethodParameterResolver> resolverClass) {
		if (!resolverAlreadyPresent(resolverClass)) {
			this.resolvers.add(ReflectionUtils.newInstance(resolverClass));
		}
	}

	private boolean resolverAlreadyPresent(Class<? extends MethodParameterResolver> resolverClass) {
		// Only one resolver of same type needed since resolvers are stateless.
		return this.resolvers.stream().anyMatch(r -> r.getClass().equals(resolverClass));
	}

}
