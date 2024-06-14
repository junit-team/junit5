/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.Resource;

/**
 * Resource-related predicate used by reflection utilities.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.11
 */
@API(status = INTERNAL, since = "1.11")
public class ResourceFilter implements Predicate<Resource> {

	/**
	 * Create a {@link ResourceFilter} instance that filters resources.
	 */
	public static ResourceFilter of(Predicate<Resource> resourcePredicate) {
		return new ResourceFilter(resourcePredicate);
	}

	private final Predicate<Resource> resourcePredicate;

	private ResourceFilter(Predicate<Resource> resourcePredicate) {
		this.resourcePredicate = Preconditions.notNull(resourcePredicate, "resource predicate must not be null");
	}

	@Override
	public boolean test(Resource resource) {
		return resourcePredicate.test(resource);
	}
}
