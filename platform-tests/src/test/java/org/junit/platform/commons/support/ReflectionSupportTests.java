/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 1.0
 */
class ReflectionSupportTests {

	private final Predicate<Class<?>> allTypes = type -> true;
	private final Predicate<String> allNames = name -> true;
	private final Predicate<Method> allMethods = name -> true;

	@TestFactory
	List<DynamicTest> findAllClassesInClasspathRootDelegates() throws Throwable {
		List<DynamicTest> tests = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get(".").toRealPath());
		paths.addAll(ReflectionUtils.getAllClasspathRootDirectories());
		for (Path path : paths) {
			URI root = path.toUri();
			String displayName = root.getPath();
			if (displayName.length() > 42) {
				displayName = "..." + displayName.substring(displayName.length() - 42);
			}
			tests.add(DynamicTest.dynamicTest(displayName,
				() -> assertEquals(ReflectionUtils.findAllClassesInClasspathRoot(root, allTypes, allNames),
					ReflectionSupport.findAllClassesInClasspathRoot(root, allTypes, allNames))));
		}
		return tests;
	}

	@Test
	void findAllClassesInPackageDelegates() {
		assertEquals(0, ReflectionSupport.findAllClassesInPackage("illegal package name", allTypes, allNames).size());
		assertEquals(ReflectionUtils.findAllClassesInPackage("illegal package name", allTypes, allNames),
			ReflectionSupport.findAllClassesInPackage("illegal package name", allTypes, allNames));
		assertNotEquals(0, ReflectionSupport.findAllClassesInPackage("org.junit", allTypes, allNames).size());
		assertEquals(ReflectionUtils.findAllClassesInPackage("org.junit", allTypes, allNames),
			ReflectionSupport.findAllClassesInPackage("org.junit", allTypes, allNames));
	}

	@Test
	void findMethodsDelegates() {
		assertEquals(
			ReflectionUtils.findMethods(ReflectionSupportTests.class, allMethods,
				ReflectionUtils.MethodSortOrder.HierarchyUp),
			ReflectionSupport.findMethods(ReflectionSupportTests.class, allMethods, MethodSortOrder.HierarchyUp));
		assertEquals(
			ReflectionUtils.findMethods(ReflectionSupportTests.class, allMethods,
				ReflectionUtils.MethodSortOrder.HierarchyDown),
			ReflectionSupport.findMethods(ReflectionSupportTests.class, allMethods, MethodSortOrder.HierarchyDown));
	}
}
