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
import org.junit.platform.engine.DiscoverySelector;

@API(status = STABLE, since = "1.6")
public class NestedMethodSelector implements DiscoverySelector {

	private final NestedClassSelector nestedClassSelector;
	private final MethodSelector methodSelector;

	NestedMethodSelector(List<String> enclosingClassNames, String nestedClassName, String methodName) {
		this.nestedClassSelector = new NestedClassSelector(enclosingClassNames, nestedClassName);
		this.methodSelector = new MethodSelector(nestedClassName, methodName);
	}

	NestedMethodSelector(List<String> enclosingClassNames, String nestedClassName, String methodName,
			String methodParameterTypes) {
		this.nestedClassSelector = new NestedClassSelector(enclosingClassNames, nestedClassName);
		this.methodSelector = new MethodSelector(nestedClassName, methodName, methodParameterTypes);
	}

	NestedMethodSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass, String methodName) {
		this.nestedClassSelector = new NestedClassSelector(enclosingClasses, nestedClass);
		this.methodSelector = new MethodSelector(nestedClass, methodName);
	}

	NestedMethodSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass, String methodName,
			String methodParameterTypes) {
		this.nestedClassSelector = new NestedClassSelector(enclosingClasses, nestedClass);
		this.methodSelector = new MethodSelector(nestedClass, methodName, methodParameterTypes);
	}

	NestedMethodSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass, Method method) {
		this.nestedClassSelector = new NestedClassSelector(enclosingClasses, nestedClass);
		this.methodSelector = new MethodSelector(nestedClass, method);
	}

	public List<Class<?>> getEnclosingClasses() {
		return nestedClassSelector.getEnclosingClasses();
	}

	public Class<?> getNestedClass() {
		return nestedClassSelector.getNestedClass();
	}

	public Method getMethod() {
		return methodSelector.getJavaMethod();
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
		return nestedClassSelector.equals(that.nestedClassSelector) && methodSelector.equals(that.methodSelector);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nestedClassSelector, methodSelector);
	}

}
