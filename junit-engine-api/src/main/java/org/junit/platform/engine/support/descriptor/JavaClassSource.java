/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Java class based {@link org.junit.platform.engine.TestSource}.
 *
 * @since 5.0
 */
@API(Experimental)
public class JavaClassSource implements JavaSource {

	private static final long serialVersionUID = 1L;

	private final Class<?> javaClass;

	/**
	 * Create a new {@code JavaClassSource} using the supplied
	 * {@link Class javaClass}.
	 *
	 * @param javaClass the Java class; must not be {@code null}
	 */
	public JavaClassSource(Class<?> javaClass) {
		this.javaClass = Preconditions.notNull(javaClass, "class must not be null");
	}

	/**
	 * Get the {@linkplain Class Java class} of this source.
	 */
	public final Class<?> getJavaClass() {
		return this.javaClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JavaClassSource that = (JavaClassSource) o;
		return this.javaClass.equals(that.javaClass);
	}

	@Override
	public int hashCode() {
		return this.javaClass.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("javaClass", this.javaClass.getName()).toString();
	}

}
