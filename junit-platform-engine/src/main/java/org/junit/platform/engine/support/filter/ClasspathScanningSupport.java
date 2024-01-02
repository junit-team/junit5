/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.filter;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.junit.platform.engine.Filter.composeFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;

/**
 * Support utility methods for classpath scanning.
 *
 * @since 1.0
 * @deprecated Please use {@link org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver} instead.
 */
@Deprecated
@API(status = DEPRECATED, since = "1.5")
public final class ClasspathScanningSupport {

	private ClasspathScanningSupport() {
		/* no-op */
	}

	/**
	 * Build a {@link Predicate} for fully qualified class names to be used for
	 * classpath scanning from an {@link EngineDiscoveryRequest}.
	 *
	 * @param request the request to build a predicate from
	 * @deprecated Please use {@link org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver} instead.
	 */
	@Deprecated
	public static Predicate<String> buildClassNamePredicate(EngineDiscoveryRequest request) {
		List<DiscoveryFilter<String>> filters = new ArrayList<>();
		filters.addAll(request.getFiltersByType(ClassNameFilter.class));
		filters.addAll(request.getFiltersByType(PackageNameFilter.class));
		return composeFilters(filters).toPredicate();
	}

	/**
	 * Build a {@link ClassFilter} by combining the name predicate built by
	 * {@link #buildClassNamePredicate(EngineDiscoveryRequest)} and the passed-in
	 * class predicate.
	 *
	 * @param request the request to build a name predicate from
	 * @param classPredicate the class predicate
	 * @deprecated Please use {@link org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver} instead.
	 */
	@Deprecated
	@SuppressWarnings("exports")
	public static ClassFilter buildClassFilter(EngineDiscoveryRequest request, Predicate<Class<?>> classPredicate) {
		return ClassFilter.of(buildClassNamePredicate(request), classPredicate);
	}

}
