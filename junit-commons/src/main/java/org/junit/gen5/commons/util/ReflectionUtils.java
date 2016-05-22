/*
 * Copyright 2015-2016 the original author or authors.
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
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.gen5.commons.meta.API;

/**
 * Collection of utilities for working with the Java reflection APIs.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.0
 */
@API(Internal)
public final class ReflectionUtils {

	///CLOVER:OFF
	private ReflectionUtils() {
		/* no-op */
	}
	///CLOVER:ON

	public enum MethodSortOrder {
		HierarchyDown, HierarchyUp
	}

	private static final ClasspathScanner classpathScanner = new ClasspathScanner(
		ReflectionUtils::getDefaultClassLoader, ReflectionUtils::loadClass);

	private static final Map<Class<?>, Class<?>> primitiveToWrapperMap;

	static {
		Map<Class<?>, Class<?>> map = new HashMap<>(8);

		map.put(boolean.class, Boolean.class);
		map.put(byte.class, Byte.class);
		map.put(char.class, Character.class);
		map.put(short.class, Short.class);
		map.put(int.class, Integer.class);
		map.put(long.class, Long.class);
		map.put(float.class, Float.class);
		map.put(double.class, Double.class);

		primitiveToWrapperMap = Collections.unmodifiableMap(map);
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

	public static boolean isPublic(Class<?> clazz) {
		return Modifier.isPublic(clazz.getModifiers());
	}

	public static boolean isPublic(Member member) {
		return Modifier.isPublic(member.getModifiers());
	}

	public static boolean isPrivate(Class<?> clazz) {
		return Modifier.isPrivate(clazz.getModifiers());
	}

	public static boolean isPrivate(Member member) {
		return Modifier.isPrivate(member.getModifiers());
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

	/**
	 * Determine if the supplied object can be assigned to the supplied type
	 * for the purpose of reflective method invocations.
	 *
	 * <p>In contrast to {@link Class#isInstance(Object)}, this method returns
	 * {@code true} if the supplied type represents a primitive type whose
	 * wrapper matches the supplied object's type.
	 *
	 * <p>Returns {@code true} if the supplied object is {@code null} and the
	 * supplied type does not represent a primitive type.
	 *
	 * @param obj the object to test for assignment compatibility; potentially {@code null}
	 * @param type the type to check against; never {@code null}
	 * @return {@code true} if the object is assignment compatible
	 * @see Class#isInstance(Object)
	 * @see Class#isAssignableFrom(Class)
	 */
	public static boolean isAssignableTo(Object obj, Class<?> type) {
		Preconditions.notNull(type, "type must not be null");

		if (obj == null) {
			return !type.isPrimitive();
		}

		if (type.isInstance(obj)) {
			return true;
		}

		if (type.isPrimitive()) {
			return primitiveToWrapperMap.get(type) == obj.getClass();
		}

		return false;
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
		Preconditions.notNull(clazz, "class must not be null");
		Preconditions.notNull(args, "none of the arguments may be null");

		try {
			Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
			Constructor<T> constructor = makeAccessible(clazz.getDeclaredConstructor(parameterTypes));
			return constructor.newInstance(args);
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
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
		Preconditions.notNull(method, "method must not be null");
		Preconditions.condition((target != null || isStatic(method)),
			() -> String.format("Cannot invoke non-static method [%s] on a null target.", method.toGenericString()));

		try {
			return makeAccessible(method).invoke(target, args);
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	/**
	 * Load a class by its <em>fully qualified name</em>.
	 * @param name the fully qualified name of the class to load; never
	 * {@code null} or empty
	 * @see #loadClass(String, ClassLoader)
	 */
	public static Optional<Class<?>> loadClass(String name) {
		return loadClass(name, getDefaultClassLoader());
	}

	/**
	 * Load a class by its <em>fully qualified name</em>, using the supplied
	 * {@link ClassLoader}.
	 * @param name the fully qualified name of the class to load; never
	 * {@code null} or empty
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @see #loadClass(String)
	 */
	public static Optional<Class<?>> loadClass(String name, ClassLoader classLoader) {
		Preconditions.notBlank(name, "class name must not be null or empty");
		Preconditions.notNull(classLoader, "ClassLoader must not be null");

		// TODO Add support for primitive types and arrays.

		try {
			return Optional.of(classLoader.loadClass(name.trim()));
		}
		catch (ClassNotFoundException ignore) {
			return Optional.empty();
		}
	}

	/**
	 * Load a method by its <em>fully qualified name</em>.
	 * <p>The supported format is {@code [fully qualified class name]#[methodName]}.
	 * For example, the fully qualified name for the {@code chars()} method in
	 * {@code java.lang.String} is {@code java.lang.String#chars}.
	 * @param fullyQualifiedMethodName the fully qualified name of the method to load;
	 * never {@code null} or empty
	 * @return Optional of Method
	 */
	public static Optional<Method> loadMethod(String fullyQualifiedMethodName) {
		Preconditions.notBlank(fullyQualifiedMethodName, "fully qualified method name must not be null or empty");
		fullyQualifiedMethodName = fullyQualifiedMethodName.trim();

		// TODO Handle overloaded and inherited methods

		int hashPosition = fullyQualifiedMethodName.lastIndexOf('#');
		if (hashPosition >= 0 && hashPosition < fullyQualifiedMethodName.length()) {
			String className = fullyQualifiedMethodName.substring(0, hashPosition);
			String methodName = fullyQualifiedMethodName.substring(hashPosition + 1);
			if (StringUtils.isNotBlank(className) && StringUtils.isNotBlank(methodName)) {
				Optional<Class<?>> classOptional = loadClass(className);
				if (classOptional.isPresent()) {
					try {
						return Optional.of(classOptional.get().getDeclaredMethod(methodName));
					}
					catch (NoSuchMethodException ignore) {
					}
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<Object> getOuterInstance(Object inner) {
		// This is risky since it depends on the name of the field which is nowhere guaranteed
		// but has been stable so far in all JDKs

		// @formatter:off
		return Arrays.stream(inner.getClass().getDeclaredFields())
				.filter(field -> field.getName().startsWith("this$"))
				.findFirst()
				.map(field -> {
					try {
						return makeAccessible(field).get(inner);
					}
					catch (SecurityException | IllegalArgumentException | IllegalAccessException ignore) {
						return Optional.empty();
					}
				});
		// @formatter:on
	}

	public static Optional<Object> getOuterInstance(Object inner, Class<?> targetType) {
		Preconditions.notNull(inner, "inner object must not be null");
		Preconditions.notNull(targetType, "targetType must not be null");

		if (targetType.isInstance(inner)) {
			return Optional.of(inner);
		}

		Optional<Object> candidate = getOuterInstance(inner);
		if (candidate.isPresent()) {
			return getOuterInstance(candidate.get(), targetType);
		}

		return Optional.empty();
	}

	public static boolean isPackage(String packageName) {
		return classpathScanner.isPackage(packageName);
	}

	public static Set<File> getAllClasspathRootDirectories() {
		// TODO This is quite a hack, since sometimes the classpath is quite different
		String fullClassPath = System.getProperty("java.class.path");
		// @formatter:off
		return Arrays.stream(fullClassPath.split(File.pathSeparator))
				.filter(part -> !part.endsWith(".jar"))
				.map(File::new)
				.filter(File::isDirectory)
				.collect(toSet());
		// @formatter:on
	}

	public static List<Class<?>> findAllClassesInClasspathRoot(File root, Predicate<Class<?>> classTester) {
		return classpathScanner.scanForClassesInClasspathRoot(root, classTester);
	}

	public static List<Class<?>> findAllClassesInPackage(String basePackageName, Predicate<Class<?>> classTester) {
		return classpathScanner.scanForClassesInPackage(basePackageName, classTester);
	}

	public static List<Class<?>> findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");

		return Arrays.stream(clazz.getDeclaredClasses()).filter(predicate).collect(toList());
	}

	public static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "method name must not be null or empty");

		try {
			return Optional.ofNullable(clazz.getMethod(methodName, parameterTypes));
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "method name must not be null or empty");

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
	private static List<Method> findAllMethodsInHierarchy(Class<?> clazz, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(sortOrder, "MethodSortOrder must not be null");

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

	/**
	 * Read the value of a potentially inaccessible field.
	 *
	 * <p>If the field does not exist, an exception occurs while reading it, or
	 * the value of the field is {@code null}, an empty {@link Optional} is
	 * returned.
	 *
	 * @param clazz the class where the field is declared; never {@code null}
	 * @param fieldName the name of the field; never {@code null} or empty
	 * @param instance the instance from where the value is to be read; may
	 * be {@code null} for a static field
	 */
	public static <T> Optional<Object> readFieldValue(Class<T> clazz, String fieldName, T instance) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(fieldName, "fieldName must not be null or empty");

		try {
			Field field = makeAccessible(clazz.getDeclaredField(fieldName));
			return Optional.ofNullable(field.get(instance));
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return Optional.empty();
		}
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
		if (clazz != Object.class && clazz.getSuperclass() != Object.class) {
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
		if (!Arrays.equals(lower.getParameterTypes(), upper.getParameterTypes())) {
			return false;
		}
		return true;
	}

	private static <T extends AccessibleObject> T makeAccessible(T object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
		return object;
	}

	/**
	 * Get the underlying cause of the supplied {@link Throwable}.
	 *
	 * <p>If the supplied {@code Throwable} is an instance of
	 * {@link InvocationTargetException}, this method will be invoked
	 * recursively with the underlying
	 * {@linkplain InvocationTargetException#getTargetException() target
	 * exception}; otherwise, this method simply returns the supplied
	 * {@code Throwable}.
	 */
	private static Throwable getUnderlyingCause(Throwable t) {
		if (t instanceof InvocationTargetException) {
			return getUnderlyingCause(((InvocationTargetException) t).getTargetException());
		}
		return t;
	}

	/**
	 * Return all classes and interfaces that can be used as assignment types
	 * for instances of the specified {@link Class}, including itself.
	 *
	 * @param clazz the {@code Class} to lookup
	 * @see Class#isAssignableFrom
	 */
	public static Set<Class<?>> getAllAssignmentCompatibleClasses(Class<?> clazz) {
		Preconditions.notNull(clazz, "class must not be null");

		Set<Class<?>> result = new LinkedHashSet<>();
		getAllAssignmentCompatibleClasses(clazz, result);
		return result;
	}

	private static void getAllAssignmentCompatibleClasses(Class<?> clazz, Set<Class<?>> result) {
		for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
			result.add(current);
			for (Class<?> interfaceClass : current.getInterfaces()) {
				if (!result.contains(interfaceClass)) {
					getAllAssignmentCompatibleClasses(interfaceClass, result);
				}
			}
		}
	}

}
