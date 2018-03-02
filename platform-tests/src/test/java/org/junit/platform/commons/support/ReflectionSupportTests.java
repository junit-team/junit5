/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 1.0
 */
class ReflectionSupportTests {

	private final Predicate<Class<?>> allTypes = type -> true;
	private final Predicate<String> allNames = name -> true;
	private final Predicate<Method> allMethods = name -> true;

	@Test
	void loadClassDelegates() {
		assertEquals(ReflectionUtils.loadClass("-"), ReflectionSupport.loadClass("-"));
		assertEquals(ReflectionUtils.loadClass("A"), ReflectionSupport.loadClass("A"));
		assertEquals(ReflectionUtils.loadClass("java.io.Bits"), ReflectionSupport.loadClass("java.io.Bits"));
	}

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
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findAllClassesInPackage("void.return.null", allTypes, allNames));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionSupport.findAllClassesInPackage("void.return.null", allTypes, allNames));
		assertNotEquals(0, ReflectionSupport.findAllClassesInPackage("org.junit", allTypes, allNames).size());
		assertEquals(ReflectionUtils.findAllClassesInPackage("org.junit", allTypes, allNames),
			ReflectionSupport.findAllClassesInPackage("org.junit", allTypes, allNames));
	}

	@Test
	void findAllClassesInModuleDelegates() {
		assertEquals(ReflectionUtils.findAllClassesInModule("org.junit.platform.commons", allTypes, allNames),
			ReflectionSupport.findAllClassesInModule("org.junit.platform.commons", allTypes, allNames));
	}

	@Test
	void findMethodsDelegates() {
		assertEquals(
			ReflectionUtils.findMethods(ReflectionSupportTests.class, allMethods,
				ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP),
			ReflectionSupport.findMethods(ReflectionSupportTests.class, allMethods, HierarchyTraversalMode.BOTTOM_UP));
		assertEquals(
			ReflectionUtils.findMethods(ReflectionSupportTests.class, allMethods,
				ReflectionUtils.HierarchyTraversalMode.TOP_DOWN),
			ReflectionSupport.findMethods(ReflectionSupportTests.class, allMethods, HierarchyTraversalMode.TOP_DOWN));
	}
}
