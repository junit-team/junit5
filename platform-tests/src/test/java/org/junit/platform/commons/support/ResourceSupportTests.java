/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.support.PreconditionAssertions.assertPreconditionViolationException;
import static org.junit.platform.commons.support.PreconditionAssertions.assertPreconditionViolationExceptionForString;
import static org.junit.platform.commons.util.ClassLoaderUtils.getDefaultClassLoader;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.io.ResourceFilter;
import org.junit.platform.commons.util.ReflectionUtils;

class ResourceSupportTests {

	private static final ResourceFilter allResources = ResourceFilter.of(__ -> true);

	/**
	 * @since 1.12
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void tryToGetResourcesPreconditions() {
		assertPreconditionViolationExceptionForString("Resource name", () -> ResourceSupport.tryToGetResources(null));
		assertPreconditionViolationExceptionForString("Resource name", () -> ResourceSupport.tryToGetResources(""));
		assertPreconditionViolationException("Class loader",
			() -> ResourceSupport.tryToGetResources("default-package.resource", null));
		assertPreconditionViolationException("Class loader",
			() -> ResourceSupport.tryToGetResources("default-package.resource", null));
	}

	/**
	 * @since 1.12
	 */
	@Test
	void tryToGetResources() {
		assertEquals(ReflectionUtils.tryToGetResources("default-package.resource").toOptional(),
			ResourceSupport.tryToGetResources("default-package.resource").toOptional());
		assertEquals(
			ReflectionUtils.tryToGetResources("default-package.resource", getDefaultClassLoader()).toOptional(), //
			ResourceSupport.tryToGetResources("default-package.resource", getDefaultClassLoader()).toOptional());
	}

	/**
	 * @since 1.11
	 */
	@TestFactory
	List<DynamicTest> findAllResourcesInClasspathRootDelegates() throws Throwable {
		List<DynamicTest> tests = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		paths.add(Path.of(".").toRealPath());
		paths.addAll(ReflectionUtils.getAllClasspathRootDirectories());
		for (var path : paths) {
			var root = path.toUri();
			tests.add(DynamicTest.dynamicTest(createDisplayName(root),
				() -> assertThat(ReflectionUtils.findAllResourcesInClasspathRoot(root, allResources)) //
						.containsExactlyElementsOf(
							ResourceSupport.findAllResourcesInClasspathRoot(root, allResources))));
		}
		return tests;
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void findAllResourcesInClasspathRootPreconditions() {
		var path = Path.of(".").toUri();
		assertPreconditionViolationException("root",
			() -> ResourceSupport.findAllResourcesInClasspathRoot(null, allResources));
		assertPreconditionViolationException("resourceFilter",
			() -> ResourceSupport.findAllResourcesInClasspathRoot(path, null));
	}

	/**
	 * @since 1.11
	 */
	@TestFactory
	List<DynamicTest> streamAllResourcesInClasspathRootDelegates() throws Throwable {
		List<DynamicTest> tests = new ArrayList<>();
		List<Path> paths = new ArrayList<>();
		paths.add(Path.of(".").toRealPath());
		paths.addAll(ReflectionUtils.getAllClasspathRootDirectories());
		for (var path : paths) {
			var root = path.toUri();
			tests.add(DynamicTest.dynamicTest(createDisplayName(root),
				() -> assertThat(ReflectionUtils.streamAllResourcesInClasspathRoot(root, allResources)) //
						.containsExactlyElementsOf(
							ResourceSupport.streamAllResourcesInClasspathRoot(root, allResources).toList())));
		}
		return tests;
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void streamAllResourcesInClasspathRootPreconditions() {
		var path = Path.of(".").toUri();
		assertPreconditionViolationException("root",
			() -> ResourceSupport.streamAllResourcesInClasspathRoot(null, allResources));
		assertPreconditionViolationException("resourceFilter",
			() -> ResourceSupport.streamAllResourcesInClasspathRoot(path, null));
	}

	/**
	 * @since 1.11
	 */
	@Test
	void findAllResourcesInPackageDelegates() {
		assertNotEquals(0, ResourceSupport.findAllResourcesInPackage("org.junit", allResources).size());

		assertEquals(ReflectionUtils.findAllResourcesInPackage("org.junit", allResources),
			ResourceSupport.findAllResourcesInPackage("org.junit", allResources));
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void findAllResourcesInPackagePreconditions() {
		assertPreconditionViolationExceptionForString("basePackageName",
			() -> ResourceSupport.findAllResourcesInPackage(null, allResources));
		assertPreconditionViolationException("resourceFilter",
			() -> ResourceSupport.findAllResourcesInPackage("org.junit", null));
	}

	/**
	 * @since 1.11
	 */
	@Test
	void streamAllResourcesInPackageDelegates() {
		assertNotEquals(0, ResourceSupport.streamAllResourcesInPackage("org.junit", allResources).count());

		assertEquals(ReflectionUtils.streamAllResourcesInPackage("org.junit", allResources).toList(),
			ResourceSupport.streamAllResourcesInPackage("org.junit", allResources).toList());
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void streamAllResourcesInPackagePreconditions() {
		assertPreconditionViolationExceptionForString("basePackageName",
			() -> ResourceSupport.streamAllResourcesInPackage(null, allResources));
		assertPreconditionViolationException("resourceFilter",
			() -> ResourceSupport.streamAllResourcesInPackage("org.junit", null));
	}

	/**
	 * @since 1.11
	 */
	@Test
	void findAllResourcesInModuleDelegates() {
		assertEquals(ReflectionUtils.findAllResourcesInModule("org.junit.platform.commons", allResources),
			ResourceSupport.findAllResourcesInModule("org.junit.platform.commons", allResources));
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void findAllResourcesInModulePreconditions() {
		var exception = assertThrows(PreconditionViolationException.class,
			() -> ResourceSupport.findAllResourcesInModule(null, allResources));
		assertEquals("Module name must not be null or empty", exception.getMessage());
		assertPreconditionViolationException("Resource filter",
			() -> ResourceSupport.findAllResourcesInModule("org.junit.platform.commons", null));
	}

	/**
	 * @since 1.11
	 */
	@Test
	void streamAllResourcesInModuleDelegates() {
		assertEquals(ReflectionUtils.streamAllResourcesInModule("org.junit.platform.commons", allResources).toList(),
			ResourceSupport.streamAllResourcesInModule("org.junit.platform.commons", allResources).toList());
	}

	/**
	 * @since 1.11
	 */
	@SuppressWarnings("DataFlowIssue")
	@Test
	void streamAllResourcesInModulePreconditions() {
		var exception = assertThrows(PreconditionViolationException.class,
			() -> ResourceSupport.streamAllResourcesInModule(null, allResources));
		assertEquals("Module name must not be null or empty", exception.getMessage());
		assertPreconditionViolationException("Resource filter",
			() -> ResourceSupport.streamAllResourcesInModule("org.junit.platform.commons", null));
	}

	private static String createDisplayName(URI root) {
		var displayName = root.getPath();
		if (displayName.length() > 42) {
			displayName = "..." + displayName.substring(displayName.length() - 42);
		}
		return displayName;
	}

}
