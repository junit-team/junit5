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

import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.TestNameArgumentResolver;

/**
 * @author Sam Brannen
 * @author Matthias Merdes
 * @since 5.0
 */
class MethodArgumentResolverRegistry {

	private final Set<MethodArgumentResolver> resolvers = new LinkedHashSet<>();

	/**
	 * Create a new {@link MethodArgumentResolverRegistry} with
	 * global, default argument resolvers pre-registered.
	 *
	 * @see TestNameArgumentResolver
	 */
	MethodArgumentResolverRegistry() {
		addResolverWithClass(TestNameArgumentResolver.class);
	}

	Set<MethodArgumentResolver> getResolvers() {
		return Collections.unmodifiableSet(this.resolvers);
	}

	void addResolverWithClass(Class<? extends MethodArgumentResolver> resolverClass) {
		if (resolverAlreadyPresent(resolverClass))
			return;
		MethodArgumentResolver resolver = ReflectionUtils.newInstance(resolverClass);
		this.resolvers.add(resolver);

	}

	private boolean resolverAlreadyPresent(Class<? extends MethodArgumentResolver> resolverClass) {
		//Only one resolver of same class needed since resolvers don't have state
		return resolvers.stream().anyMatch(r -> r.getClass().equals(resolverClass));
	}

	public void addAllResolvers(Set<MethodArgumentResolver> parentResolvers) {
		parentResolvers.stream().forEach(r -> {
			if (!resolverAlreadyPresent(r.getClass()))
				this.resolvers.add(r);
		});
	}
}
