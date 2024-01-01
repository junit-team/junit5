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

import java.net.URI;
import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a <em>classpath root</em> so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can search for class
 * files or resources within the physical classpath &mdash; for example, to
 * scan for test classes.
 *
 * <p>Since {@linkplain org.junit.platform.engine.TestEngine engines} are not
 * expected to modify the classpath, the classpath root represented by this
 * selector must be on the classpath of the
 * {@linkplain Thread#getContextClassLoader() context class loader} of the
 * {@linkplain Thread thread} that uses this selector.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectClasspathRoots(java.util.Set)
 * @see ClasspathResourceSelector
 * @see Thread#getContextClassLoader()
 */
@API(status = STABLE, since = "1.0")
public class ClasspathRootSelector implements DiscoverySelector {

	private final URI classpathRoot;

	ClasspathRootSelector(URI classpathRoot) {
		this.classpathRoot = classpathRoot;
	}

	/**
	 * Get the selected classpath root directory as an {@link URI}.
	 */
	public URI getClasspathRoot() {
		return this.classpathRoot;
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
		ClasspathRootSelector that = (ClasspathRootSelector) o;
		return Objects.equals(this.classpathRoot, that.classpathRoot);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return this.classpathRoot.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("classpathRoot", this.classpathRoot).toString();
	}

}
