/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Objects;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.discovery.ClassSelector;

/**
 * Java class based {@link org.junit.platform.engine.TestSource} with an optional
 * {@linkplain FilePosition position}.
 *
 * @since 1.0
 * @see ClassSelector
 */
@API(status = STABLE, since = "1.0")
public class ClassSource implements TestSource {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@code ClassSource} using the supplied
	 * className.
	 *
	 * @param className the Java class name; must not be {@code null}
	 */
	public static ClassSource from(String className) {
		return new ClassSource(className);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied
	 * className and {@link FilePosition filePosition}.
	 *
	 * @param className the Java class name; must not be {@code null}
	 * @param filePosition the position in the Java source file; may be {@code null}
	 */
	public static ClassSource from(String className, FilePosition filePosition) {
		return new ClassSource(className, filePosition);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied
	 * {@link Class javaClass}.
	 *
	 * @param javaClass the Java class; must not be {@code null}
	 */
	public static ClassSource from(Class<?> javaClass) {
		return new ClassSource(javaClass);
	}

	/**
	 * Create a new {@code ClassSource} using the supplied
	 * {@link Class javaClass} and {@link FilePosition filePosition}.
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
		this.className = className;
		this.filePosition = filePosition;
	}

	private ClassSource(Class<?> javaClass) {
		this(javaClass, null);
	}

	private ClassSource(Class<?> javaClass, FilePosition filePosition) {
		this.javaClass = Preconditions.notNull(javaClass, "class must not be null");
		this.className = this.javaClass.getName();
		this.filePosition = filePosition;
	}

	/**
	 * Get the class name of this source.
	 *
	 * @see #getPosition()
	 */
	public final String getClassName() {
		return this.className;
	}

	/**
	 * Get the {@linkplain Class Java class} of this source.
	 *
	 * @see #getPosition()
	 */
	public final Class<?> getJavaClass() {
		if (this.javaClass == null) {
			this.javaClass = ReflectionUtils.loadClass(this.className).orElseThrow(
				() -> new PreconditionViolationException("Could not load class with name: " + this.className));
		}
		return this.javaClass;
	}

	/**
	 * Get the {@linkplain FilePosition position} in the Java source file for
	 * the associated {@linkplain #getJavaClass Java class}, if available.
	 *
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
		return Objects.equals(this.javaClass, that.javaClass) && Objects.equals(this.filePosition, that.filePosition);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.javaClass, this.filePosition);
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
