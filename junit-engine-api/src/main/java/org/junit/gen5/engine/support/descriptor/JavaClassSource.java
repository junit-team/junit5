/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Objects;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * @since 5.0
 */
@API(Experimental)
public class JavaClassSource implements JavaSource {

	private static final long serialVersionUID = 1L;

	private final Class<?> javaClass;

	public JavaClassSource(Class<?> javaClass) {
		this.javaClass = Preconditions.notNull(javaClass, "class must not be null");
	}

	public Class<?> getJavaClass() {
		return javaClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JavaClassSource that = (JavaClassSource) o;
		return Objects.equals(this.javaClass, that.javaClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(javaClass);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("javaClass", javaClass.getName()).toString();
	}

}
