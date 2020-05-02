/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Objects;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestSource;

/**
 * Class based {@link org.junit.platform.engine.TestSource TestSource} with
 * an optional {@linkplain FilePosition file position}.
 *
 * <p>If a Java {@link Class} reference is provided, the {@code ClassSource}
 * will contain that {@code Class} and its class name accordingly. If a class
 * name is provided, the {@code ClassSource} will contain the class name and
 * will only attempt to lazily load the {@link Class} if {@link #getJavaClass()}
 * is invoked.
 *
 * <p>In this context, Java {@link Class} means anything that can be referenced
 * as a {@link Class} on the JVM &mdash; for example, classes from other JVM
 * languages such Groovy, Scala, etc.
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.ClassSelector
 */
@API(status = STABLE, since = "1.0")
public class ClassSource implements TestSource {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@code ClassSource} using the supplied class name.
	 *
	 * @param className the class name; must not be {@code null} or blank
	 */
	public static ClassSource from(String className) {
		return new ClassSource(className);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied class name and
	 * {@linkplain FilePosition file position}.
	 *
	 * @param className the class name; must not be {@code null} or blank
	 * @param filePosition the position in the source file; may be {@code null}
	 */
	public static ClassSource from(String className, FilePosition filePosition) {
		return new ClassSource(className, filePosition);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied {@linkplain Class class}.
	 *
	 * @param javaClass the Java class; must not be {@code null}
	 */
	public static ClassSource from(Class<?> javaClass) {
		return new ClassSource(javaClass);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied {@linkplain Class class}
	 * and {@linkplain FilePosition file position}.
	 *
	 * @param javaClass the Java class; must not be {@code null}
	 * @param filePosition the position in the Java source file; may be {@code null}
	 */
	public static ClassSource from(Class<?> javaClass, FilePosition filePosition) {
		return new ClassSource(javaClass, filePosition);
	}

	private final String className;
	private final FilePosition filePosition;
	private Class<?> javaClass;

	private ClassSource(String className) {
		this(className, null);
	}

	private ClassSource(String className, FilePosition filePosition) {
		this.className = Preconditions.notBlank(className, "Class name must not be null or blank");
		this.filePosition = filePosition;
	}

	private ClassSource(Class<?> javaClass) {
		this(javaClass, null);
	}

	private ClassSource(Class<?> javaClass, FilePosition filePosition) {
		this.javaClass = Preconditions.notNull(javaClass, "Class must not be null");
		this.className = this.javaClass.getName();
		this.filePosition = filePosition;
	}

	/**
	 * Get the class name of this source.
	 *
	 * @see #getJavaClass()
	 * @see #getPosition()
	 */
	public final String getClassName() {
		return this.className;
	}

	/**
	 * Get the {@linkplain Class Java class} of this source.
	 *
	 * <p>If the {@link Class} was not provided, but only the name, this method
	 * attempts to lazily load the {@link Class} based on its name and throws a
	 * {@link PreconditionViolationException} if the class cannot be loaded.
	 *
	 * @see #getClassName()
	 * @see #getPosition()
	 */
	public final Class<?> getJavaClass() {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.tryToLoadClass(this.className).getOrThrow(
				cause -> new PreconditionViolationException("Could not load class with name: " + this.className,
					cause));
		}
		return this.javaClass;
	}

	/**
	 * Get the {@linkplain FilePosition position} in the source file for
	 * the associated {@linkplain #getClassName class}, if available.
	 *
	 * @see #getClassName()
	 * @see #getJavaClass()
	 */
	public final Optional<FilePosition> getPosition() {
		return Optional.ofNullable(this.filePosition);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ClassSource that = (ClassSource) o;
		return Objects.equals(this.className, that.className) && Objects.equals(this.filePosition, that.filePosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.className, this.filePosition);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("filePosition", this.filePosition)
				.toString();
		// @formatter:on
	}

}
