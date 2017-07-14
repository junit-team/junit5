/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.support;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Common reflection and classpath scanning support.
 *
 * @since 1.0
 */
@API(Maintained)
public final class ReflectionSupport {

	///CLOVER:OFF
	private ReflectionSupport() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Find all {@linkplain Class classes} of the supplied {@code root}
	 * {@linkplain URI} that match the specified {@code classTester} and
	 * {@code classNameFilter} predicates.
	 *
	 * @param root the root URI to start scanning
	 * @param classTester the class type filter; never {@code null}
	 * @param classNameFilter the class name filter; never {@code null}
	 * @return the list of all such classes found; neither {@code null} nor mutable
	 */
	public static List<Class<?>> findAllClassesInClasspathRoot(URI root, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return ReflectionUtils.findAllClassesInClasspathRoot(root, classTester, classNameFilter);
	}

	/**
	 * Find all {@linkplain Class classes} of the supplied {@code basePackageName}
	 * that match the specified {@code classTester} and {@code classNameFilter}
	 * predicates.
	 *
	 * @param basePackageName the base package name to start scanning
	 * @param classTester the class type filter; never {@code null}
	 * @param classNameFilter the class name filter; never {@code null}
	 * @return the list of all such classes found; neither {@code null} nor mutable
	 */
	public static List<Class<?>> findAllClassesInPackage(String basePackageName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return ReflectionUtils.findAllClassesInPackage(basePackageName, classTester, classNameFilter);
	}

	/**
	 * Find all {@linkplain Method methods} of the supplied class or interface
	 * that match the specified {@code predicate}.
	 *
	 * <p>If you're are looking for methods annotated with a certain annotation
	 * type, consider using {@linkplain AnnotationSupport#findAnnotatedMethods(Class, Class, HierarchyTraversalMode)}.
	 *
	 * @param clazz the class or interface in which to find the methods; never {@code null}
	 * @param predicate the method filter; never {@code null}
	 * @param traversalMode the hierarchy traversal mode; never {@code null}
	 * @return the list of all such methods found; neither {@code null} nor mutable
	 */
	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate,
			HierarchyTraversalMode traversalMode) {

		return ReflectionUtils.findMethods(clazz, predicate,
			ReflectionUtils.HierarchyTraversalMode.valueOf(traversalMode.name()));
	}

	/**
	 * Load a class by its <em>primitive name</em> or <em>fully qualified name</em>,
	 * using the default {@link ClassLoader}.
	 *
	 * @param name the name of the class to load; never {@code null} or blank
	 */
	public static Optional<Class<?>> loadClass(String name) {
		return ReflectionUtils.loadClass(name);
	}

	/**
	 * Find the first {@link Method} of the supplied class or interface that
	 * meets the specified criteria, beginning with the specified class or
	 * interface and traversing up the type hierarchy until such a method is
	 * found or the type hierarchy is exhausted.
	 *
	 * <p>Note, however, that the current algorithm traverses the entire
	 * type hierarchy even after having found a match.
	 *
	 * @param clazz the class or interface in which to find the method; never {@code null}
	 * @param methodName the name of the method to find; never {@code null} or empty
	 * @param parameterTypeNames the fully qualified names of the types of parameters
	 * accepted by the method, if any, provided as a comma-separated list
	 * @return an {@code Optional} containing the method found; never {@code null}
	 * but potentially empty if no such method could be found
	 * @see #findMethod(Class, String, Class...)
	 * @see ReflectionUtils.HierarchyTraversalMode#BOTTOM_UP
	 */
	public static Optional<Method> findMethod(Class<?> clazz, String methodName, String parameterTypeNames) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypeNames);
	}

	/**
	 * Find the first {@link Method} of the supplied class or interface that
	 * meets the specified criteria, beginning with the specified class or
	 * interface and traversing up the type hierarchy until such a method is
	 * found or the type hierarchy is exhausted.
	 *
	 * <p>Note, however, that the current algorithm traverses the entire
	 * type hierarchy even after having found a match.
	 *
	 * @param clazz the class or interface in which to find the method; never {@code null}
	 * @param methodName the name of the method to find; never {@code null} or empty
	 * @param parameterTypes the types of parameters accepted by the method, if any;
	 * never {@code null}
	 * @return an {@code Optional} containing the method found; never {@code null}
	 * but potentially empty if no such method could be found
	 * @see #findMethod(Class, String, String)
	 * @see ReflectionUtils.HierarchyTraversalMode#BOTTOM_UP
	 */
	public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes);
	}

	/**
	 * Create a new instance of the specified {@link Class} by invoking
	 * the constructor whose argument list matches the types of the supplied
	 * arguments.
	 *
	 * <p>The constructor will be made accessible if necessary, and any checked
	 * exception will be {@linkplain ExceptionUtils#throwAsUncheckedException masked}
	 * as an unchecked exception.
	 *
	 * @param clazz the class to instantiate; never {@code null}
	 * @param args the arguments to pass to the constructor none of which may be {@code null}
	 * @return the new instance
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	public static <T> T newInstance(Class<T> clazz, Object... args) {
		return ReflectionUtils.newInstance(clazz, args);
	}

	/**
	 * Invoke the supplied method, making it accessible if necessary and
	 * {@linkplain ExceptionUtils#throwAsUncheckedException masking} any
	 * checked exception as an unchecked exception.
	 *
	 * @param method the method to invoke; never {@code null}
	 * @param target the object on which to invoke the method; may be
	 * {@code null} if the method is {@code static}
	 * @param args the arguments to pass to the method
	 * @return the value returned by the method invocation or {@code null}
	 * if the return type is {@code void}
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		return ReflectionUtils.invokeMethod(method, target, args);
	}

	/**
	 * Find all nested classes of the given class conforming to the given predicate.
	 *
	 * @param clazz the class to be searched; never {@code null}
	 * @param predicate the predicate against which the list of nested classes is
	 * checked; never {@code null}
	 * @return the list of all such classes found; never {@code null}
	 */
	public static List<Class<?>> findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		return ReflectionUtils.findNestedClasses(clazz, predicate);
	}

}
