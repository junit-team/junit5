/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a Java package name so that
 * {@link org.junit.gen5.engine.TestEngine TestEngines} can discover
 * tests or containers based on packages.
 *
 * @since 5.0
 */
@API(Experimental)
public class PackageSelector implements DiscoverySelector {

	/**
	 * Create a {@code PackageSelector} for the supplied package name.
	 *
	 * @param packageName the package name to select; never {@code null} or empty
	 */
	public static PackageSelector forPackageName(String packageName) {
		Preconditions.notBlank(packageName, "Package name must not be null or empty");
		return new PackageSelector(packageName);
	}

	private final String packageName;

	private PackageSelector(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Get the selected package name.
	 */
	public String getPackageName() {
		return this.packageName;
	}

}
