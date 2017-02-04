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

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;
import java.util.jar.Manifest;

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
			return Optional.ofNullable(function.apply(typePackage));
		}
		return Optional.empty();
	}

	/**
	 * Get the value of the specified attribute name, specified as a string,
	 * or an empty {@link Optional} if the attribute was not found. The attribute
	 * name is case-insensitive.
	 *
	 * <p>This method also returns an empty {@link Optional} value holder
	 * if any exception is caught while loading the manifest file via the
	 * specified class loader.
	 *
	 * @param loader the {@link ClassLoader} used to load the manifest with
	 * @param name the attribute name as a string
	 * @return an {@code Optional} containing the attribute value; never
	 * {@code null} but potentially empty
	 * @throws PreconditionViolationException if the supplied loader is
	 * {@code null} or the specified name is blank
	 * @see Manifest
	 */
	public static Optional<String> getAttribute(ClassLoader loader, String name) {
		Preconditions.notNull(loader, "loader must not be null");
		Preconditions.notBlank(name, "name must not be blank");
		try (InputStream stream = loader.getResourceAsStream("META-INF/MANIFEST.MF")) {
			Manifest manifest = new Manifest(stream);
			return Optional.ofNullable(manifest.getMainAttributes().getValue(name));
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

}
