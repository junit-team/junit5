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

import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Collection of utilities for working with {@link ServiceLoader}.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.11
 */
@API(status = Status.INTERNAL, since = "5.11")
public class ServiceLoaderUtils {

	/**
	 * Loads services of the given type using the specified class loader and filters them using the provided predicate.
	 *
	 * @param <T> the type of the service
	 * @param service the class of the service to be loaded
	 * @param providerPredicate the predicate to filter the loaded services
	 * @param loader the class loader to be used to load the services
	 * @return a stream of loaded services that match the predicate
	 */
	public static <T> Stream<T> load(Class<T> service, Predicate<? super Class<? extends T>> providerPredicate,
			ClassLoader loader) {
		// @formatter:off
		return ServiceLoader.load(service, loader)
				.stream()
				.filter(provider -> providerPredicate.test(provider.type()))
				.map(ServiceLoader.Provider::get);
		// @formatter:on
	}

}
