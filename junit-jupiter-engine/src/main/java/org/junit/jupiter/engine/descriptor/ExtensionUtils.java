/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.Preconditions;

/**
 * Collection of utilities for working with extensions and the extension registry.
 *
 * @since 5.1
 * @see ExtensionRegistry
 * @see ExtendWith
 * @see RegisterExtension
 */
final class ExtensionUtils {

	private static final Predicate<Field> isStaticExtension = new IsStaticExtensionField();
	private static final Predicate<Field> isNonStaticExtension = new IsNonStaticExtensionField();

	private ExtensionUtils() {
		/* no-op */
	}

	/**
	 * Populate a new {@link ExtensionRegistry} from extension types declared via
	 * {@link ExtendWith @ExtendWith} on the supplied {@link AnnotatedElement}.
	 *
	 * @param parentRegistry the parent extension registry to set in the newly
	 * created registry; never {@code null}
	 * @param annotatedElement the annotated element on which to search for
	 * declarations of {@code @ExtendWith}; never {@code null}
	 *
	 * @return the new extension registry; never {@code null}
	 * @since 5.0
	 */
	static ExtensionRegistry populateNewExtensionRegistryFromExtendWithAnnotation(ExtensionRegistry parentRegistry,
			AnnotatedElement annotatedElement) {

		Preconditions.notNull(annotatedElement, "AnnotatedElement must not be null");
		Preconditions.notNull(parentRegistry, "Parent ExtensionRegistry must not be null");

		// @formatter:off
		List<Class<? extends Extension>> extensionTypes = findRepeatableAnnotations(annotatedElement, ExtendWith.class).stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.collect(toList());
		// @formatter:on

		return ExtensionRegistry.createRegistryFrom(parentRegistry, extensionTypes);
	}

	/**
	 * Register extensions in the supplied registry from fields in the supplied
	 * class that are annotated with {@link RegisterExtension @RegisterExtension}.
	 *
	 * @param registry the registry in which to register the extensions; never {@code null}
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param instance the instance of the supplied class; may be {@code null}
	 * when searching for {@code static} fields in the class
	 */
	static void registerExtensionsFromFields(ExtensionRegistry registry, Class<?> clazz, Object instance) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(registry, "ExtensionRegistry must not be null");

		Predicate<Field> predicate = (instance == null) ? isStaticExtension : isNonStaticExtension;

		findAnnotatedFields(clazz, RegisterExtension.class, predicate).forEach(field -> {
			readFieldValue(field, instance).ifPresent(value -> {
				Extension extension = (Extension) value;
				registry.registerExtension(extension, field);
			});
		});
	}

	static class IsNonStaticExtensionField implements Predicate<Field> {

		@Override
		public boolean test(Field field) {
			// Please do not collapse the following into a single statement.
			if (isStatic(field)) {
				return false;
			}
			if (isPrivate(field)) {
				return false;
			}
			if (!Extension.class.isAssignableFrom(field.getType())) {
				return false;
			}
			return true;
		}
	}

	static class IsStaticExtensionField implements Predicate<Field> {

		@Override
		public boolean test(Field field) {
			// Please do not collapse the following into a single statement.
			if (!isStatic(field)) {
				return false;
			}
			if (isPrivate(field)) {
				return false;
			}
			if (!Extension.class.isAssignableFrom(field.getType())) {
				return false;
			}
			return true;
		}
	}

}
