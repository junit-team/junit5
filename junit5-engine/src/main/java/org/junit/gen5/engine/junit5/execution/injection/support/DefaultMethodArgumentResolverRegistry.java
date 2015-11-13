/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.junit5.execution.injection.MethodArgumentResolver;
import org.junit.gen5.engine.junit5.execution.injection.MethodArgumentResolverRegistry;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public class DefaultMethodArgumentResolverRegistry implements MethodArgumentResolverRegistry {

	private final List<MethodArgumentResolver> resolvers;

	public DefaultMethodArgumentResolverRegistry() {
		List<MethodArgumentResolver> resolvers = new ArrayList<>();

		// Default resolvers
		resolvers.add(new TestNameArgumentResolver());

		// Custom resolvers
		resolvers.addAll(lookUpCustomResolvers());

		this.resolvers = resolvers;
	}

	@Override
	public List<MethodArgumentResolver> getResolvers() {
		return Collections.unmodifiableList(this.resolvers);
	}

	@Override
	public void addResolvers(MethodArgumentResolver... resolvers) {
		Preconditions.notNull(resolvers, "MethodArgumentResolver array must not be null");
		addResolvers(Arrays.asList(resolvers));
	}

	@Override
	public void addResolvers(List<MethodArgumentResolver> resolvers) {
		Preconditions.notNull(resolvers, "MethodArgumentResolver list must not be null");
		resolvers.forEach(resolver -> this.resolvers.add(resolver));
	}

	private static List<MethodArgumentResolver> lookUpCustomResolvers() {
		List<MethodArgumentResolver> customResolvers = new ArrayList<>();

		// TODO Look up custom MethodArgumentResolvers via an extension mechanism.
		customResolvers.add(new DemoTypeBasedMethodArgumentResolver());
		customResolvers.add(new DemoAnnotationBasedMethodArgumentResolver());

		return customResolvers;
	}

}
