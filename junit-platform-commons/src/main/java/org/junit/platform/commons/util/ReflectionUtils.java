/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with the Java reflection APIs.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * <p>Some utilities are published via the maintained {@code ReflectionSupport}
 * class.
 *
 * @since 1.0
 * @see org.junit.platform.commons.support.ReflectionSupport
 */
@API(status = INTERNAL, since = "1.0")
public final class ReflectionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

	private ReflectionUtils() {
		/* no-op */
	}

	/**
	 * Modes in which a hierarchy can be traversed &mdash; for example, when
	 * searching for methods or fields within a class hierarchy.
	 */
	public enum HierarchyTraversalMode {

		/**
		 * Traverse the hierarchy using top-down semantics.
		 */
		TOP_DOWN,

		/**
		 * Traverse the hierarchy using bottom-up semantics.
		 */
		BOTTOM_UP
	}

	// Pattern: "[Ljava.lang.String;", "[[[[Ljava.lang.String;", etc.
	private static final Pattern VM_INTERNAL_OBJECT_ARRAY_PATTERN = Pattern.compile("^(\\[+)L(.+);$");

	/**
	 * Pattern: "[x", "[[[[x", etc., where x is Z, B, C, D, F, I, J, S, etc.
	 *
	 * <p>The pattern intentionally captures the last bracket with the
	 * capital letter so that the combination can be looked up via
	 * {@link #classNameToTypeMap}. For example, the last matched group
	 * will contain {@code "[I"} instead of {@code "I"}.
	 *
	 * @see Class#getName()
	 */
	private static final Pattern VM_INTERNAL_PRIMITIVE_ARRAY_PATTERN = Pattern.compile("^(\\[+)(\\[[ZBCDFIJS])$");

	// Pattern: "java.lang.String[]", "int[]", "int[][][][]", etc.
	// ?> => non-capturing atomic group
	// ++ => possessive quantifier
	private static final Pattern SOURCE_CODE_SYNTAX_ARRAY_PATTERN = Pattern.compile("^([^\\[\\]]+)((?>\\[\\])++)$");

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private static final ClasspathScanner classpathScanner = new ClasspathScanner(
		ClassLoaderUtils::getDefaultClassLoader, ReflectionUtils::tryToLoadClass);

	/**
	 * Cache for equivalent methods on an interface implemented by the declaring class.
	 * @since 1.11
	 * @see #getInterfaceMethodIfPossible(Method, Class)
	 */
	private static final Map<Method, Method> interfaceMethodCache = synchronizedMap(new LruCache<>(255));

	/**
	 * Set of fully qualified class names for which no cycles have been detected
	 * in inner class hierarchies.
	 * <p>This serves as a cache to avoid repeated cycle detection for classes
	 * that have already been checked.
	 * @since 1.6
	 * @see #detectInnerClassCycle(Class)
	 */
	private static final Set<String> noCyclesDetectedCache = ConcurrentHashMap.newKeySet();

	/**
	 * Internal cache of common class names mapped to their types.
	 */
	private static final Map<String, Class<?>> classNameToTypeMap;

	/**
	 * Internal cache of primitive types mapped to their wrapper types.
	 */
	private static final Map<Class<?>, Class<?>> primitiveToWrapperMap;

	static {
		// @formatter:off
		List<Class<?>> commonTypes = Arrays.asList(
			boolean.class,
			byte.class,
			char.class,
			short.class,
			int.class,
			long.class,
			float.class,
			double.class,

			boolean[].class,
			byte[].class,
			char[].class,
			short[].class,
			int[].class,
			long[].class,
			float[].class,
			double[].class,

			boolean[][].class,
			byte[][].class,
			char[][].class,
			short[][].class,
			int[][].class,
			long[][].class,
			float[][].class,
			double[][].class,

			Boolean.class,
			Byte.class,
			Character.class,
			Short.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class,
			String.class,

			Boolean[].class,
			Byte[].class,
			Character[].class,
			Short[].class,
			Integer[].class,
			Long[].class,
			Float[].class,
			Double[].class,
			String[].class,

			Boolean[][].class,
			Byte[][].class,
			Character[][].class,
			Short[][].class,
			Integer[][].class,
			Long[][].class,
			Float[][].class,
			Double[][].class,
			String[][].class
		);
		// @formatter:on

		Map<String, Class<?>> classNamesToTypes = new HashMap<>(64);

		commonTypes.forEach(type -> {
			classNamesToTypes.put(type.getName(), type);
			classNamesToTypes.put(type.getCanonicalName(), type);
		});

		classNameToTypeMap = Collections.unmodifiableMap(classNamesToTypes);

		Map<Class<?>, Class<?>> primitivesToWrappers = new IdentityHashMap<>(8);

		primitivesToWrappers.put(boolean.class, Boolean.class);
		primitivesToWrappers.put(byte.class, Byte.class);
		primitivesToWrappers.put(char.class, Character.class);
		primitivesToWrappers.put(short.class, Short.class);
		primitivesToWrappers.put(int.class, Integer.class);
		primitivesToWrappers.put(long.class, Long.class);
		primitivesToWrappers.put(float.class, Float.class);
		primitivesToWrappers.put(double.class, Double.class);

		primitiveToWrapperMap = Collections.unmodifiableMap(primitivesToWrappers);
	}

	public static boolean isPublic(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return Modifier.isPublic(clazz.getModifiers());
	}

	public static boolean isPublic(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return Modifier.isPublic(member.getModifiers());
	}

	public static boolean isPrivate(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return Modifier.isPrivate(clazz.getModifiers());
	}

	public static boolean isPrivate(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return Modifier.isPrivate(member.getModifiers());
	}

	@API(status = INTERNAL, since = "1.4")
	public static boolean isNotPrivate(Class<?> clazz) {
		return !isPrivate(clazz);
	}

	@API(status = INTERNAL, since = "1.1")
	public static boolean isNotPrivate(Member member) {
		return !isPrivate(member);
	}

	public static boolean isAbstract(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return Modifier.isAbstract(clazz.getModifiers());
	}

	public static boolean isAbstract(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return Modifier.isAbstract(member.getModifiers());
	}

	public static boolean isStatic(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return Modifier.isStatic(clazz.getModifiers());
	}

	@API(status = INTERNAL, since = "1.4")
	public static boolean isNotStatic(Class<?> clazz) {
		return !isStatic(clazz);
	}

	public static boolean isStatic(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return Modifier.isStatic(member.getModifiers());
	}

	@API(status = INTERNAL, since = "1.1")
	public static boolean isNotStatic(Member member) {
		return !isStatic(member);
	}

	/**
	 * @since 1.5
	 */
	@API(status = INTERNAL, since = "1.5")
	public static boolean isFinal(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return Modifier.isFinal(clazz.getModifiers());
	}

	/**
	 * @since 1.5
	 */
	@API(status = INTERNAL, since = "1.5")
	public static boolean isNotFinal(Class<?> clazz) {
		return !isFinal(clazz);
	}

	/**
	 * @since 1.5
	 */
	@API(status = INTERNAL, since = "1.5")
	public static boolean isFinal(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return Modifier.isFinal(member.getModifiers());
	}

	/**
	 * @since 1.5
	 */
	@API(status = INTERNAL, since = "1.5")
	public static boolean isNotFinal(Member member) {
		return !isFinal(member);
	}

	/**
	 * Determine if the supplied class is an <em>inner class</em> (i.e., a
	 * non-static member class).
	 *
	 * <p>Technically speaking (i.e., according to the Java Language
	 * Specification), "an inner class may be a non-static member class, a
	 * local class, or an anonymous class." However, this method does not
	 * return {@code true} for a local or anonymous class.
	 *
	 * @param clazz the class to check; never {@code null}
	 * @return {@code true} if the class is an <em>inner class</em>
	 */
	public static boolean isInnerClass(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return !isStatic(clazz) && clazz.isMemberClass();
	}

	/**
	 * Determine if the return type of the supplied method is primitive {@code void}.
	 *
	 * @param method the method to test; never {@code null}
	 * @return {@code true} if the method's return type is {@code void}
	 */
	public static boolean returnsPrimitiveVoid(Method method) {
		return method.getReturnType() == void.class;
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
	 * Determine if the supplied object is a multidimensional array.
	 *
	 * @param obj the object to test; potentially {@code null}
	 * @return {@code true} if the object is a multidimensional array
	 * @since 1.3.2
	 */
	@API(status = INTERNAL, since = "1.3.2")
	public static boolean isMultidimensionalArray(Object obj) {
		return (obj != null && obj.getClass().isArray() && obj.getClass().getComponentType().isArray());
	}

	/**
	 * Determine if an object of the supplied source type can be assigned to the
	 * supplied target type for the purpose of reflective method invocations.
	 *
	 * <p>In contrast to {@link Class#isAssignableFrom(Class)}, this method
	 * returns {@code true} if the target type represents a primitive type whose
	 * wrapper matches the supplied source type. In addition, this method
	 * also supports
	 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">
	 * widening conversions</a> for primitive target types.
	 *
	 * @param sourceType the non-primitive target type; never {@code null}
	 * @param targetType the target type; never {@code null}
	 * @return {@code true} if an object of the source type is assignment compatible
	 * with the target type
	 * @since 1.8
	 * @see Class#isInstance(Object)
	 * @see Class#isAssignableFrom(Class)
	 * @see #isAssignableTo(Object, Class)
	 */
	public static boolean isAssignableTo(Class<?> sourceType, Class<?> targetType) {
		Preconditions.notNull(sourceType, "source type must not be null");
		Preconditions.condition(!sourceType.isPrimitive(), "source type must not be a primitive type");
		Preconditions.notNull(targetType, "target type must not be null");

		if (targetType.isAssignableFrom(sourceType)) {
			return true;
		}

		if (targetType.isPrimitive()) {
			return sourceType == primitiveToWrapperMap.get(targetType) || isWideningConversion(sourceType, targetType);
		}

		return false;
	}

	/**
	 * Determine if the supplied object can be assigned to the supplied target
	 * type for the purpose of reflective method invocations.
	 *
	 * <p>In contrast to {@link Class#isInstance(Object)}, this method returns
	 * {@code true} if the target type represents a primitive type whose
	 * wrapper matches the supplied object's type. In addition, this method
	 * also supports
	 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">
	 * widening conversions</a> for primitive types and their corresponding
	 * wrapper types.
	 *
	 * <p>If the supplied object is {@code null} and the supplied type does not
	 * represent a primitive type, this method returns {@code true}.
	 *
	 * @param obj the object to test for assignment compatibility; potentially {@code null}
	 * @param targetType the type to check against; never {@code null}
	 * @return {@code true} if the object is assignment compatible
	 * @see Class#isInstance(Object)
	 * @see Class#isAssignableFrom(Class)
	 * @see #isAssignableTo(Class, Class)
	 */
	public static boolean isAssignableTo(Object obj, Class<?> targetType) {
		Preconditions.notNull(targetType, "target type must not be null");

		if (obj == null) {
			return !targetType.isPrimitive();
		}

		if (targetType.isInstance(obj)) {
			return true;
		}

		if (targetType.isPrimitive()) {
			Class<?> sourceType = obj.getClass();
			return sourceType == primitiveToWrapperMap.get(targetType) || isWideningConversion(sourceType, targetType);
		}

		return false;
	}

	/**
	 * Determine if Java supports a <em>widening primitive conversion</em> from the
	 * supplied source type to the supplied <strong>primitive</strong> target type.
	 */
	static boolean isWideningConversion(Class<?> sourceType, Class<?> targetType) {
		Preconditions.condition(targetType.isPrimitive(), "targetType must be primitive");

		boolean isPrimitive = sourceType.isPrimitive();
		boolean isWrapper = primitiveToWrapperMap.containsValue(sourceType);

		// Neither a primitive nor a wrapper?
		if (!isPrimitive && !isWrapper) {
			return false;
		}

		if (isPrimitive) {
			sourceType = primitiveToWrapperMap.get(sourceType);
		}

		// @formatter:off
		if (sourceType == Byte.class) {
			return
					targetType == short.class ||
					targetType == int.class ||
					targetType == long.class ||
					targetType == float.class ||
					targetType == double.class;
		}

		if (sourceType == Short.class || sourceType == Character.class) {
			return
					targetType == int.class ||
					targetType == long.class ||
					targetType == float.class ||
					targetType == double.class;
		}

		if (sourceType == Integer.class) {
			return
					targetType == long.class ||
					targetType == float.class ||
					targetType == double.class;
		}

		if (sourceType == Long.class) {
			return
					targetType == float.class ||
					targetType == double.class;
		}

		if (sourceType == Float.class) {
			return
					targetType == double.class;
		}
		// @formatter:on

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
	 * @see org.junit.platform.commons.support.ReflectionSupport#newInstance(Class, Object...)
	 * @see #newInstance(Constructor, Object...)
	 */
	public static <T> T newInstance(Class<T> clazz, Object... args) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(args, "Argument array must not be null");
		Preconditions.containsNoNullElements(args, "Individual arguments must not be null");

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
		Preconditions.notNull(constructor, "Constructor must not be null");

		try {
			return makeAccessible(constructor).newInstance(args);
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	/**
	 * Read the value of a potentially inaccessible or nonexistent field.
	 *
	 * <p>If the field does not exist or the value of the field is {@code null},
	 * an empty {@link Optional} will be returned.
	 *
	 * @param clazz the class where the field is declared; never {@code null}
	 * @param fieldName the name of the field; never {@code null} or empty
	 * @param instance the instance from where the value is to be read; may
	 * be {@code null} for a static field
	 * @see #readFieldValue(Field)
	 * @see #readFieldValue(Field, Object)
	 * @deprecated Please use {@link #tryToReadFieldValue(Class, String, Object)}
	 * instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	public static <T> Optional<Object> readFieldValue(Class<T> clazz, String fieldName, T instance) {
		return tryToReadFieldValue(clazz, fieldName, instance).toOptional();
	}

	/**
	 * Try to read the value of a potentially inaccessible or nonexistent field.
	 *
	 * <p>If the field does not exist or an exception occurs while reading it, a
	 * failed {@link Try} is returned that contains the corresponding exception.
	 *
	 * @param clazz the class where the field is declared; never {@code null}
	 * @param fieldName the name of the field; never {@code null} or empty
	 * @param instance the instance from where the value is to be read; may
	 * be {@code null} for a static field
	 * @since 1.4
	 * @see #tryToReadFieldValue(Field)
	 * @see #tryToReadFieldValue(Field, Object)
	 */
	@API(status = INTERNAL, since = "1.4")
	public static <T> Try<Object> tryToReadFieldValue(Class<T> clazz, String fieldName, T instance) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(fieldName, "Field name must not be null or blank");

		// @formatter:off
		return Try.call(() -> clazz.getDeclaredField(fieldName))
				.andThen(field -> tryToReadFieldValue(field, instance));
		// @formatter:on
	}

	/**
	 * Read the value of the supplied static field, making it accessible if
	 * necessary and {@linkplain ExceptionUtils#throwAsUncheckedException masking}
	 * any checked exception as an unchecked exception.
	 *
	 * <p>If the value of the field is {@code null}, an empty {@link Optional}
	 * will be returned.
	 *
	 * @param field the field to read; never {@code null}
	 * @see #readFieldValue(Field, Object)
	 * @see #readFieldValue(Class, String, Object)
	 * @deprecated Please use {@link #tryToReadFieldValue(Field)} instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	public static Optional<Object> readFieldValue(Field field) {
		return tryToReadFieldValue(field).toOptional();
	}

	/**
	 * Try to read the value of a potentially inaccessible static field.
	 *
	 * <p>If an exception occurs while reading the field, a failed {@link Try}
	 * is returned that contains the corresponding exception.
	 *
	 * @param field the field to read; never {@code null}
	 * @since 1.4
	 * @see #tryToReadFieldValue(Field, Object)
	 * @see #tryToReadFieldValue(Class, String, Object)
	 */
	@API(status = INTERNAL, since = "1.4")
	public static Try<Object> tryToReadFieldValue(Field field) {
		return tryToReadFieldValue(field, null);
	}

	/**
	 * Read the value of the supplied field, making it accessible if necessary
	 * and {@linkplain ExceptionUtils#throwAsUncheckedException masking} any
	 * checked exception as an unchecked exception.
	 *
	 * <p>If the value of the field is {@code null}, an empty {@link Optional}
	 * will be returned.
	 *
	 * @param field the field to read; never {@code null}
	 * @param instance the instance from which the value is to be read; may
	 * be {@code null} for a static field
	 * @see #readFieldValue(Field)
	 * @see #readFieldValue(Class, String, Object)
	 * @deprecated Please use {@link #tryToReadFieldValue(Field, Object)}
	 * instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	public static Optional<Object> readFieldValue(Field field, Object instance) {
		return tryToReadFieldValue(field, instance).toOptional();
	}

	/**
	 * @since 1.4
	 * @see org.junit.platform.commons.support.ReflectionSupport#tryToReadFieldValue(Field, Object)
	 * @see #tryToReadFieldValue(Class, String, Object)
	 */
	@API(status = INTERNAL, since = "1.4")
	public static Try<Object> tryToReadFieldValue(Field field, Object instance) {
		Preconditions.notNull(field, "Field must not be null");
		Preconditions.condition((instance != null || isStatic(field)),
			() -> String.format("Cannot read non-static field [%s] on a null instance.", field));

		return Try.call(() -> makeAccessible(field).get(instance));
	}

	/**
	 * Read the values of the supplied fields, making each field accessible if
	 * necessary and {@linkplain ExceptionUtils#throwAsUncheckedException masking}
	 * any checked exception as an unchecked exception.
	 *
	 * @param fields the list of fields to read; never {@code null}
	 * @param instance the instance from which the values are to be read; may
	 * be {@code null} for static fields
	 * @return an immutable list of the values of the specified fields; never
	 * {@code null} but may be empty or contain {@code null} entries
	 */
	public static List<Object> readFieldValues(List<Field> fields, Object instance) {
		return readFieldValues(fields, instance, field -> true);
	}

	/**
	 * Read the values of the supplied fields, making each field accessible if
	 * necessary, {@linkplain ExceptionUtils#throwAsUncheckedException masking}
	 * any checked exception as an unchecked exception, and filtering out fields
	 * that do not pass the supplied {@code predicate}.
	 *
	 * @param fields the list of fields to read; never {@code null}
	 * @param instance the instance from which the values are to be read; may
	 * be {@code null} for static fields
	 * @param predicate the field filter; never {@code null}
	 * @return an immutable list of the values of the specified fields; never
	 * {@code null} but may be empty or contain {@code null} entries
	 */
	public static List<Object> readFieldValues(List<Field> fields, Object instance, Predicate<Field> predicate) {
		Preconditions.notNull(fields, "fields list must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		// @formatter:off
		return fields.stream()
				.filter(predicate)
				.map(field ->
					tryToReadFieldValue(field, instance)
						.getOrThrow(ExceptionUtils::throwAsUncheckedException))
				.collect(toUnmodifiableList());
		// @formatter:on
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#invokeMethod(Method, Object, Object...)
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		Preconditions.notNull(method, "Method must not be null");
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
	 * @see org.junit.platform.commons.support.ReflectionSupport#loadClass(String)
	 * @deprecated Please use {@link #tryToLoadClass(String)} instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	public static Optional<Class<?>> loadClass(String name) {
		return tryToLoadClass(name).toOptional();
	}

	/**
	 * @since 1.4
	 * @see org.junit.platform.commons.support.ReflectionSupport#tryToLoadClass(String)
	 */
	@API(status = INTERNAL, since = "1.4")
	public static Try<Class<?>> tryToLoadClass(String name) {
		return tryToLoadClass(name, ClassLoaderUtils.getDefaultClassLoader());
	}

	/**
	 * Load a class by its <em>primitive name</em> or <em>fully qualified name</em>,
	 * using the supplied {@link ClassLoader}.
	 *
	 * <p>See {@link org.junit.platform.commons.support.ReflectionSupport#loadClass(String)}
	 * for details on support for class names for arrays.
	 *
	 * @param name the name of the class to load; never {@code null} or blank
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @see #loadClass(String)
	 * @deprecated Please use {@link #tryToLoadClass(String, ClassLoader)}
	 * instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	public static Optional<Class<?>> loadClass(String name, ClassLoader classLoader) {
		return tryToLoadClass(name, classLoader).toOptional();
	}

	/**
	 * Try to load a class by its <em>primitive name</em> or <em>fully qualified
	 * name</em>, using the supplied {@link ClassLoader}.
	 *
	 * <p>See {@link org.junit.platform.commons.support.ReflectionSupport#tryToLoadClass(String)}
	 * for details on support for class names for arrays.
	 *
	 * @param name the name of the class to load; never {@code null} or blank
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @since 1.4
	 * @see #tryToLoadClass(String)
	 */
	@API(status = INTERNAL, since = "1.4")
	public static Try<Class<?>> tryToLoadClass(String name, ClassLoader classLoader) {
		Preconditions.notBlank(name, "Class name must not be null or blank");
		Preconditions.notNull(classLoader, "ClassLoader must not be null");
		String trimmedName = name.trim();

		if (classNameToTypeMap.containsKey(trimmedName)) {
			return Try.success(classNameToTypeMap.get(trimmedName));
		}

		return Try.call(() -> {
			Matcher matcher;

			// Primitive arrays such as "[I", "[[[[D", etc.
			matcher = VM_INTERNAL_PRIMITIVE_ARRAY_PATTERN.matcher(trimmedName);
			if (matcher.matches()) {
				String brackets = matcher.group(1);
				String componentTypeName = matcher.group(2);
				// Calculate dimensions by counting brackets.
				int dimensions = brackets.length();

				return loadArrayType(classLoader, componentTypeName, dimensions);
			}

			// Object arrays such as "[Ljava.lang.String;", "[[[[Ljava.lang.String;", etc.
			matcher = VM_INTERNAL_OBJECT_ARRAY_PATTERN.matcher(trimmedName);
			if (matcher.matches()) {
				String brackets = matcher.group(1);
				String componentTypeName = matcher.group(2);
				// Calculate dimensions by counting brackets.
				int dimensions = brackets.length();

				return loadArrayType(classLoader, componentTypeName, dimensions);
			}

			// Arrays such as "java.lang.String[]", "int[]", "int[][][][]", etc.
			matcher = SOURCE_CODE_SYNTAX_ARRAY_PATTERN.matcher(trimmedName);
			if (matcher.matches()) {
				String componentTypeName = matcher.group(1);
				String bracketPairs = matcher.group(2);
				// Calculate dimensions by counting bracket pairs.
				int dimensions = bracketPairs.length() / 2;

				return loadArrayType(classLoader, componentTypeName, dimensions);
			}

			// Fallback to standard VM class loading
			return Class.forName(trimmedName, false, classLoader);
		});
	}

	private static Class<?> loadArrayType(ClassLoader classLoader, String componentTypeName, int dimensions)
			throws ClassNotFoundException {

		Class<?> componentType = classNameToTypeMap.containsKey(componentTypeName)
				? classNameToTypeMap.get(componentTypeName)
				: Class.forName(componentTypeName, false, classLoader);

		return Array.newInstance(componentType, new int[dimensions]).getClass();
	}

	/**
	 * Build the <em>fully qualified method name</em> for the method described by the
	 * supplied class and method.
	 *
	 * <p>Note that the class is not necessarily the class in which the method is
	 * declared.
	 *
	 * @param clazz the class from which the method should be referenced; never {@code null}
	 * @param method the method; never {@code null}
	 * @return fully qualified method name; never {@code null}
	 * @since 1.4
	 * @see #getFullyQualifiedMethodName(Class, String, Class...)
	 */
	public static String getFullyQualifiedMethodName(Class<?> clazz, Method method) {
		Preconditions.notNull(method, "Method must not be null");

		return getFullyQualifiedMethodName(clazz, method.getName(), method.getParameterTypes());
	}

	/**
	 * Build the <em>fully qualified method name</em> for the method described by the
	 * supplied class, method name, and parameter types.
	 *
	 * <p>Note that the class is not necessarily the class in which the method is
	 * declared.
	 *
	 * @param clazz the class from which the method should be referenced; never {@code null}
	 * @param methodName the name of the method; never {@code null} or blank
	 * @param parameterTypes the parameter types of the method; may be {@code null} or empty
	 * @return fully qualified method name; never {@code null}
	 * @see #getFullyQualifiedMethodName(Class, Method)
	 */
	public static String getFullyQualifiedMethodName(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");

		return String.format("%s#%s(%s)", clazz.getName(), methodName, ClassUtils.nullSafeToString(parameterTypes));
	}

	/**
	 * Parse the supplied <em>fully qualified method name</em> into a 3-element
	 * {@code String[]} with the following content.
	 *
	 * <ul>
	 *   <li>index {@code 0}: the fully qualified class name</li>
	 *   <li>index {@code 1}: the name of the method</li>
	 *   <li>index {@code 2}: a comma-separated list of parameter types, or a
	 *       blank string if the method does not declare any formal parameters</li>
	 * </ul>
	 *
	 * @param fullyQualifiedMethodName a <em>fully qualified method name</em>,
	 * never {@code null} or blank
	 * @return a 3-element array of strings containing the parsed values
	 */
	public static String[] parseFullyQualifiedMethodName(String fullyQualifiedMethodName) {
		Preconditions.notBlank(fullyQualifiedMethodName, "fullyQualifiedMethodName must not be null or blank");

		int indexOfFirstHashtag = fullyQualifiedMethodName.indexOf('#');
		boolean validSyntax = (indexOfFirstHashtag > 0)
				&& (indexOfFirstHashtag < fullyQualifiedMethodName.length() - 1);

		Preconditions.condition(validSyntax,
			() -> "[" + fullyQualifiedMethodName + "] is not a valid fully qualified method name: "
					+ "it must start with a fully qualified class name followed by a '#' "
					+ "and then the method name, optionally followed by a parameter list enclosed in parentheses.");

		String className = fullyQualifiedMethodName.substring(0, indexOfFirstHashtag);
		String methodPart = fullyQualifiedMethodName.substring(indexOfFirstHashtag + 1);
		String methodName = methodPart;
		String methodParameters = "";

		if (methodPart.endsWith("()")) {
			methodName = methodPart.substring(0, methodPart.length() - 2);
		}
		else if (methodPart.endsWith(")")) {
			int indexOfLastOpeningParenthesis = methodPart.lastIndexOf('(');
			if ((indexOfLastOpeningParenthesis > 0) && (indexOfLastOpeningParenthesis < methodPart.length() - 1)) {
				methodName = methodPart.substring(0, indexOfLastOpeningParenthesis);
				methodParameters = methodPart.substring(indexOfLastOpeningParenthesis + 1, methodPart.length() - 1);
			}
		}
		return new String[] { className, methodName, methodParameters };
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

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findAllClassesInClasspathRoot(URI, Predicate, Predicate)
	 */
	public static List<Class<?>> findAllClassesInClasspathRoot(URI root, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		// unmodifiable since returned by public, non-internal method(s)
		return findAllClassesInClasspathRoot(root, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * @since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamAllClassesInClasspathRoot(URI, Predicate, Predicate)
	 */
	public static Stream<Class<?>> streamAllClassesInClasspathRoot(URI root, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		return streamAllClassesInClasspathRoot(root, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * @since 1.1
	 */
	public static List<Class<?>> findAllClassesInClasspathRoot(URI root, ClassFilter classFilter) {
		return Collections.unmodifiableList(classpathScanner.scanForClassesInClasspathRoot(root, classFilter));
	}

	/**
	 * @since 1.10
	 */
	public static Stream<Class<?>> streamAllClassesInClasspathRoot(URI root, ClassFilter classFilter) {
		return findAllClassesInClasspathRoot(root, classFilter).stream();
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findAllClassesInPackage(String, Predicate, Predicate)
	 */
	public static List<Class<?>> findAllClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		// unmodifiable since returned by public, non-internal method(s)
		return findAllClassesInPackage(basePackageName, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamAllClassesInPackage(String, Predicate, Predicate)
	 */
	public static Stream<Class<?>> streamAllClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		return streamAllClassesInPackage(basePackageName, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * @since 1.1
	 */
	public static List<Class<?>> findAllClassesInPackage(String basePackageName, ClassFilter classFilter) {
		return Collections.unmodifiableList(classpathScanner.scanForClassesInPackage(basePackageName, classFilter));
	}

	/**
	 * @since 1.10
	 */
	public static Stream<Class<?>> streamAllClassesInPackage(String basePackageName, ClassFilter classFilter) {
		return findAllClassesInPackage(basePackageName, classFilter).stream();
	}

	/**
	 * @since 1.1.1
	 * @see org.junit.platform.commons.support.ReflectionSupport#findAllClassesInModule(String, Predicate, Predicate)
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		// unmodifiable since returned by public, non-internal method(s)
		return findAllClassesInModule(moduleName, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * @since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamAllClassesInModule(String, Predicate, Predicate)
	 */
	public static Stream<Class<?>> streamAllClassesInModule(String moduleName, Predicate<Class<?>> classFilter,
			Predicate<String> classNameFilter) {
		return streamAllClassesInModule(moduleName, ClassFilter.of(classNameFilter, classFilter));
	}

	/**
	 * @since 1.1.1
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter classFilter) {
		return Collections.unmodifiableList(ModuleUtils.findAllClassesInModule(moduleName, classFilter));
	}

	/**
	 * @since 1.10
	 */
	public static Stream<Class<?>> streamAllClassesInModule(String moduleName, ClassFilter classFilter) {
		return findAllClassesInModule(moduleName, classFilter).stream();
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findNestedClasses(Class, Predicate)
	 */
	public static List<Class<?>> findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		Set<Class<?>> candidates = new LinkedHashSet<>();
		findNestedClasses(clazz, predicate, candidates);
		return Collections.unmodifiableList(new ArrayList<>(candidates));
	}

	/**
	 * since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamNestedClasses(Class, Predicate)
	 */
	public static Stream<Class<?>> streamNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate) {
		return findNestedClasses(clazz, predicate).stream();
	}

	private static void findNestedClasses(Class<?> clazz, Predicate<Class<?>> predicate, Set<Class<?>> candidates) {
		if (!isSearchable(clazz)) {
			return;
		}

		if (isInnerClass(clazz) && predicate.test(clazz)) {
			detectInnerClassCycle(clazz);
		}

		try {
			// Candidates in current class
			for (Class<?> nestedClass : clazz.getDeclaredClasses()) {
				if (predicate.test(nestedClass)) {
					detectInnerClassCycle(nestedClass);
					candidates.add(nestedClass);
				}
			}
		}
		catch (NoClassDefFoundError error) {
			logger.debug(error, () -> "Failed to retrieve declared classes for " + clazz.getName());
		}

		// Search class hierarchy
		findNestedClasses(clazz.getSuperclass(), predicate, candidates);

		// Search interface hierarchy
		for (Class<?> ifc : clazz.getInterfaces()) {
			findNestedClasses(ifc, predicate, candidates);
		}
	}

	/**
	 * Detect a cycle in the inner class hierarchy in which the supplied class
	 * resides &mdash; from the supplied class up to the outermost enclosing class
	 * &mdash; and throw a {@link JUnitException} if a cycle is detected.
	 *
	 * <p>This method does <strong>not</strong> detect cycles within inner class
	 * hierarchies <em>below</em> the supplied class.
	 *
	 * <p>If the supplied class is not an inner class and does not have a
	 * searchable superclass, this method is effectively a no-op.
	 *
	 * @since 1.6
	 * @see #isInnerClass(Class)
	 * @see #isSearchable(Class)
	 */
	private static void detectInnerClassCycle(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		String className = clazz.getName();

		if (noCyclesDetectedCache.contains(className)) {
			return;
		}

		Class<?> superclass = clazz.getSuperclass();
		if (isInnerClass(clazz) && isSearchable(superclass)) {
			for (Class<?> enclosing = clazz.getEnclosingClass(); enclosing != null; enclosing = enclosing.getEnclosingClass()) {
				if (superclass.equals(enclosing)) {
					throw new JUnitException(String.format("Detected cycle in inner class hierarchy between %s and %s",
						className, enclosing.getName()));
				}
			}
		}

		noCyclesDetectedCache.add(className);
	}

	/**
	 * Get the sole declared, non-synthetic {@link Constructor} for the supplied class.
	 *
	 * <p>Throws a {@link org.junit.platform.commons.PreconditionViolationException}
	 * if the supplied class declares more than one non-synthetic constructor.
	 *
	 * @param clazz the class to get the constructor for
	 * @return the sole declared constructor; never {@code null}
	 * @see Class#getDeclaredConstructors()
	 * @see Class#isSynthetic()
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getDeclaredConstructor(Class<T> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		try {
			List<Constructor<?>> constructors = Arrays.stream(clazz.getDeclaredConstructors())//
					.filter(ctor -> !ctor.isSynthetic())//
					.collect(toList());

			Preconditions.condition(constructors.size() == 1,
				() -> String.format("Class [%s] must declare a single constructor", clazz.getName()));

			return (Constructor<T>) constructors.get(0);
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	/**
	 * Find all constructors in the supplied class that match the supplied predicate.
	 *
	 * <p>Note that this method may return {@linkplain Class#isSynthetic() synthetic}
	 * constructors. If you wish to ignore synthetic constructors, you may filter
	 * them out with the supplied {@code predicate} or filter them out of the list
	 * returned by this method.
	 *
	 * @param clazz the class in which to search for constructors; never {@code null}
	 * @param predicate the predicate to use to test for a match; never {@code null}
	 * @return an immutable list of all such constructors found; never {@code null}
	 * but potentially empty
	 * @see Class#getDeclaredConstructors()
	 * @see Class#isSynthetic()
	 */
	public static List<Constructor<?>> findConstructors(Class<?> clazz, Predicate<Constructor<?>> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		try {
			// @formatter:off
			return Arrays.stream(clazz.getDeclaredConstructors())
					.filter(predicate)
					.collect(toUnmodifiableList());
			// @formatter:on
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findFields(Class, Predicate, org.junit.platform.commons.support.HierarchyTraversalMode)
	 */
	public static List<Field> findFields(Class<?> clazz, Predicate<Field> predicate,
			HierarchyTraversalMode traversalMode) {

		return streamFields(clazz, predicate, traversalMode).collect(toUnmodifiableList());
	}

	/**
	 * @since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamFields(Class, Predicate, org.junit.platform.commons.support.HierarchyTraversalMode)
	 */
	public static Stream<Field> streamFields(Class<?> clazz, Predicate<Field> predicate,
			HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");
		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		// @formatter:off
		return findAllFieldsInHierarchy(clazz, traversalMode).stream()
				.filter(predicate)
				.distinct();
		// @formatter:on
	}

	private static List<Field> findAllFieldsInHierarchy(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		// @formatter:off
		List<Field> localFields = getDeclaredFields(clazz).stream()
				.filter(field -> !field.isSynthetic())
				.collect(toList());
		List<Field> superclassFields = getSuperclassFields(clazz, traversalMode).stream()
				.filter(field -> !isFieldShadowedByLocalFields(field, localFields))
				.collect(toList());
		List<Field> interfaceFields = getInterfaceFields(clazz, traversalMode).stream()
				.filter(field -> !isFieldShadowedByLocalFields(field, localFields))
				.collect(toList());
		// @formatter:on

		List<Field> fields = new ArrayList<>();
		if (traversalMode == TOP_DOWN) {
			fields.addAll(superclassFields);
			fields.addAll(interfaceFields);
		}
		fields.addAll(localFields);
		if (traversalMode == BOTTOM_UP) {
			fields.addAll(interfaceFields);
			fields.addAll(superclassFields);
		}
		return fields;
	}

	/**
	 * Determine if a {@link Method} matching the supplied {@link Predicate}
	 * is present within the type hierarchy of the specified class, beginning
	 * with the specified class or interface and traversing up the type
	 * hierarchy until such a method is found or the type hierarchy is exhausted.
	 *
	 * @param clazz the class or interface in which to find the method; never
	 * {@code null}
	 * @param predicate the predicate to use to test for a match; never
	 * {@code null}
	 * @return {@code true} if such a method is present
	 * @see #findMethod(Class, String, String)
	 * @see #findMethod(Class, String, Class...)
	 */
	public static boolean isMethodPresent(Class<?> clazz, Predicate<Method> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		return findMethod(clazz, predicate).isPresent();
	}

	/**
	 * Get the {@link Method} in the specified class with the specified name
	 * and parameter types.
	 *
	 * <p>This method delegates to {@link Class#getMethod(String, Class...)} but
	 * swallows any exception thrown.
	 *
	 * @param clazz the class in which to search for the method; never {@code null}
	 * @param methodName the name of the method to get; never {@code null} or blank
	 * @param parameterTypes the parameter types of the method; may be {@code null}
	 * or empty
	 * @return an {@code Optional} containing the method; never {@code null} but
	 * empty if the invocation of {@code Class#getMethod()} throws a
	 * {@link NoSuchMethodException}
	 * @deprecated Please use {@link #tryToGetMethod(Class, String, Class[])}
	 * instead.
	 */
	@API(status = DEPRECATED, since = "1.4")
	@Deprecated
	static Optional<Method> getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return tryToGetMethod(clazz, methodName, parameterTypes).toOptional();
	}

	/**
	 * Try to get the {@link Method} in the specified class with the specified
	 * name and parameter types.
	 *
	 * <p>This method delegates to {@link Class#getMethod(String, Class...)} but
	 * catches any exception thrown.
	 *
	 * @param clazz the class in which to search for the method; never {@code null}
	 * @param methodName the name of the method to get; never {@code null} or blank
	 * @param parameterTypes the parameter types of the method; may be {@code null}
	 * or empty
	 * @return a successful {@link Try} containing the method or a failed
	 * {@link Try} containing the {@link NoSuchMethodException} thrown by
	 * {@code Class#getMethod()}; never {@code null}
	 * @since 1.4
	 */
	@API(status = INTERNAL, since = "1.4")
	public static Try<Method> tryToGetMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");

		return Try.call(() -> clazz.getMethod(methodName, parameterTypes));
	}

	/**
	 * Determine a corresponding interface method for the given method handle, if possible.
	 * <p>This is particularly useful for arriving at a public exported type on the Java
	 * Module System which can be reflectively invoked without an illegal access warning.
	 * @param method the method to be invoked, potentially from an implementation class
	 * @param targetClass the target class to check for declared interfaces
	 * @return the corresponding interface method, or the original method if none found
	 * @since 1.11
	 */
	@API(status = INTERNAL, since = "1.11")
	public static Method getInterfaceMethodIfPossible(Method method, Class<?> targetClass) {
		if (!isPublic(method) || method.getDeclaringClass().isInterface()) {
			return method;
		}
		// Try cached version of method in its declaring class
		Method result = interfaceMethodCache.computeIfAbsent(method,
			m -> findInterfaceMethodIfPossible(m, m.getDeclaringClass(), Object.class));
		if (result == method && targetClass != null) {
			// No interface method found yet -> try given target class (possibly a subclass of the
			// declaring class, late-binding a base class method to a subclass-declared interface:
			// see e.g. HashMap.HashIterator.hasNext)
			result = findInterfaceMethodIfPossible(method, targetClass, method.getDeclaringClass());
		}
		return result;
	}

	private static Method findInterfaceMethodIfPossible(Method method, Class<?> startClass, Class<?> endClass) {
		Class<?>[] parameterTypes = null;
		Class<?> current = startClass;
		while (current != null && current != endClass) {
			if (parameterTypes == null) {
				// Since Method#getParameterTypes() clones the array, we lazily retrieve
				// and cache parameter types to avoid cloning the array multiple times.
				parameterTypes = method.getParameterTypes();
			}
			for (Class<?> ifc : current.getInterfaces()) {
				try {
					return ifc.getMethod(method.getName(), parameterTypes);
				}
				catch (NoSuchMethodException ex) {
					// ignore
				}
			}
			current = current.getSuperclass();
		}
		return method;
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findMethod(Class, String, String)
	 */
	public static Optional<Method> findMethod(Class<?> clazz, String methodName, String parameterTypeNames) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		return findMethod(clazz, methodName, resolveParameterTypes(clazz, methodName, parameterTypeNames));
	}

	@API(status = INTERNAL, since = "1.10")
	public static Class<?>[] resolveParameterTypes(Class<?> clazz, String methodName, String parameterTypeNames) {
		if (StringUtils.isBlank(parameterTypeNames)) {
			return EMPTY_CLASS_ARRAY;
		}

		// @formatter:off
		return Arrays.stream(parameterTypeNames.split(","))
				.map(String::trim)
				.map(typeName -> loadRequiredParameterType(clazz, methodName, typeName))
				.toArray(Class[]::new);
		// @formatter:on
	}

	private static Class<?> loadRequiredParameterType(Class<?> clazz, String methodName, String typeName) {
		ClassLoader classLoader = ClassLoaderUtils.getClassLoader(clazz);

		// @formatter:off
		return tryToLoadClass(typeName, classLoader)
				.getOrThrow(cause -> new JUnitException(
						String.format("Failed to load parameter type [%s] for method [%s] in class [%s].",
								typeName, methodName, clazz.getName()), cause));
		// @formatter:on
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findMethod(Class, String, Class...)
	 */
	public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		Preconditions.notNull(parameterTypes, "Parameter types array must not be null");
		Preconditions.containsNoNullElements(parameterTypes, "Individual parameter types must not be null");

		return findMethod(clazz, method -> hasCompatibleSignature(method, methodName, parameterTypes));
	}

	private static Optional<Method> findMethod(Class<?> clazz, Predicate<Method> predicate) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		for (Class<?> current = clazz; isSearchable(current); current = current.getSuperclass()) {
			// Search for match in current type
			List<Method> methods = current.isInterface() ? getMethods(current) : getDeclaredMethods(current, BOTTOM_UP);
			for (Method method : methods) {
				if (predicate.test(method)) {
					return Optional.of(method);
				}
			}

			// Search for match in interfaces implemented by current type
			for (Class<?> ifc : current.getInterfaces()) {
				Optional<Method> optional = findMethod(ifc, predicate);
				if (optional.isPresent()) {
					return optional;
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Find the first {@link Method} of the supplied class or interface that
	 * meets the specified criteria, beginning with the specified class or
	 * interface and traversing up the type hierarchy until such a method is
	 * found or the type hierarchy is exhausted.
	 *
	 * <p>Use this method as an alternative to
	 * {@link #findMethod(Class, String, Class...)} for use cases in which the
	 * method is required to be present.
	 *
	 * @param clazz the class or interface in which to find the method;
	 * never {@code null}
	 * @param methodName the name of the method to find; never {@code null}
	 * or empty
	 * @param parameterTypes the types of parameters accepted by the method,
	 * if any; never {@code null}
	 * @return the {@code Method} found; never {@code null}
	 * @throws JUnitException if no method is found
	 *
	 * @since 1.7
	 * @see #findMethod(Class, String, Class...)
	 */
	@API(status = STABLE, since = "1.7")
	public static Method getRequiredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes).orElseThrow(
			() -> new JUnitException(format("Could not find method [%s] in class [%s]", methodName, clazz.getName())));
	}

	/**
	 * Find all {@linkplain Method methods} of the supplied class or interface
	 * that match the specified {@code predicate}, using top-down search semantics
	 * within the type hierarchy.
	 *
	 * <p>The results will not contain instance methods that are <em>overridden</em>
	 * or {@code static} methods that are <em>hidden</em>.
	 *
	 * @param clazz the class or interface in which to find the methods; never {@code null}
	 * @param predicate the method filter; never {@code null}
	 * @return an immutable list of all such methods found; never {@code null}
	 * @see HierarchyTraversalMode#TOP_DOWN
	 * @see #findMethods(Class, Predicate, HierarchyTraversalMode)
	 */
	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate) {
		return findMethods(clazz, predicate, TOP_DOWN);
	}

	/**
	 * @see org.junit.platform.commons.support.ReflectionSupport#findMethods(Class, Predicate, org.junit.platform.commons.support.HierarchyTraversalMode)
	 */
	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate,
			HierarchyTraversalMode traversalMode) {

		return streamMethods(clazz, predicate, traversalMode).collect(toUnmodifiableList());
	}

	/**
	 * @since 1.10
	 * @see org.junit.platform.commons.support.ReflectionSupport#streamMethods(Class, Predicate, org.junit.platform.commons.support.HierarchyTraversalMode)
	 */
	public static Stream<Method> streamMethods(Class<?> clazz, Predicate<Method> predicate,
			HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");
		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		// @formatter:off
		return findAllMethodsInHierarchy(clazz, traversalMode).stream()
				.filter(predicate)
				.distinct();
		// @formatter:on
	}

	/**
	 * Find all non-synthetic methods in the superclass and interface hierarchy,
	 * excluding Object.
	 */
	private static List<Method> findAllMethodsInHierarchy(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		// @formatter:off
		List<Method> localMethods = getDeclaredMethods(clazz, traversalMode).stream()
				.filter(method -> !method.isSynthetic())
				.collect(toList());
		List<Method> superclassMethods = getSuperclassMethods(clazz, traversalMode).stream()
				.filter(method -> !isMethodShadowedByLocalMethods(method, localMethods))
				.collect(toList());
		List<Method> interfaceMethods = getInterfaceMethods(clazz, traversalMode).stream()
				.filter(method -> !isMethodShadowedByLocalMethods(method, localMethods))
				.collect(toList());
		// @formatter:on

		List<Method> methods = new ArrayList<>();
		if (traversalMode == TOP_DOWN) {
			methods.addAll(superclassMethods);
			methods.addAll(interfaceMethods);
		}
		methods.addAll(localMethods);
		if (traversalMode == BOTTOM_UP) {
			methods.addAll(interfaceMethods);
			methods.addAll(superclassMethods);
		}
		return methods;
	}

	/**
	 * Custom alternative to {@link Class#getFields()} that sorts the fields
	 * and converts them to a mutable list.
	 */
	private static List<Field> getFields(Class<?> clazz) {
		return toSortedMutableList(clazz.getFields());
	}

	/**
	 * Custom alternative to {@link Class#getDeclaredFields()} that sorts the
	 * fields and converts them to a mutable list.
	 */
	private static List<Field> getDeclaredFields(Class<?> clazz) {
		return toSortedMutableList(clazz.getDeclaredFields());
	}

	/**
	 * Custom alternative to {@link Class#getMethods()} that sorts the methods
	 * and converts them to a mutable list.
	 */
	private static List<Method> getMethods(Class<?> clazz) {
		return toSortedMutableList(clazz.getMethods());
	}

	/**
	 * Custom alternative to {@link Class#getDeclaredMethods()} that sorts the
	 * methods and converts them to a mutable list.
	 *
	 * <p>In addition, the list returned by this method includes interface
	 * default methods which are either prepended or appended to the list of
	 * declared methods depending on the supplied traversal mode.
	 */
	private static List<Method> getDeclaredMethods(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		// Note: getDefaultMethods() already sorts the methods,
		List<Method> defaultMethods = getDefaultMethods(clazz);
		List<Method> declaredMethods = toSortedMutableList(clazz.getDeclaredMethods());

		// Take the traversal mode into account in order to retain the inherited
		// nature of interface default methods.
		if (traversalMode == BOTTOM_UP) {
			declaredMethods.addAll(defaultMethods);
			return declaredMethods;
		}
		else {
			defaultMethods.addAll(declaredMethods);
			return defaultMethods;
		}
	}

	/**
	 * Get a sorted, mutable list of all default methods present in interfaces
	 * implemented by the supplied class which are also <em>visible</em> within
	 * the supplied class.
	 *
	 * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#d5e9652">Method Visibility</a>
	 * in the Java Language Specification
	 */
	private static List<Method> getDefaultMethods(Class<?> clazz) {
		// @formatter:off
		// Visible default methods are interface default methods that have not
		// been overridden.
		List<Method> visibleDefaultMethods = Arrays.stream(clazz.getMethods())
				.filter(Method::isDefault)
				.collect(toCollection(ArrayList::new));
		if (visibleDefaultMethods.isEmpty()) {
			return visibleDefaultMethods;
		}
		return Arrays.stream(clazz.getInterfaces())
				.map(ReflectionUtils::getMethods)
				.flatMap(List::stream)
				.filter(visibleDefaultMethods::contains)
				.collect(toCollection(ArrayList::new));
		// @formatter:on
	}

	private static List<Field> toSortedMutableList(Field[] fields) {
		// @formatter:off
		return Arrays.stream(fields)
				.sorted(ReflectionUtils::defaultFieldSorter)
				// Use toCollection() instead of toList() to ensure list is mutable.
				.collect(toCollection(ArrayList::new));
		// @formatter:on
	}

	private static List<Method> toSortedMutableList(Method[] methods) {
		// @formatter:off
		return Arrays.stream(methods)
				.sorted(ReflectionUtils::defaultMethodSorter)
				// Use toCollection() instead of toList() to ensure list is mutable.
				.collect(toCollection(ArrayList::new));
		// @formatter:on
	}

	/**
	 * Field comparator inspired by JUnit 4's {@code org.junit.internal.MethodSorter}
	 * implementation.
	 */
	private static int defaultFieldSorter(Field field1, Field field2) {
		return Integer.compare(field1.getName().hashCode(), field2.getName().hashCode());
	}

	/**
	 * Method comparator based upon JUnit 4's {@code org.junit.internal.MethodSorter}
	 * implementation.
	 */
	private static int defaultMethodSorter(Method method1, Method method2) {
		String name1 = method1.getName();
		String name2 = method2.getName();
		int comparison = Integer.compare(name1.hashCode(), name2.hashCode());
		if (comparison == 0) {
			comparison = name1.compareTo(name2);
			if (comparison == 0) {
				comparison = method1.toString().compareTo(method2.toString());
			}
		}
		return comparison;
	}

	private static List<Method> getInterfaceMethods(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		List<Method> allInterfaceMethods = new ArrayList<>();
		for (Class<?> ifc : clazz.getInterfaces()) {

			// @formatter:off
			List<Method> localInterfaceMethods = getMethods(ifc).stream()
					.filter(m -> !isAbstract(m))
					.collect(toList());

			List<Method> superinterfaceMethods = getInterfaceMethods(ifc, traversalMode).stream()
					.filter(method -> !isMethodShadowedByLocalMethods(method, localInterfaceMethods))
					.collect(toList());
			// @formatter:on

			if (traversalMode == TOP_DOWN) {
				allInterfaceMethods.addAll(superinterfaceMethods);
			}
			allInterfaceMethods.addAll(localInterfaceMethods);
			if (traversalMode == BOTTOM_UP) {
				allInterfaceMethods.addAll(superinterfaceMethods);
			}
		}
		return allInterfaceMethods;
	}

	private static List<Field> getInterfaceFields(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		List<Field> allInterfaceFields = new ArrayList<>();
		for (Class<?> ifc : clazz.getInterfaces()) {
			List<Field> localInterfaceFields = getFields(ifc);

			// @formatter:off
			List<Field> superinterfaceFields = getInterfaceFields(ifc, traversalMode).stream()
					.filter(field -> !isFieldShadowedByLocalFields(field, localInterfaceFields))
					.collect(toList());
			// @formatter:on

			if (traversalMode == TOP_DOWN) {
				allInterfaceFields.addAll(superinterfaceFields);
			}
			allInterfaceFields.addAll(localInterfaceFields);
			if (traversalMode == BOTTOM_UP) {
				allInterfaceFields.addAll(superinterfaceFields);
			}
		}
		return allInterfaceFields;
	}

	private static List<Field> getSuperclassFields(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		Class<?> superclass = clazz.getSuperclass();
		if (!isSearchable(superclass)) {
			return Collections.emptyList();
		}
		return findAllFieldsInHierarchy(superclass, traversalMode);
	}

	private static boolean isFieldShadowedByLocalFields(Field field, List<Field> localFields) {
		return localFields.stream().anyMatch(local -> local.getName().equals(field.getName()));
	}

	private static List<Method> getSuperclassMethods(Class<?> clazz, HierarchyTraversalMode traversalMode) {
		Class<?> superclass = clazz.getSuperclass();
		if (!isSearchable(superclass)) {
			return Collections.emptyList();
		}
		return findAllMethodsInHierarchy(superclass, traversalMode);
	}

	private static boolean isMethodShadowedByLocalMethods(Method method, List<Method> localMethods) {
		return localMethods.stream().anyMatch(local -> isMethodShadowedBy(method, local));
	}

	private static boolean isMethodShadowedBy(Method upper, Method lower) {
		return hasCompatibleSignature(upper, lower.getName(), lower.getParameterTypes());
	}

	/**
	 * Determine if the supplied candidate method (typically a method higher in
	 * the type hierarchy) has a signature that is compatible with a method that
	 * has the supplied name and parameter types, taking method sub-signatures
	 * and generics into account.
	 */
	private static boolean hasCompatibleSignature(Method candidate, String methodName, Class<?>[] parameterTypes) {
		if (!methodName.equals(candidate.getName())) {
			return false;
		}
		if (parameterTypes.length != candidate.getParameterCount()) {
			return false;
		}
		// trivial case: parameter types exactly match
		if (Arrays.equals(parameterTypes, candidate.getParameterTypes())) {
			return true;
		}
		// param count is equal, but types do not match exactly: check for method sub-signatures
		// https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.2
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> lowerType = parameterTypes[i];
			Class<?> upperType = candidate.getParameterTypes()[i];
			if (!upperType.isAssignableFrom(lowerType)) {
				return false;
			}
		}
		// lower is sub-signature of upper: check for generics in upper method
		if (isGeneric(candidate)) {
			return true;
		}
		return false;
	}

	static boolean isGeneric(Method method) {
		return isGeneric(method.getGenericReturnType())
				|| Arrays.stream(method.getGenericParameterTypes()).anyMatch(ReflectionUtils::isGeneric);
	}

	private static boolean isGeneric(Type type) {
		return type instanceof TypeVariable || type instanceof GenericArrayType;
	}

	@API(status = INTERNAL, since = "1.11")
	@SuppressWarnings("deprecation") // "AccessibleObject.isAccessible()" is deprecated in Java 9
	public static <T extends Executable> T makeAccessible(T executable) {
		if ((!isPublic(executable) || !isPublic(executable.getDeclaringClass())) && !executable.isAccessible()) {
			executable.setAccessible(true);
		}
		return executable;
	}

	@API(status = INTERNAL, since = "1.11")
	@SuppressWarnings("deprecation") // "AccessibleObject.isAccessible()" is deprecated in Java 9
	public static Field makeAccessible(Field field) {
		if ((!isPublic(field) || !isPublic(field.getDeclaringClass()) || isFinal(field)) && !field.isAccessible()) {
			field.setAccessible(true);
		}
		return field;
	}

	/**
	 * Return all classes and interfaces that can be used as assignment types
	 * for instances of the specified {@link Class}, including itself.
	 *
	 * @param clazz the {@code Class} to look up
	 * @see Class#isAssignableFrom
	 */
	public static Set<Class<?>> getAllAssignmentCompatibleClasses(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");

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

	/**
	 * Determine if the supplied class is <em>searchable</em>: is non-null and is
	 * not equal to the class reference for {@code java.lang.Object}.
	 *
	 * <p>This method is often used to determine if a superclass should be
	 * searched but may be applicable for other use cases as well.
	 * @since 1.6
	 */
	private static boolean isSearchable(Class<?> clazz) {
		return (clazz != null && clazz != Object.class);
	}

	/**
	 * Get the underlying cause of the supplied {@link Throwable}.
	 *
	 * <p>If the supplied {@code Throwable} is an instance of
	 * {@link InvocationTargetException}, this method will be invoked
	 * recursively with the underlying
	 * {@linkplain InvocationTargetException#getTargetException() target
	 * exception}; otherwise, this method returns the supplied {@code Throwable}.
	 */
	private static Throwable getUnderlyingCause(Throwable t) {
		if (t instanceof InvocationTargetException) {
			return getUnderlyingCause(((InvocationTargetException) t).getTargetException());
		}
		return t;
	}

}
