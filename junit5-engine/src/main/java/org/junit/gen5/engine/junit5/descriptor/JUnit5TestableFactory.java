/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.junit5.discovery.IsNestedTestClass;
import org.junit.gen5.engine.junit5.discovery.IsPotentialTestContainer;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;

/**
 * @since 5.0
 */
class JUnit5TestableFactory {

	private static final String SEPARATORS = ":@#";

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();
	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();
	private static final IsTestMethod isTestMethod = new IsTestMethod();

	JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		Preconditions.notBlank(uniqueId, "Unique ID must not be null or empty");
		List<String> parts = split(uniqueId);
		Preconditions.condition(parts.remove(0).equals(engineId), "uniqueId must start with engineId");

		return createTestable(uniqueId, engineId, parts, null);
	}

	JUnit5Testable fromClass(Class<?> clazz, String engineId) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(engineId, "Engine ID must not be null or empty");
		if (isPotentialTestContainer.test(clazz)) {
			String uniqueId = engineId + ":" + clazz.getName();
			return new JUnit5Class(uniqueId, clazz);
		}
		if (isNestedTestClass.test(clazz)) {
			return createNestedClassTestable(clazz, clazz.getEnclosingClass(), engineId);
		}
		throwCannotResolveClassException(clazz);
		return null; //cannot happen
	}

	private JUnit5Testable createNestedClassTestable(Class<?> testClass, Class<?> container, String engineId) {
		String uniqueId = fromClass(container, engineId).getUniqueId() + "@" + testClass.getSimpleName();
		return new JUnit5NestedClass(uniqueId, testClass, container);
	}

	JUnit5Testable fromMethod(Method testMethod, Class<?> clazz, String engineId) {
		if (!isTestMethod.test(testMethod)) {
			throwCannotResolveMethodException(testMethod);
		}
		String uniqueId = String.format("%s#%s(%s)", fromClass(clazz, engineId).getUniqueId(), testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
		return new JUnit5Method(uniqueId, testMethod, clazz);
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

	private JUnit5Testable createTestable(String uniqueId, String engineId, List<String> parts, JUnit5Testable last) {
		if (parts.isEmpty())
			return last;
		JUnit5Testable next = null;
		String head = parts.remove(0);
		switch (head.charAt(0)) {
			case ':':
				next = fromClass(findTopLevelClass(head), engineId);
				break;
			case '@': {
				Class<?> container = ((JUnit5Class) last).getJavaClass();
				next = fromClass(findNestedClass(head, container), engineId);
				break;
			}
			case '#': {
				Class<?> container = ((JUnit5Class) last).getJavaClass();
				next = fromMethod(findMethod(head, container, uniqueId), container, engineId);
				break;
			}
			default:
				throw createCannotResolveUniqueIdException(uniqueId, head);
		}
		return createTestable(uniqueId, engineId, parts, next);
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
			() -> new PreconditionViolationException(String.format("No method with name '%s' and parameter types '%s'",
				methodName, StringUtils.nullSafeToString(parameterTypes))));
	}

	private Class<?> findNestedClass(String nameExtension, Class<?> containerClass) {
		return classByName(containerClass.getName() + "$" + nameExtension.substring(1));
	}

	private Class<?> findTopLevelClass(String classNamePart) {
		return loadClassByName(classNamePart.substring(1));
	}

	private Class<?> classByName(String className) {
		return loadClass(className).orElseThrow(
			() -> new PreconditionViolationException(String.format("Cannot resolve class name '%s'", className)));
	}

	private Class<?> loadClassByName(String className) {
		return ReflectionUtils.loadClass(className).orElse(null);
	}

	private Class<?> loadRequiredClass(String className, String fullUniqueId, String uniqueIdPart) {
		return ReflectionUtils.loadClass(className).orElseThrow(
			() -> createCannotResolveUniqueIdException(fullUniqueId, uniqueIdPart));
	}

	private static RuntimeException createCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
		return new PreconditionViolationException(
			String.format("Cannot resolve part '%s' of unique ID '%s'", uniqueIdPart, fullUniqueId));
	}

	private static void throwCannotResolveMethodException(Method method) {
		throw new PreconditionViolationException(String.format("Method '%s' is not a test method.", method.getName()));
	}

	private void throwCannotResolveClassException(Class<?> clazz) {
		throw new PreconditionViolationException(
			String.format("Cannot resolve class name '%s' because it's not a test container", clazz.getName()));
	}

}
