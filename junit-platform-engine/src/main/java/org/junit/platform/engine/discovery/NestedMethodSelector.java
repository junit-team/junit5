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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a nested {@link Method}
 * or a combination of enclosing classes names, class name, method
 * name, and parameter types so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on methods.
 *
 * <p>If a Java {@link Method} is provided, the selector will return that
 * {@linkplain #getMethod() method} and its method name, class name, enclosing
 * classes names and parameter types accordingly. If class or methods names are
 * provided, this selector will only attempt to lazily load the {@link Class}
 * and {@link Method} if {@link #getEnclosingClasses()},
 * {@link #getNestedClass()} or {@link #getMethod()} is invoked.
 *
 * <p>In this context, a Java {@code Method} means anything that can be referenced
 * as a {@link Method} on the JVM &mdash; for example, methods from Java classes
 * or methods from other JVM languages such Groovy, Scala, etc.
 *
 * @since 1.6
 * @see DiscoverySelectors#selectNestedMethod(List, String, String)
 * @see DiscoverySelectors#selectNestedMethod(List, String, String, String)
 * @see DiscoverySelectors#selectNestedMethod(List, Class, String)
 * @see DiscoverySelectors#selectNestedMethod(List, Class, String, String)
 * @see DiscoverySelectors#selectNestedMethod(List, Class, Method)
 * @see org.junit.platform.engine.support.descriptor.MethodSource
 * @see NestedClassSelector
 * @see MethodSelector
 */
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

	/**
	 * Get the names of the classes enclosing the nested class
	 * containing the selected method.
	 */
	public List<String> getEnclosingClassNames() {
		return nestedClassSelector.getEnclosingClassNames();
	}

	/**
	 * Get the list of {@link Class} enclosing the nested {@link Class}
	 * containing the selected {@link Method}.
	 *
	 * <p>If the {@link Class} were not provided, but only the name of the
	 * nested class and its enclosing classes, this method attempts to lazily
	 * load the list of enclosing {@link Class} and throws a
	 * {@link PreconditionViolationException} if the classes cannot be loaded.
	 */
	public List<Class<?>> getEnclosingClasses() {
		return nestedClassSelector.getEnclosingClasses();
	}

	/**
	 * Get the name of the nested class containing the selected method.
	 */
	public String getNestedClassName() {
		return nestedClassSelector.getNestedClassName();
	}

	/**
	 * Get the nested {@link Class} containing the selected {@link Method}.
	 *
	 * <p>If the {@link Class} were not provided, but only the name of the
	 * nested class and its enclosing classes, this method attempts to lazily
	 * load the nested {@link Class} and throws a
	 * {@link PreconditionViolationException} if the class cannot be loaded.
	 */
	public Class<?> getNestedClass() {
		return nestedClassSelector.getNestedClass();
	}

	/**
	 * Get the name of the selected method.
	 */
	public String getMethodName() {
		return methodSelector.getMethodName();
	}

	/**
	 * Get the selected {@link Method}.
	 *
	 * <p>If the {@link Method} was not provided, but only the name, this method
	 * attempts to lazily load the {@code Method} based on its name and throws a
	 * {@link PreconditionViolationException} if the method cannot be loaded.
	 */
	public Method getMethod() {
		return methodSelector.getJavaMethod();
	}

	/**
	 * Get the parameter types for the selected method as a {@link String},
	 * typically a comma-separated list of primitive types, fully qualified
	 * class names, or array types.
	 *
	 * <p>Note: the parameter types are provided as a single string instead of
	 * a collection in order to allow this selector to be used in a generic
	 * fashion by various test engines. It is therefore the responsibility of
	 * the caller of this method to determine how to parse the returned string.
	 *
	 * @return the parameter types supplied to this {@code NestedMethodSelector}
	 * via a constructor or deduced from a {@code Method} supplied via a
	 * constructor; never {@code null}
	 */
	public String getMethodParameterTypes() {
		return methodSelector.getMethodParameterTypes();
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

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("enclosingClassNames", getEnclosingClassNames()) //
				.append("nestedClassName", getNestedClassName()) //
				.append("methodName", getMethodName()) //
				.append("methodParameterTypes", getMethodParameterTypes()) //
				.toString();
	}

}
