/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Collection of utilities for working with Java reflection APIs.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
public final class ReflectionUtils {

	public enum MethodSortOrder {
		HierarchyDown, HierarchyUp
	}

	private ReflectionUtils() {
		/* no-op */
	}

	public static ClassLoader getDefaultClassLoader() {
		try {
			return Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			/* ignore */
		}
		return ClassLoader.getSystemClassLoader();
	}

	public static <T> T newInstance(Class<T> clazz, Object... args) {
		Preconditions.notNull(clazz, "class must not be null");

		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			makeAccessible(constructor);
			return constructor.newInstance(args);
		}
		catch (Exception ex) {
			handleException(ex);
		}

		// Appeasing the compiler: this should hopefully never happen...
		throw new IllegalStateException("Exception handling algorithm in ReflectionUtils is incomplete");
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		Preconditions.notNull(method, "method must not be null");
		Preconditions.notNull(target, "target must not be null");

		try {
			makeAccessible(method);
			return method.invoke(target, args);
		}
		catch (Exception ex) {
			handleException(ex);
		}

		// Appeasing the compiler: this should hopefully never happen...
		throw new IllegalStateException("Exception handling algorithm in ReflectionUtils is incomplete");
	}

	public static Optional<Class<?>> loadClass(String name) {
		return loadClass(name, getDefaultClassLoader());
	}

	public static Optional<Class<?>> loadClass(String name, ClassLoader classLoader) {
		Preconditions.notBlank(name, "class name must not be null or empty");
		Preconditions.notNull(classLoader, "ClassLoader must not be null");
		try {
			// TODO Add support for primitive types and arrays.
			return Optional.of(classLoader.loadClass(name));
		}
		catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	public static <T> Optional<Class<T>> loadClass(String name, Class<T> requiredType) {
		return loadClass(name, requiredType, getDefaultClassLoader());
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<Class<T>> loadClass(String name, Class<T> requiredType, ClassLoader classLoader) {
		Preconditions.notBlank(name, "class name must not be null or empty");
		Preconditions.notNull(requiredType, "requiredType must not be null");
		Preconditions.notNull(classLoader, "ClassLoader must not be null");

		try {
			// TODO Add support for primitive types and arrays.
			Class<?> clazz = classLoader.loadClass(name);
			if (requiredType.isAssignableFrom(clazz)) {
				return Optional.of((Class<T>) clazz);
			}
			else {
				throw new IllegalStateException(
					String.format("Class [%s] is not of required type [%s]", name, requiredType.getName()));
			}
		}
		catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	public static Class<?>[] findAllClassesInPackage(String basePackageName) {
		return new ClasspathScanner(basePackageName).scanForClassesRecursively();
	}

	public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "methodName must not be null or empty");

		Predicate<Method> nameAndParameterTypesMatch = (method -> method.getName().equals(methodName)
				&& Arrays.equals(method.getParameterTypes(), parameterTypes));

		List<Method> candidates = findMethods(clazz, nameAndParameterTypesMatch);
		return (!candidates.isEmpty() ? Optional.of(candidates.get(0)) : Optional.empty());
	}

	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate) {
		return findMethods(clazz, predicate, MethodSortOrder.HierarchyDown);
	}

	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");
		Preconditions.notNull(sortOrder, "MethodSortOrder must not be null");

		// @formatter:off
		return findAllMethodsInHierarchy(clazz, sortOrder).stream()
				.filter(predicate)
				.collect(toList());
		// @formatter:on
	}

	/**
	 * Return all methods in superclass hierarchy except from Object.
	 */
	public static List<Method> findAllMethodsInHierarchy(Class<?> clazz, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(sortOrder, "MethodSortOrder must not be null");

		// TODO Support interface default methods.
		// TODO Determine if we need to support bridged methods.

		List<Method> localMethods = Arrays.asList(clazz.getDeclaredMethods());

		// @formatter:off
		List<Method> superclassMethods = getSuperclassMethods(clazz, sortOrder).stream()
				.filter(method -> !isMethodShadowedByLocalMethods(method, localMethods))
				.collect(toList());
		// @formatter:on

		// @formatter:off
		List<Method> interfaceMethods = getInterfaceMethods(clazz, sortOrder).stream()
				.filter(method -> !isMethodShadowedByLocalMethods(method, localMethods))
				.collect(toList());
		// @formatter:on

		List<Method> methods = new ArrayList<>();
		if (sortOrder == MethodSortOrder.HierarchyDown) {
			methods.addAll(superclassMethods);
			methods.addAll(interfaceMethods);
		}
		methods.addAll(localMethods);
		if (sortOrder == MethodSortOrder.HierarchyUp) {
			methods.addAll(interfaceMethods);
			methods.addAll(superclassMethods);
		}
		return methods;
	}

	private static List<Method> getInterfaceMethods(Class<?> clazz, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(sortOrder, "MethodSortOrder must not be null");

		List<Method> allInterfaceMethods = new ArrayList<>();
		for (Class<?> ifc : clazz.getInterfaces()) {

			List<Method> localMethods = Arrays.stream(ifc.getDeclaredMethods()).filter(
				method -> method.isDefault()).collect(Collectors.toList());

			// @formatter:off
			List<Method> subInterfaceMethods = getInterfaceMethods(ifc, sortOrder).stream()
					.filter(method -> !isMethodShadowedByLocalMethods(method, localMethods))
					.collect(toList());
			// @formatter:on

			if (sortOrder == MethodSortOrder.HierarchyDown) {
				allInterfaceMethods.addAll(subInterfaceMethods);
			}
			allInterfaceMethods.addAll(localMethods);
			if (sortOrder == MethodSortOrder.HierarchyUp) {
				allInterfaceMethods.addAll(subInterfaceMethods);
			}
		}
		return allInterfaceMethods;

	}

	private static List<Method> getSuperclassMethods(Class<?> clazz, MethodSortOrder sortOrder) {
		if (clazz.getSuperclass() != Object.class) {
			return findAllMethodsInHierarchy(clazz.getSuperclass(), sortOrder);
		}
		else {
			return Collections.emptyList();
		}
	}

	private static boolean isMethodShadowedByLocalMethods(Method method, List<Method> localMethods) {
		return localMethods.stream().anyMatch(local -> isMethodShadowedBy(method, local));
	}

	private static boolean isMethodShadowedBy(Method upper, Method lower) {
		if (!lower.getName().equals(upper.getName())) {
			return false;
		}
		Class<?>[] lowerParameterTypes = lower.getParameterTypes();
		Class<?>[] upperParameterTypes = upper.getParameterTypes();
		if (lowerParameterTypes.length != upperParameterTypes.length) {
			return false;
		}
		for (int i = 0; i < lowerParameterTypes.length; i++) {
			if (!lowerParameterTypes[i].equals(upperParameterTypes[i])) {
				return false;
			}
		}
		return true;
	}

	private static void makeAccessible(AccessibleObject object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
	}

	private static void handleException(Throwable ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("No such method or constructor", ex);
		}
		if (ex instanceof InstantiationException) {
			throw new IllegalStateException("Instantiation failed", ex);
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Failed to access method or constructor", ex);
		}
		if (ex instanceof InvocationTargetException) {
			handleException(((InvocationTargetException) ex).getTargetException());
		}
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if (ex instanceof Error) {
			throw (Error) ex;
		}
	}

}
