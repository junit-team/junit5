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
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects the name of a <em>classpath resource</em>
 * so that {@link org.junit.platform.engine.TestEngine TestEngines} can load resources
 * from the classpath &mdash; for example, to load XML or JSON files from the classpath,
 * potentially within JARs.
 *
 * <p>Since {@linkplain org.junit.platform.engine.TestEngine engines} are not
 * expected to modify the classpath, the classpath resource represented by this
 * selector must be on the classpath of the
 * {@linkplain Thread#getContextClassLoader() context class loader} of the
 * {@linkplain Thread thread} that uses it.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectClasspathResource(String)
 * @see ClasspathRootSelector
 * @see #getClasspathResourceName()
 */
@API(status = STABLE, since = "1.0")
public class ClasspathResourceSelector implements DiscoverySelector {

	private final String classpathResourceName;
	private final FilePosition position;

	ClasspathResourceSelector(String classpathResourceName, FilePosition position) {
		boolean startsWithSlash = classpathResourceName.startsWith("/");
		this.classpathResourceName = (startsWithSlash ? classpathResourceName.substring(1) : classpathResourceName);
		this.position = position;
	}

	/**
	 * Get the name of the selected classpath resource.
	 *
	 * <p>The name of a <em>classpath resource</em> must follow the semantics
	 * for resource paths as defined in {@link ClassLoader#getResource(String)}.
	 *
	 * @see ClassLoader#getResource(String)
	 * @see ClassLoader#getResourceAsStream(String)
	 * @see ClassLoader#getResources(String)
	 */
	public String getClasspathResourceName() {
		return this.classpathResourceName;
	}

	/**
	 * Get the selected {@code FilePosition} within the classpath resource.
	 */
	public Optional<FilePosition> getPosition() {
		return Optional.ofNullable(this.position);
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
		ClasspathResourceSelector that = (ClasspathResourceSelector) o;
		return Objects.equals(this.classpathResourceName, that.classpathResourceName)
				&& Objects.equals(this.position, that.position);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return Objects.hash(this.classpathResourceName, this.position);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("classpathResourceName", this.classpathResourceName).append("position",
			this.position).toString();
	}

}
