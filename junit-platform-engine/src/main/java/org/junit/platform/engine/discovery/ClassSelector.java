/*
 * Copyright 2015-2020 the original author or authors.
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
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Class} or class name so
 * that {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on classes.
 *
 * <p>If a Java {@link Class} reference is provided, the selector will return
 * that {@code Class} and its class name accordingly. If a class name is
 * provided, the selector will only attempt to lazily load the {@link Class}
 * if {@link #getJavaClass()} is invoked.
 *
 * <p>In this context, Java {@link Class} means anything that can be referenced
 * as a {@link Class} on the JVM &mdash; for example, classes from other JVM
 * languages such Groovy, Scala, etc.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectClass(String)
 * @see DiscoverySelectors#selectClass(Class)
 * @see org.junit.platform.engine.support.descriptor.ClassSource
 */
@API(status = STABLE, since = "1.0")
public class ClassSelector implements DiscoverySelector {

	private final String className;

	private Class<?> javaClass;

	ClassSelector(String className) {
		this.className = className;
	}

	ClassSelector(Class<?> javaClass) {
		this.className = javaClass.getName();
		this.javaClass = javaClass;
	}

	/**
	 * Get the selected class name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the selected {@link Class}.
	 *
	 * <p>If the {@link Class} was not provided, but only the name, this method
	 * attempts to lazily load the {@link Class} based on its name and throws a
	 * {@link PreconditionViolationException} if the class cannot be loaded.
	 */
	public Class<?> getJavaClass() {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.tryToLoadClass(this.className).getOrThrow(
				cause -> new PreconditionViolationException("Could not load class with name: " + this.className,
					cause));
		}
		return this.javaClass;
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
		ClassSelector that = (ClassSelector) o;
		return Objects.equals(this.className, that.className);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return this.className.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("className", this.className).toString();
	}

}
