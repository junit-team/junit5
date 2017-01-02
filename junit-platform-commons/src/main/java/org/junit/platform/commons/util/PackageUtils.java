/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with {@linkplain Package packages}.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
public final class PackageUtils {

	///CLOVER:OFF
	private PackageUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Get the package attribute for the supplied {@code type} using the
	 * supplied {@code function}.
	 *
	 * <p>This method only returns a non-empty {@link Optional} value holder
	 * if the class loader for the supplied type created a {@link Package}
	 * object and the supplied function does not return {@code null} when
	 * applied.
	 *
	 * @param type the type to get the package attribute for
	 * @param function a function that computes the package attribute value
	 * (e.g., {@code Package::getImplementationTitle}); never {@code null}
	 * @return an {@code Optional} containing the attribute value; never
	 * {@code null} but potentially empty
	 * @throws PreconditionViolationException if the supplied type or function
	 * is {@code null}
	 * @see Class#getPackage()
	 * @see Package#getImplementationTitle()
	 * @see Package#getImplementationVersion()
	 */
	public static Optional<String> getAttribute(Class<?> type, Function<Package, String> function) {
		Preconditions.notNull(type, "type must not be null");
		Preconditions.notNull(function, "function must not be null");
		Package typePackage = type.getPackage();
		if (typePackage != null) {
			String value = function.apply(typePackage);
			if (value != null) {
				return Optional.of(value);
			}
		}
		return Optional.empty();
	}

}
