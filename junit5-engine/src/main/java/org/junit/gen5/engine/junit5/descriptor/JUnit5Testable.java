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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import lombok.Value;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;

@Value
public class JUnit5Testable {

	private static final String SEPARATORS = ":$#";

	private static List<String> split(String uniqueId) {
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

	public static JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		Preconditions.notEmpty(uniqueId, "uniqueId must not be empty");
		List<String> parts = split(uniqueId);
		Preconditions.condition(parts.remove(0).equals(engineId), "uniqueId must start with engineId");

		return createElement(uniqueId, parts);
	}

	public static JUnit5Testable fromClassName(String className, String engineId) {
		Preconditions.notEmpty(className, "className must not be empty");
		Class<?> clazz = classByName(className);
		if (clazz == null) {
			throwCannotResolveClassNameException(className);
		}
		return fromClass(clazz, engineId);
	}

	public static JUnit5Testable fromClass(Class<?> clazz, String engineId) {
		Preconditions.notNull(clazz, "clazz must not be null");
		String uniqueId = engineId + ":" + clazz.getName();
		return new JUnit5Testable(uniqueId, clazz, null);
	}

	public static JUnit5Testable fromMethod(Method testMethod, Class<?> clazz, String engineId) {
		String uniqueId = fromClass(clazz, engineId).getUniqueId() + "#" + testMethod.getName() + "()";
		return new JUnit5Testable(uniqueId, testMethod, clazz);
	}

	private static JUnit5Testable createElement(String uniqueId, List<String> parts) {
		AnnotatedElement currentJavaElement = null;
		Class<?> currentJavaContainer = null;
		String head = parts.remove(0);
		while (true) {
			switch (head.charAt(0)) {
				case ':':
					currentJavaElement = findTopLevelClass(head);
					break;
				case '$':
					currentJavaContainer = (Class<?>) currentJavaElement;
					currentJavaElement = findNestedClass(head, (Class<?>) currentJavaElement);
					break;
				case '#':
					currentJavaContainer = (Class<?>) currentJavaElement;
					currentJavaElement = findMethod(head, (Class<?>) currentJavaElement);
					break;
				default:
					currentJavaContainer = null;
					currentJavaElement = null;
			}

			if (currentJavaElement == null) {
				throwCannotResolveUniqueIdException(uniqueId, head);
			}
			if (parts.isEmpty())
				break;
			head = parts.remove(0);
		}
		return new JUnit5Testable(uniqueId, currentJavaElement, currentJavaContainer);
	}

	private static Method findMethod(String methodSpecPart, Class<?> clazz) {
		String methodName = methodSpecPart.substring(1, methodSpecPart.length() - 2);
		return findMethodByName(clazz, methodName);
	}

	private static Method findMethodByName(Class<?> clazz, String methodName) {
		//Todo consider parameters
		Predicate<Method> methodPredicate = method -> method.getName().equals(methodName);

		List<Method> candidates = ReflectionUtils.findMethods(clazz, methodPredicate,
			ReflectionUtils.MethodSortOrder.HierarchyDown);
		if (candidates.isEmpty()) {
			return null;
		}
		return candidates.get(0);
	}

	private static Class<?> findNestedClass(String nameExtension, Class<?> containerClass) {
		String fullClassName = containerClass.getName() + nameExtension;
		return classByName(fullClassName);
	}

	private static Class<?> findTopLevelClass(String classNamePart) {
		String className = classNamePart.substring(1);
		return classByName(className);
	}

	private static Class<?> classByName(String className) {
		Class<?> clazz = null;
		try {
			clazz = loadClass(className);
		}
		catch (ClassNotFoundException e) {
			clazz = null;
		}
		return clazz;
	}

	private static void throwCannotResolveClassNameException(String className) {
		throw new IllegalArgumentException(String.format("Cannot resolve class name '%s'", className));
	}

	private static void throwCannotResolveUniqueIdException(String fullUniqueId, String uniqueIdPart) {
		throw new IllegalArgumentException(
			String.format("Cannot resolve part '%s' of unique id '%s'", uniqueIdPart, fullUniqueId));
	}

	private final String uniqueId;
	private final AnnotatedElement javaElement;
	private final Class<?> javaContainer;

	private JUnit5Testable(String uniqueId, AnnotatedElement javaElement, Class<?> javaContainer) {
		this.uniqueId = uniqueId;
		this.javaElement = javaElement;
		this.javaContainer = javaContainer;
	}

}
