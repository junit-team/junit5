/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @author Sam Brannen
 * @since 5.0
 */
class JUnit5TestableFactory {

	private static final String SEPARATORS = ":$#";

	JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		Preconditions.notBlank(uniqueId, "Unique ID must not be null or empty");
		List<String> parts = split(uniqueId);
		Preconditions.condition(parts.remove(0).equals(engineId), "uniqueId must start with engineId");

		return createElement(uniqueId, parts);
	}

	JUnit5Testable fromClassName(String className, String engineId) {
		Preconditions.notBlank(className, "className must not be null or empty");
		Class<?> clazz = loadClassByName(className);
		if (clazz == null) {
			throw new IllegalArgumentException(String.format("Cannot resolve class name '%s'", className));
		}
		return fromClass(clazz, engineId);
	}

	JUnit5Testable fromClass(Class<?> clazz, String engineId) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(engineId, "Engine ID must not be null or empty");
		String uniqueId = engineId + ":" + clazz.getName();
		return new JUnit5Class(uniqueId, clazz);
	}

	JUnit5Testable fromMethod(Method method, Class<?> clazz, String engineId) {
		Preconditions.notNull(method, "Method must not be null");
		Preconditions.notNull(clazz, "Class must not be null");
		String uniqueId = String.format("%s#%s(%s)", fromClass(clazz, engineId).getUniqueId(), method.getName(),
			ObjectUtils.nullSafeToString(method.getParameterTypes()));
		return new JUnit5Method(uniqueId, method, clazz);
	}

	private List<String> split(String uniqueId) {
		List<String> parts = new ArrayList<>();
		String currentPart = "";
		for (char c : uniqueId.toCharArray()) {
			if (SEPARATORS.contains(Character.toString(c))) {
				parts.add(currentPart);
				currentPart = "";
			}
			currentPart += c;
		}
		parts.add(currentPart);
		return parts;
	}

	private JUnit5Testable createElement(String uniqueId, List<String> parts) {
		AnnotatedElement currentJavaElement = null;
		Class<?> currentJavaContainer = null;
		String head = parts.remove(0);
		while (true) {
			switch (head.charAt(0)) {
				case ':': {
					currentJavaElement = findTopLevelClass(head);
					break;
				}
				case '$': {
					currentJavaContainer = (Class<?>) currentJavaElement;
					currentJavaElement = findNestedClass(head, (Class<?>) currentJavaElement);
					break;
				}
				case '#': {
					currentJavaContainer = (Class<?>) currentJavaElement;
					currentJavaElement = findMethod(head, currentJavaContainer, uniqueId);
					break;
				}
				default: {
					currentJavaContainer = null;
					currentJavaElement = null;
				}
			}

			if (currentJavaElement == null) {
				throw createCannotResolveUniqueIdException(uniqueId, head);
			}
			if (parts.isEmpty()) {
				break;
			}
			head = parts.remove(0);
		}
		if (currentJavaElement instanceof Method) {
			return new JUnit5Method(uniqueId, (Method) currentJavaElement, currentJavaContainer);
		}
		if (currentJavaElement instanceof Class) {
			return new JUnit5Class(uniqueId, (Class<?>) currentJavaElement);
		}

		// Should not happen
		return null;
	}

	private Method findMethod(String methodSpecPart, Class<?> clazz, String uniqueId) {
		// TODO Throw IAE when format wrong. Currently you get IndexOutOfBoundsException.
		int startParams = methodSpecPart.indexOf('(');
		String methodName = methodSpecPart.substring(1, startParams);
		int endParams = methodSpecPart.lastIndexOf(')');
		String paramsPart = methodSpecPart.substring(startParams + 1, endParams);
		Class<?>[] parameterTypes = resolveParameterTypes(paramsPart, uniqueId);
		return findMethod(clazz, methodName, parameterTypes);
	}

	private Class<?>[] resolveParameterTypes(String paramsPart, String uniqueId) {
		if (paramsPart.isEmpty()) {
			return new Class<?>[0];
		}

		// @formatter:off
		List<Class<?>> types = Arrays.stream(paramsPart.split(","))
				.map(className -> loadRequiredClass(className, uniqueId, paramsPart))
				.collect(Collectors.toList());
		// @formatter:on

		return types.toArray(new Class<?>[types.size()]);
	}

	private Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes).orElseThrow(
			() -> new IllegalArgumentException(String.format("No method with name '%s' and parameter types '%s'",
				methodName, ObjectUtils.nullSafeToString(parameterTypes))));
	}

	private static Class<?> findNestedClass(String nameExtension, Class<?> containerClass) {
		return loadClassByName(containerClass.getName() + nameExtension);
	}

	private static Class<?> findTopLevelClass(String classNamePart) {
		return loadClassByName(classNamePart.substring(1));
	}

	private static Class<?> loadClassByName(String className) {
		return ReflectionUtils.loadClass(className).orElse(null);
	}

	private Class<?> loadRequiredClass(String className, String fullUniqueId, String uniqueIdPart) {
		return ReflectionUtils.loadClass(className).orElseThrow(
			() -> createCannotResolveUniqueIdException(fullUniqueId, uniqueIdPart));
	}

	private static RuntimeException createCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
		return new IllegalArgumentException(
			String.format("Cannot resolve part '%s' of unique ID '%s'", uniqueIdPart, fullUniqueId));
	}

}
