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

import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.testers.CanBeTestClass;
import org.junit.gen5.engine.junit5.testers.IsTestContext;
import org.junit.gen5.engine.junit5.testers.IsTestMethod;

public class JUnit5TestableFactory {

	private static final String SEPARATORS = ":@#";

	private CanBeTestClass canBeTestClass = new CanBeTestClass();
	private IsTestContext isTestContext = new IsTestContext();
	private IsTestMethod isTestMethod = new IsTestMethod();

	public JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		Preconditions.notEmpty(uniqueId, "uniqueId must not be empty");
		List<String> parts = split(uniqueId);
		Preconditions.condition(parts.remove(0).equals(engineId), "uniqueId must start with engineId");

		return createTestable(uniqueId, engineId, parts, null);
	}

	public JUnit5Testable fromClassName(String className, String engineId) {
		Preconditions.notEmpty(className, "className must not be empty");
		Class<?> clazz = classByName(className);
		if (clazz == null) {
			throwCannotResolveClassNameException(className);
		}
		return fromClass(clazz, engineId);
	}

	public JUnit5Testable fromClass(Class<?> clazz, String engineId) {
		Preconditions.notNull(clazz, "clazz must not be null");
		if (canBeTestClass.test(clazz)) {
			String uniqueId = engineId + ":" + clazz.getName();
			return new JUnit5Class(uniqueId, clazz);
		}
		if (isTestContext.test(clazz)) {
			return createContextTestable(clazz, clazz.getEnclosingClass(), engineId);
		}
		throwCannotResolveClassException(clazz);
		return null; //cannot happen
	}

	private JUnit5Testable createContextTestable(Class<?> testClass, Class<?> container, String engineId) {
		String uniqueId = fromClass(container, engineId).getUniqueId() + "@" + testClass.getSimpleName();
		return new JUnit5Context(uniqueId, testClass, container);
	}

	public JUnit5Testable fromMethod(Method testMethod, Class<?> clazz, String engineId) {
		JUnit5Testable containerTestable = fromClass(clazz, engineId);
		if (!isTestMethod.test(testMethod)) {
			throwCannotResolveMethodException(testMethod);
		}
		String uniqueId = createUniqueIdForMethod(testMethod, containerTestable.getUniqueId());
		return new JUnit5Method(uniqueId, testMethod, clazz);
	}

	private String createUniqueIdForMethod(Method testMethod, String parentUniqueId) {
		return parentUniqueId + "#" + testMethod.getName() + createParameterIdPart(testMethod);
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

	private String createParameterIdPart(Method testMethod) {
		String parameterString = Arrays.stream(testMethod.getParameterTypes()).map(Class::getName).collect(
			Collectors.joining(","));
		return "(" + parameterString + ")";
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
				next = fromClass(findNestedContext(head, container), engineId);
				break;
			}
			case '#': {
				Class<?> container = ((JUnit5Class) last).getJavaClass();
				next = fromMethod(findMethod(head, container, uniqueId), container, engineId);
				break;
			}
			default:
				throwCannotResolveUniqueIdException(uniqueId, head);
		}
		return createTestable(uniqueId, engineId, parts, next);
	}

	private Method findMethod(String methodSpecPart, Class<?> clazz, String uniqueId) {
		int startParams = methodSpecPart.indexOf('(');
		String methodName = methodSpecPart.substring(1, startParams);
		int endParams = methodSpecPart.lastIndexOf(')');
		String paramsPart = methodSpecPart.substring(startParams + 1, endParams);
		Class<?>[] parameterTypes = findParameterTypes(paramsPart, uniqueId);
		return findMethod(clazz, methodName, parameterTypes);
	}

	private Class<?>[] findParameterTypes(String paramsPart, String uniqueId) {
		if (paramsPart.isEmpty()) {
			return new Class<?>[0];
		}
		List<Class<?>> types = Arrays.stream(paramsPart.split(",")).map(typeName -> {
			Optional<Class<?>> aClass = ReflectionUtils.loadClass(typeName);
			if (aClass.isPresent())
				return aClass.get();
			else {
				throwCannotResolveUniqueIdException(uniqueId, paramsPart);
				return null; //cannot happen
			}
		}).collect(Collectors.toList());
		return types.toArray(new Class<?>[types.size()]);
	}

	private Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes).orElseThrow(
			() -> new IllegalArgumentException(String.format("No method with 'name' %s and paramter types '%s'",
				methodName, Arrays.toString(parameterTypes))));
	}

	private Class<?> findNestedContext(String nameExtension, Class<?> containerClass) {
		String fullClassName = containerClass.getName() + "$" + nameExtension.substring(1);
		return classByName(fullClassName);
	}

	private Class<?> findTopLevelClass(String classNamePart) {
		String className = classNamePart.substring(1);
		return classByName(className);
	}

	private Class<?> classByName(String className) {
		return loadClass(className).orElseThrow(
			() -> new IllegalArgumentException(String.format("Cannot resolve class name '%s'", className)));
	}

	private void throwCannotResolveClassException(Class<?> clazz) {
		throw new IllegalArgumentException(
			String.format("Cannot resolve class name '%s' because it's not a test container", clazz.getName()));
	}

	private void throwCannotResolveClassNameException(String className) {
		throw new IllegalArgumentException(String.format("Cannot resolve class name '%s'", className));
	}

	private void throwCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
		throw new IllegalArgumentException(
			String.format("Cannot resolve part '%s' of unique id '%s'", uniqueIdPart, fullUniqueId));
	}

	private static void throwCannotResolveMethodException(Method method) {
		throw new IllegalArgumentException(String.format("Method '%s' is not a test method.", method.getName()));
	}

}
