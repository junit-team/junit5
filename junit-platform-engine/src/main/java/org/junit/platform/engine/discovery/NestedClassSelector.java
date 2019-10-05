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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;

@API(status = STABLE, since = "1.6")
public class NestedClassSelector implements DiscoverySelector {

	private final List<String> enclosingClassNames;
	private final String nestedClassName;

	private List<Class<?>> enclosingClasses;
	private Class<?> nestedClass;

	NestedClassSelector(List<String> enclosingClassNames, String nestedClassName) {
		this.enclosingClassNames = enclosingClassNames;
		this.nestedClassName = nestedClassName;
	}

	NestedClassSelector(List<Class<?>> enclosingClasses, Class<?> nestedClass) {
		this.enclosingClasses = enclosingClasses;
		this.nestedClass = nestedClass;
		this.enclosingClassNames = enclosingClasses.stream().map(Class::getName).collect(Collectors.toList());
		this.nestedClassName = nestedClass.getName();
	}

	public List<Class<?>> getEnclosingClasses() {
		lazyLoadEnclosingClasses();
		return enclosingClasses;
	}

	public Class<?> getNestedClass() {
		lazyLoadNestedClass();
		return nestedClass;
	}

	private void lazyLoadEnclosingClasses() {
		if (enclosingClasses == null) {
			enclosingClasses = enclosingClassNames.stream() //
					.map(this::loadJavaClass).collect(Collectors.toList());
		}
	}

	private void lazyLoadNestedClass() {
		if (nestedClass == null) {
			nestedClass = loadJavaClass(nestedClassName);
		}
	}

	private Class<?> loadJavaClass(String className) {
		return ReflectionUtils.tryToLoadClass(className).getOrThrow(
			cause -> new PreconditionViolationException("Could not load class with name: " + className, cause));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NestedClassSelector that = (NestedClassSelector) o;
		return enclosingClassNames.equals(that.enclosingClassNames) && nestedClassName.equals(that.nestedClassName)
				&& enclosingClasses.equals(that.enclosingClasses) && nestedClass.equals(that.nestedClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enclosingClassNames, nestedClassName, enclosingClasses, nestedClass);
	}

}
