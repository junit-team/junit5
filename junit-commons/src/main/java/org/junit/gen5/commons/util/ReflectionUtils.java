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
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Collection of utilities for working with Java reflection APIs.
 *
 * @since 5.0
 */
public final class ReflectionUtils {

	private static ClassLoader explicitClassloader = null;

	public enum MethodSortOrder {
		HierarchyDown, HierarchyUp
	}

	private ReflectionUtils() {
		/* no-op */
	}

	public static void setDefaultClassLoader(ClassLoader classLoader) {
		explicitClassloader = classLoader;
	}

	public static ClassLoader getDefaultClassLoader() {
		if (explicitClassloader != null)
			return explicitClassloader;
		try {
			return Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			/* ignore */
		}
		return ClassLoader.getSystemClassLoader();
	}

	public static boolean isPrivate(Class<?> clazz) {
		return Modifier.isPrivate(clazz.getModifiers());
	}

	public static boolean isPublic(Class<?> clazz) {
		return Modifier.isPublic(clazz.getModifiers());
	}

	public static boolean isPrivate(Member member) {
		return Modifier.isPrivate(member.getModifiers());
	}

	public static boolean isPublic(Member member) {
		return Modifier.isPublic(member.getModifiers());
	}

	public static boolean isAbstract(Class<?> clazz) {
		return Modifier.isAbstract(clazz.getModifiers());
	}

	public static boolean isAbstract(Member member) {
		return Modifier.isAbstract(member.getModifiers());
	}

	public static boolean isStatic(Class<?> clazz) {
		return Modifier.isStatic(clazz.getModifiers());
	}

	public static boolean isStatic(Member member) {
		return Modifier.isStatic(member.getModifiers());
	}

	public static <T> T newInstance(Class<T> clazz, Object... args) {
		Preconditions.notNull(clazz, "class must not be null");

		try {
			Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
			Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
			makeAccessible(constructor);
			return constructor.newInstance(args);
		}
		catch (Throwable ex) {
			handleException(ex);
		}

		// Appeasing the compiler: this should hopefully never happen...
		throw new IllegalStateException("Exception handling algorithm in ReflectionUtils is incomplete");
	}

	/**
	 * Invoke the supplied method, making it accessible if necessary and
	 * wrapping any checked exception in an {@code IllegalStateException}.
	 *
	 * @param method the method to invoke
	 * @param target the object on which to invoke the method; may be
	 * {@code null} if the method is {@code static}
	 * @param args the arguments to pass to the method
	 * @return the value returned by the method invocation or {@code null}
	 * if the return type is {@code void}
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		Preconditions.notNull(method, "method must not be null");
		Preconditions.condition((target != null || Modifier.isStatic(method.getModifiers())),
			() -> String.format("Cannot invoke non-static method [%s] on a null target.", method.toGenericString()));

		try {
			makeAccessible(method);
			return method.invoke(target, args);
		}
		catch (Throwable ex) {
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
			return Optional.of(classLoader.loadClass(name.trim()));
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

	/**
	 * Try to load a method by its fully qualified name (if such a thing exists for methods).
	 * @param fullyQualifiedMethodName In the form 'package.subpackage.ClassName#methodName'
	 * @return Optional of Method
	 */
	public static Optional<Method> loadMethod(String fullyQualifiedMethodName) {
		Preconditions.notBlank(fullyQualifiedMethodName, "full method name must not be null or empty");
		//TODO Handle overloaded and inherited methods

		Optional<Method> testMethodOptional = Optional.empty();
		int hashPosition = fullyQualifiedMethodName.lastIndexOf('#');
		if (hashPosition >= 0 && hashPosition < fullyQualifiedMethodName.length()) {
			String className = fullyQualifiedMethodName.substring(0, hashPosition);
			String methodName = fullyQualifiedMethodName.substring(hashPosition + 1);
			Optional<Class<?>> methodClassOptional = loadClass(className);
			if (methodClassOptional.isPresent()) {
				try {
					testMethodOptional = Optional.of(methodClassOptional.get().getDeclaredMethod(methodName));
				}
				catch (NoSuchMethodException ignore) {
				}
			}
		}
		return testMethodOptional;
	}

	public static Optional<Object> getOuterInstance(Object inner) {
		// This is risky since it depends on the name of the field which is nowhere guaranteed
		// but has been stable so far in all JDKs

		return Arrays.stream(inner.getClass().getDeclaredFields()).filter(
			f -> f.getName().startsWith("this$")).findFirst().map(f -> {
				makeAccessible(f);
				try {
					return f.get(inner);
				}
				catch (IllegalAccessException e) {
					return Optional.empty();
				}
			});
	}

	public static boolean isPackage(String packageName) {
		return new ClasspathScanner(ReflectionUtils::getDefaultClassLoader, ReflectionUtils::loadClass).isPackage(
			packageName);
	}

	public static Set<File> getAllClasspathRootDirectories() {
		//TODO This is quite a hack, since sometimes the classpath is quite different
		String fullClassPath = System.getProperty("java.class.path");
		final String separator = System.getProperty("path.separator");
		// @formatter:off
		return Arrays.stream(fullClassPath.split(separator))
				.filter(part -> !part.endsWith(".jar"))
				.map(File::new)
				.collect(toSet());
		// @formatter:on
	}

	public static List<Class<?>> findAllClassesInClasspathRoot(File root, Predicate<Class<?>> classTester) {
		return new ClasspathScanner(ReflectionUtils::getDefaultClassLoader,
			ReflectionUtils::loadClass).scanForClassesInClasspathRoot(root, classTester);
	}

	public static List<Class<?>> findAllClassesInPackage(String basePackageName, Predicate<Class<?>> classTester) {
		return new ClasspathScanner(ReflectionUtils::getDefaultClassLoader,
			ReflectionUtils::loadClass).scanForClassesInPackage(basePackageName, classTester);
	}

	public static List<Class<?>> findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");

		return Arrays.stream(clazz.getDeclaredClasses()).filter(predicate).collect(toList());
	}

	public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
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

			List<Method> localMethods = Arrays.stream(ifc.getDeclaredMethods()).filter(Method::isDefault).collect(
				toList());

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
		if (ex instanceof InvocationTargetException) {
			handleException(((InvocationTargetException) ex).getTargetException());
		}
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("No such method or constructor", ex);
		}
		if (ex instanceof NoSuchFieldException) {
			throw new IllegalStateException("No such field", ex);
		}
		if (ex instanceof InstantiationException) {
			throw new IllegalStateException("Instantiation failed", ex);
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Failed to access method or constructor", ex);
		}
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if (ex instanceof Error) {
			throw (Error) ex;
		}
		throw new IllegalStateException("Unhandled exception", ex);
	}

}
