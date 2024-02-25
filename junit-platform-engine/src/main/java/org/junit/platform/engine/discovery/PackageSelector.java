/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a package name so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on packages.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectPackage(String)
 * @see org.junit.platform.engine.support.descriptor.PackageSource
 */
@API(status = STABLE, since = "1.0")
public class PackageSelector implements DiscoverySelector {

	private final String packageName;

	PackageSelector(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Get the selected package name.
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PackageSelector that = (PackageSelector) o;
		return Objects.equals(this.packageName, that.packageName);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return this.packageName.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("packageName", this.packageName).toString();
	}

}
