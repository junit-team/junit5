/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

/**
 * {@link ArgumentsProvider} for {@link FieldSource @FieldSource}.
 *
 * @since 5.11
 */
class FieldArgumentsProvider extends AnnotationBasedArgumentsProvider<FieldSource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, FieldSource fieldSource) {
		Class<?> testClass = context.getRequiredTestClass();
		Object testInstance = context.getTestInstance().orElse(null);
		String[] fieldNames = fieldSource.value();
		if (fieldNames.length == 0) {
			fieldNames = new String[] { context.getRequiredTestMethod().getName() };
		}
		// @formatter:off
		return stream(fieldNames)
				.map(fieldName -> findField(testClass, fieldName))
				.map(field -> validateField(field, testInstance))
				.map(field -> readField(field, testInstance))
				.flatMap(fieldValue -> {
					if (fieldValue instanceof Supplier<?>) {
						fieldValue = ((Supplier<?>) fieldValue).get();
					}
					return CollectionUtils.toStream(fieldValue);
				})
				.map(ArgumentsUtils::toArguments);
		// @formatter:on
	}

	// package-private for testing
	static Field findField(Class<?> testClass, String fieldName) {
		Preconditions.notBlank(fieldName, "Field name must not be blank");
		fieldName = fieldName.trim();

		Class<?> clazz = testClass;
		if (fieldName.contains("#") || fieldName.contains(".")) {
			String[] fieldParts = ReflectionUtils.parseFullyQualifiedFieldName(fieldName);
			String className = fieldParts[0];
			fieldName = fieldParts[1];
			ClassLoader classLoader = ClassLoaderUtils.getClassLoader(testClass);
			clazz = ReflectionUtils.loadRequiredClass(className, classLoader);
		}

		Class<?> resolvedClass = clazz;
		String resolvedFieldName = fieldName;
		Predicate<Field> nameMatches = field -> field.getName().equals(resolvedFieldName);
		Field field = ReflectionUtils.streamFields(resolvedClass, nameMatches, HierarchyTraversalMode.BOTTOM_UP)//
				.findFirst()//
				.orElse(null);

		Preconditions.notNull(field,
			() -> format("Could not find field named [%s] in class [%s]", resolvedFieldName, resolvedClass.getName()));
		return field;
	}

	private static Field validateField(Field field, Object testInstance) {
		Preconditions.condition(field.getDeclaringClass().isInstance(testInstance) || ReflectionUtils.isStatic(field),
			() -> format("Field '%s' must be static: local @FieldSource fields must be static "
					+ "unless the PER_CLASS @TestInstance lifecycle mode is used; "
					+ "external @FieldSource fields must always be static.",
				field.toGenericString()));
		return field;
	}

	private static Object readField(Field field, Object testInstance) {
		Object value = ReflectionUtils.tryToReadFieldValue(field, testInstance).getOrThrow(
			cause -> new JUnitException(format("Could not read field [%s]", field.getName()), cause));

		String fieldName = field.getName();
		String declaringClass = field.getDeclaringClass().getName();

		Preconditions.notNull(value,
			() -> format("The value of field [%s] in class [%s] must not be null", fieldName, declaringClass));

		Preconditions.condition(!(value instanceof BaseStream),
			() -> format("The value of field [%s] in class [%s] must not be a stream", fieldName, declaringClass));

		Preconditions.condition(!(value instanceof Iterator),
			() -> format("The value of field [%s] in class [%s] must not be an Iterator", fieldName, declaringClass));

		Preconditions.condition(isConvertibleToStream(field, value),
			() -> format("The value of field [%s] in class [%s] must be convertible to a Stream", fieldName,
				declaringClass));

		return value;
	}

	/**
	 * Determine if the supplied value can be converted into a {@code Stream} or
	 * if the declared type of the supplied field is a {@link Supplier} of a type
	 * that can be converted into a {@code Stream}.
	 */
	private static boolean isConvertibleToStream(Field field, Object value) {
		// Check actual value type.
		if (CollectionUtils.isConvertibleToStream(value.getClass())) {
			return true;
		}

		// Check declared type T of Supplier<T>.
		if (Supplier.class.isAssignableFrom(field.getType())) {
			Type genericType = field.getGenericType();
			if (genericType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) genericType;
				Type[] typeArguments = parameterizedType.getActualTypeArguments();
				if (typeArguments.length == 1) {
					Type type = typeArguments[0];
					// Handle cases such as Supplier<IntStream>
					if (type instanceof Class) {
						Class<?> clazz = (Class<?>) type;
						return CollectionUtils.isConvertibleToStream(clazz);
					}
					// Handle cases such as Supplier<Stream<String>>
					if (type instanceof ParameterizedType) {
						Type rawType = ((ParameterizedType) type).getRawType();
						if (rawType instanceof Class<?>) {
							Class<?> clazz = (Class<?>) rawType;
							return CollectionUtils.isConvertibleToStream(clazz);
						}
					}
				}
			}
		}
		return false;
	}

}
