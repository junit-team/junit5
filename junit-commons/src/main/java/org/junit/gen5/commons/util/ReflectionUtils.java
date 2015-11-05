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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class ReflectionUtils {

	public enum MethodSortOrder {
		HierarchyDown, HierarchyUp
	}

	private ReflectionUtils() {
		/* no-op */
	}

	public static <T> T newInstance(Class<T> clazz)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<T> constructor = clazz.getDeclaredConstructor();
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}
		return constructor.newInstance();
	}

	public static Object invokeMethod(Method method, Object testInstance)
			throws IllegalAccessException, InvocationTargetException {
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		return method.invoke(testInstance);
	}

	public static Class<?> loadClass(String name) {
		try {
			// TODO Use correct classloader
			// TODO Add support for primitive types and arrays.
			return ClassLoader.getSystemClassLoader().loadClass(name);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to load class with name '" + name + "'.", e);
		}
	}

	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");

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
		List<Method> allInterfaceMethods = new ArrayList<>();
		for (Class<?> anInterface : clazz.getInterfaces()) {

			List<Method> localMethods = Arrays.stream(anInterface.getDeclaredMethods()).filter(
				method -> method.isDefault()).collect(Collectors.toList());

			// @formatter:off
			List<Method> subInterfaceMethods = getInterfaceMethods(anInterface, sortOrder).stream()
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
		//		System.out.println("INTERFACE METHODS: " + interfaceMethods);
		//		System.out.println();
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

}
