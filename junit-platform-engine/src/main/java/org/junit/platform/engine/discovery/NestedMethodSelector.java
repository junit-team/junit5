/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;

@API(status = STABLE, since = "1.6")
public class NestedMethodSelector implements DiscoverySelector {

	private final List<Class<?>> enclosingClasses;
	private final Class<?> nestedClass;
	private final Method method;

	public NestedMethodSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass, Method method) {
		this.enclosingClasses = Preconditions.notEmpty(enclosingClasses, "enclosingClasses must not be null or empty");
		this.nestedClass = Preconditions.notNull(nestedClass, "nestedClass must not be null");
		this.method = Preconditions.notNull(method, "method must not be null");
	}

	public List<Class<?>> getEnclosingClasses() {
		return enclosingClasses;
	}

	public Class<?> getNestedClass() {
		return nestedClass;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NestedMethodSelector that = (NestedMethodSelector) o;
		return enclosingClasses.equals(that.enclosingClasses) && nestedClass.equals(that.nestedClass)
				&& method.equals(that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enclosingClasses, nestedClass, method);
	}

}
