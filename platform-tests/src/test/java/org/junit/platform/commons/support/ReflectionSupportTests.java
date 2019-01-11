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
import static org.junit.platform.commons.support.PreconditionAssertions.assertPreconditionViolationException;
import static org.junit.platform.commons.support.PreconditionAssertions.assertPreconditionViolationExceptionForString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
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

	private static final Predicate<Class<?>> allTypes = type -> true;
	private static final Predicate<String> allNames = name -> true;
	private static final Predicate<Method> allMethods = name -> true;
	private static final Predicate<Field> allFields = name -> true;

	@Test
	@SuppressWarnings("deprecation")
	void loadClassDelegates() {
		assertEquals(ReflectionUtils.loadClass("-"), ReflectionSupport.loadClass("-"));
		assertEquals(ReflectionUtils.loadClass("A"), ReflectionSupport.loadClass("A"));
		assertEquals(ReflectionUtils.loadClass("java.io.Bits"), ReflectionSupport.loadClass("java.io.Bits"));
	}

	@Test
	@SuppressWarnings("deprecation")
	void loadClassPreconditions() {
		assertPreconditionViolationExceptionForString("Class name", () -> ReflectionSupport.loadClass(null));
		assertPreconditionViolationExceptionForString("Class name", () -> ReflectionSupport.loadClass(""));
	}

	@Test
	void tryToLoadClassDelegates() {
		assertEquals(ReflectionUtils.tryToLoadClass("-").toOptional(),
			ReflectionSupport.tryToLoadClass("-").toOptional());
		assertEquals(ReflectionUtils.tryToLoadClass("A").toOptional(),
			ReflectionSupport.tryToLoadClass("A").toOptional());
		assertEquals(ReflectionUtils.tryToLoadClass("java.io.Bits"), ReflectionSupport.tryToLoadClass("java.io.Bits"));
	}

	@Test
	void tryToLoadClassPreconditions() {
		assertPreconditionViolationExceptionForString("Class name", () -> ReflectionSupport.tryToLoadClass(null));
		assertPreconditionViolationExceptionForString("Class name", () -> ReflectionSupport.tryToLoadClass(""));
	}

	@TestFactory
	List<DynamicTest> findAllClassesInClasspathRootDelegates() throws Throwable {
		List<DynamicTest> tests = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		paths.add(Path.of(".").toRealPath());
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
	void findAllClassesInClasspathRootPreconditions() {
		URI path = Path.of(".").toUri();
		assertPreconditionViolationException("root",
			() -> ReflectionSupport.findAllClassesInClasspathRoot(null, allTypes, allNames));
		assertPreconditionViolationException("class predicate",
			() -> ReflectionSupport.findAllClassesInClasspathRoot(path, null, allNames));
		assertPreconditionViolationException("name predicate",
			() -> ReflectionSupport.findAllClassesInClasspathRoot(path, allTypes, null));
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
	void findAllClassesInPackagePreconditions() {
		assertPreconditionViolationException("package name",
			() -> ReflectionSupport.findAllClassesInPackage(null, allTypes, allNames));
		assertPreconditionViolationException("class predicate",
			() -> ReflectionSupport.findAllClassesInPackage("org.junit", null, allNames));
		assertPreconditionViolationException("name predicate",
			() -> ReflectionSupport.findAllClassesInPackage("org.junit", allTypes, null));
	}

	@Test
	void findAllClassesInModuleDelegates() {
		assertEquals(ReflectionUtils.findAllClassesInModule("org.junit.platform.commons", allTypes, allNames),
			ReflectionSupport.findAllClassesInModule("org.junit.platform.commons", allTypes, allNames));
	}

	@Test
	void findAllClassesInModulePreconditions() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> ReflectionSupport.findAllClassesInModule(null, allTypes, allNames));
		assertEquals("Module name must not be null or empty", exception.getMessage());
		assertPreconditionViolationException("class predicate",
			() -> ReflectionSupport.findAllClassesInModule("org.junit.platform.commons", null, allNames));
		assertPreconditionViolationException("name predicate",
			() -> ReflectionSupport.findAllClassesInModule("org.junit.platform.commons", allTypes, null));
	}

	@Test
	void findFieldsDelegates() {

		ReflectionSupport.findFields(ReflectionSupportTests.class, allFields, HierarchyTraversalMode.BOTTOM_UP).forEach(
			System.out::println);

		assertEquals(
			ReflectionUtils.findFields(ReflectionSupportTests.class, allFields,
				ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP),
			ReflectionSupport.findFields(ReflectionSupportTests.class, allFields, HierarchyTraversalMode.BOTTOM_UP));
		assertEquals(
			ReflectionUtils.findFields(ReflectionSupportTests.class, allFields,
				ReflectionUtils.HierarchyTraversalMode.TOP_DOWN),
			ReflectionSupport.findFields(ReflectionSupportTests.class, allFields, HierarchyTraversalMode.TOP_DOWN));
	}

	@Test
	void findFieldsPreconditions() {
		assertPreconditionViolationException("Class",
			() -> ReflectionSupport.findFields(null, allFields, HierarchyTraversalMode.BOTTOM_UP));
		assertPreconditionViolationException("Class",
			() -> ReflectionSupport.findFields(null, allFields, HierarchyTraversalMode.TOP_DOWN));
		assertPreconditionViolationException("Predicate",
			() -> ReflectionSupport.findFields(ReflectionSupportTests.class, null, HierarchyTraversalMode.BOTTOM_UP));
		assertPreconditionViolationException("Predicate",
			() -> ReflectionSupport.findFields(ReflectionSupportTests.class, null, HierarchyTraversalMode.TOP_DOWN));
		assertPreconditionViolationException("HierarchyTraversalMode",
			() -> ReflectionSupport.findFields(ReflectionSupportTests.class, allFields, null));
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

	@Test
	void findMethodsPreconditions() {
		assertPreconditionViolationException("Class",
			() -> ReflectionSupport.findMethods(null, allMethods, HierarchyTraversalMode.BOTTOM_UP));
		assertPreconditionViolationException("Class",
			() -> ReflectionSupport.findMethods(null, allMethods, HierarchyTraversalMode.TOP_DOWN));
		assertPreconditionViolationException("Predicate",
			() -> ReflectionSupport.findMethods(ReflectionSupportTests.class, null, HierarchyTraversalMode.BOTTOM_UP));
		assertPreconditionViolationException("Predicate",
			() -> ReflectionSupport.findMethods(ReflectionSupportTests.class, null, HierarchyTraversalMode.TOP_DOWN));
		assertPreconditionViolationException("HierarchyTraversalMode",
			() -> ReflectionSupport.findMethods(ReflectionSupportTests.class, allMethods, null));
	}

}
