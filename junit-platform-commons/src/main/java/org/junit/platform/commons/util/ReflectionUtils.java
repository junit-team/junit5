/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with the Java reflection APIs.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
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

	// Pattern: [fully qualified class name]#[methodName]((comma-separated list of parameter type names))
	private static final Pattern FULLY_QUALIFIED_METHOD_NAME_PATTERN = Pattern.compile("(.+)#([^()]+?)(\\((.*)\\))?");

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private static final ClasspathScanner classpathScanner = new ClasspathScanner(
		ReflectionUtils::getDefaultClassLoader, ReflectionUtils::loadClass);

	private static final Map<String, Class<?>> primitiveNameToTypeMap;

	private static final Map<Class<?>, Class<?>> primitiveToWrapperMap;

	static {
		Map<String, Class<?>> primitiveTypes = new HashMap<>(16);

		primitiveTypes.put(boolean.class.getName(), boolean.class);
		primitiveTypes.put(byte.class.getName(), byte.class);
		primitiveTypes.put(char.class.getName(), char.class);
		primitiveTypes.put(short.class.getName(), short.class);
		primitiveTypes.put(int.class.getName(), int.class);
		primitiveTypes.put(long.class.getName(), long.class);
		primitiveTypes.put(float.class.getName(), float.class);
		primitiveTypes.put(double.class.getName(), double.class);

		primitiveTypes.put(boolean[].class.getName(), boolean[].class);
		primitiveTypes.put(byte[].class.getName(), byte[].class);
		primitiveTypes.put(char[].class.getName(), char[].class);
		primitiveTypes.put(short[].class.getName(), short[].class);
		primitiveTypes.put(int[].class.getName(), int[].class);
		primitiveTypes.put(long[].class.getName(), long[].class);
		primitiveTypes.put(float[].class.getName(), float[].class);
		primitiveTypes.put(double[].class.getName(), double[].class);

		primitiveNameToTypeMap = Collections.unmodifiableMap(primitiveTypes);

		Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>(8);

		primitiveToWrapper.put(boolean.class, Boolean.class);
		primitiveToWrapper.put(byte.class, Byte.class);
		primitiveToWrapper.put(char.class, Character.class);
		primitiveToWrapper.put(short.class, Short.class);
		primitiveToWrapper.put(int.class, Integer.class);
		primitiveToWrapper.put(long.class, Long.class);
		primitiveToWrapper.put(float.class, Float.class);
		primitiveToWrapper.put(double.class, Double.class);

		primitiveToWrapperMap = Collections.unmodifiableMap(primitiveToWrapper);
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
	 * Determine if the supplied object is an array.
	 *
	 * @param obj the object to test; potentially {@code null}
	 * @return {@code true} if the object is an array
	 */
	public static boolean isArray(Object obj) {
		return (obj != null && obj.getClass().isArray());
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
	 * Get the wrapper type for the supplied primitive type.
	 *
	 * @param type the primitive type for which to retrieve the wrapper type
	 * @return the corresponding wrapper type or {@code null} if the
	 * supplied type is {@code null} or not a primitive type
	 */
	public static Class<?> getWrapperType(Class<?> type) {
		return primitiveToWrapperMap.get(type);
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
	 * @see #newInstance(Constructor, Object...)
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	public static <T> T newInstance(Class<T> clazz, Object... args) {
		Preconditions.notNull(clazz, "class must not be null");
		Preconditions.notNull(args, "argument array must not be null");
		Preconditions.containsNoNullElements(args, "individual arguments must not be null");

		try {
			Class<?>[] parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
			return newInstance(clazz.getDeclaredConstructor(parameterTypes), args);
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	/**
	 * Create a new instance of type {@code T} by invoking the supplied constructor
	 * with the supplied arguments.
	 *
	 * <p>The constructor will be made accessible if necessary, and any checked
	 * exception will be {@linkplain ExceptionUtils#throwAsUncheckedException masked}
	 * as an unchecked exception.
	 *
	 * @param constructor the constructor to invoke; never {@code null}
	 * @param args the arguments to pass to the constructor
	 * @return the new instance; never {@code null}
	 * @see #newInstance(Class, Object...)
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	public static <T> T newInstance(Constructor<T> constructor, Object... args) {
		Preconditions.notNull(constructor, "constructor must not be null");

		try {
			return makeAccessible(constructor).newInstance(args);
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
	 * {@code null} or blank
	 * @see #loadClass(String, ClassLoader)
	 */
	public static Optional<Class<?>> loadClass(String name) {
		return loadClass(name, getDefaultClassLoader());
	}

	/**
	 * Load a class by its <em>fully qualified name</em>, using the supplied
	 * {@link ClassLoader}.
	 * @param name the fully qualified name of the class to load; never
	 * {@code null} or blank
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @see #loadClass(String)
	 */
	public static Optional<Class<?>> loadClass(String name, ClassLoader classLoader) {
		Preconditions.notBlank(name, "class name must not be null or blank");
		Preconditions.notNull(classLoader, "ClassLoader must not be null");
		name = name.trim();

		if (primitiveNameToTypeMap.containsKey(name)) {
			return Optional.of(primitiveNameToTypeMap.get(name));
		}

		try {
			// Object array such as: [Ljava.lang.String;
			if (name.startsWith("[L") && name.endsWith(";")) {
				Class<?> componentType = classLoader.loadClass(name.substring(2, name.length() - 1));
				return Optional.of(Array.newInstance(componentType, 0).getClass());
			}

			return Optional.of(classLoader.loadClass(name));
		}
		catch (ClassNotFoundException ex) {
			return Optional.empty();
		}
	}

	/**
	 * Load a method by its <em>fully qualified name</em>.
	 *
	 * <p>The following formats are supported.
	 *
	 * <ul>
	 * <li>{@code [fully qualified class name]#[methodName]}</li>
	 * <li>{@code [fully qualified class name]#[methodName](parameter type list)}
	 * <ul><li>The <em>parameter type list</em> is a comma-separated list of
	 * fully qualified class names for the types of parameters accepted by
	 * the method.</li></ul>
	 * </li>
	 * </ul>
	 *
	 * <h3>Examples</h3>
	 *
	 * <table border="1">
	 * <tr><th>Method</th><th>Fully Qualified Method Name</th></tr>
	 * <tr><td>{@link String#chars()}</td><td>{@code java.lang.String#chars}</td></tr>
	 * <tr><td>{@link String#chars()}</td><td>{@code java.lang.String#chars()}</td></tr>
	 * <tr><td>{@link String#equalsIgnoreCase(String)}</td><td>{@code java.lang.String#equalsIgnoreCase(java.lang.String)}</td></tr>
	 * <tr><td>{@link String#substring(int, int)}</td><td>{@code java.lang.String#substring(int, int)}</td></tr>
	 * </table>
	 *
	 * @param fullyQualifiedMethodName the fully qualified name of the method to load;
	 * never {@code null} or blank
	 * @return an {@code Optional} containing the method; never {@code null} but
	 * potentially empty
	 */
	public static Optional<Method> loadMethod(String fullyQualifiedMethodName) {
		Preconditions.notBlank(fullyQualifiedMethodName, "fully qualified method name must not be null or blank");

		String fqmn = fullyQualifiedMethodName.trim();
		Matcher matcher = FULLY_QUALIFIED_METHOD_NAME_PATTERN.matcher(fqmn);

		Preconditions.condition(matcher.matches(),
			() -> String.format("Fully qualified method name [%s] does not match pattern [%s]", fqmn,
				FULLY_QUALIFIED_METHOD_NAME_PATTERN));

		String className = matcher.group(1);
		String methodName = matcher.group(2);
		// Note: group #3 includes the parameter types enclosed in parentheses;
		// group #4 contains the actual parameter types.
		String parameterTypeNames = matcher.group(4);

		Optional<Class<?>> classOptional = loadClass(className);
		if (classOptional.isPresent()) {
			try {
				return findMethod(classOptional.get(), methodName.trim(), parameterTypeNames);
			}
			catch (Exception ex) {
				/* ignore */
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
					catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
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

	/**
	 * Determine if the supplied package name refers to a package that is present
	 * in the classpath.
	 *
	 * <p>The default package is represented by an empty string ({@code ""}).
	 *
	 * @param packageName the package name to check; never {@code null} and never
	 * containing whitespace only
	 */
	public static boolean isPackage(String packageName) {
		return classpathScanner.isPackage(packageName);
	}

	public static Set<Path> getAllClasspathRootDirectories() {
		// This is quite a hack, since sometimes the classpath is quite different
		String fullClassPath = System.getProperty("java.class.path");
		// @formatter:off
		return Arrays.stream(fullClassPath.split(File.pathSeparator))
				.map(Paths::get)
				.filter(Files::isDirectory)
				.collect(toSet());
		// @formatter:on
	}

	public static List<Class<?>> findAllClassesInClasspathRoot(Path root, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return classpathScanner.scanForClassesInClasspathRoot(root, classTester, classNameFilter);
	}

	public static List<Class<?>> findAllClassesInPackage(String basePackageName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return classpathScanner.scanForClassesInPackage(basePackageName, classTester, classNameFilter);
	}

	public static List<Class<?>> findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");

		return Arrays.stream(clazz.getDeclaredClasses()).filter(predicate).collect(toList());
	}

	/**
	 * Get the sole declared {@link Constructor} for the supplied class.
	 *
	 * <p>Throws a {@link PreconditionViolationException} if the supplied
	 * class declares more than one constructor.
	 *
	 * @param clazz the class to get the constructor for
	 * @return the sole declared constructor; never {@code null}
	 * @see Class#getDeclaredConstructors()
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		try {
			Constructor<?>[] constructors = clazz.getDeclaredConstructors();
			Preconditions.condition(constructors.length == 1,
				() -> String.format("Class [%s] must declare a single constructor", clazz.getName()));

			return (Constructor<T>) constructors[0];
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
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

	public static Optional<Method> findMethod(Class<?> clazz, String methodName, String parameterTypeNames) {
		return findMethod(clazz, methodName, resolveParameterTypes(parameterTypeNames));
	}

	private static Class<?>[] resolveParameterTypes(String parameterTypeNames) {
		if (StringUtils.isBlank(parameterTypeNames)) {
			return EMPTY_CLASS_ARRAY;
		}

		// @formatter:off
		return Arrays.stream(parameterTypeNames.split(","))
				.map(ReflectionUtils::loadRequiredParameterType)
				.toArray(Class[]::new);
		// @formatter:on
	}

	private static Class<?> loadRequiredParameterType(String typeName) {
		return loadClass(typeName).orElseThrow(
			() -> new JUnitException(String.format("Failed to load parameter type [%s]", typeName)));
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

		// TODO [#333] Determine if we need to support bridged methods.

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
		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || superclass == Object.class) {
			return Collections.emptyList();
		}
		return findAllMethodsInHierarchy(superclass, sortOrder);
	}

	private static boolean isMethodShadowedByLocalMethods(Method method, List<Method> localMethods) {
		return localMethods.stream().anyMatch(local -> isMethodShadowedBy(method, local));
	}

	private static boolean isMethodShadowedBy(Method upper, Method lower) {
		if (!lower.getName().equals(upper.getName())) {
			return false;
		}

		return Arrays.equals(lower.getParameterTypes(), upper.getParameterTypes());
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
