/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.ServiceLoader;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.launcher.PostDiscoveryFilter;

/**
 * @since 1.7
 */
class ServiceLoaderPostDiscoveryFilterRegistry {

	private static final Logger logger = LoggerFactory.getLogger(ServiceLoaderPostDiscoveryFilterRegistry.class);

	Iterable<PostDiscoveryFilter> loadPostDiscoveryFilters() {
		Iterable<PostDiscoveryFilter> filters = ServiceLoader.load(PostDiscoveryFilter.class,
			ClassLoaderUtils.getDefaultClassLoader());
		logger.config(() -> "Loaded PostDiscoveryFilter instances: "
				+ stream(filters.spliterator(), false).map(Object::toString).collect(toList()));
		return filters;
	}

}
