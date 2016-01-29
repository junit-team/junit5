/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.testpackage.TestpackagePlaceholder;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class ClasspathScannerTests {

	private ClasspathScanner classpathScanner;

	@BeforeEach
	public void init() {
		classpathScanner = new ClasspathScanner(ReflectionUtils::getDefaultClassLoader, ReflectionUtils::loadClass);
	}

	@Test
	public void findAllClassesInThisPackage() throws Exception {
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.gen5.commons", clazz -> true);
		assertThat(classes.size()).isGreaterThanOrEqualTo(20);
		assertTrue(classes.contains(NestedClassToBeFound.class));
		assertTrue(classes.contains(MemberClassToBeFound.class));
	}

	@Test
	public void findAllClassesInThisPackageWithFilter() throws Exception {
		Predicate<Class<?>> thisClassOnly = clazz -> clazz == ClasspathScannerTests.class;
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.gen5.commons", thisClassOnly);
		assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	public void isPackage() throws Exception {
		assertTrue(classpathScanner.isPackage("org.junit.gen5.commons"));
		assertFalse(classpathScanner.isPackage("org.junit.gen5.commons.util.emptypackage"));
		assertFalse(classpathScanner.isPackage("org.doesnotexist"));
	}

	@Test
	public void isPackage_returnsFalseForExceptions() throws Exception {
		classpathScanner = new ClasspathScanner(() -> new ClassLoader() {
			@Override
			public Enumeration<URL> getResources(String name) throws IOException {
				throw new IOException("Intended to fail!");
			}
		}, ReflectionUtils::loadClass);
		assertFalse(classpathScanner.isPackage("org.junit.gen5"));
	}

	@Test
	public void findAllClassesInClasspathRoot() throws Exception {
		Predicate<Class<?>> thisClassOnly = clazz -> clazz == ClasspathScannerTests.class;
		File root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, thisClassOnly);
		assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	public void findAllClassesInClasspathRootWithFilter() throws Exception {
		File root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, clazz -> true);

		assertThat(classes.size()).isGreaterThanOrEqualTo(20);
		assertTrue(classes.contains(ClasspathScannerTests.class));
	}

	@Test
	void givenAPackageWithoutSubpackages_emptyListIsReturned() throws Exception {
		List<String> subpackages = classpathScanner.scanForPackagesInPackage(
			"org.junit.gen5.commons.util.emptypackage");
		assertThat(subpackages).isEmpty();
	}

	@Test
	void givenAPackageWithOneSubpackage_subpackageIsReturned() throws Exception {
		List<String> subpackages = classpathScanner.scanForPackagesInPackage("org.junit.gen5.commons.util.testpackage");

		// @formatter:off
		assertThat(subpackages)
                .containsOnly("org.junit.gen5.commons.util.testpackage.subpackage")
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageWithManySubpackage_allSubpackagesAreReturned() throws Exception {
		List<String> subpackages = classpathScanner.scanForPackagesInPackage("org.junit.gen5");

		// @formatter:off
        assertThat(subpackages)
                .contains(
                        "org.junit.gen5.api",
                        "org.junit.gen5.commons",
                        "org.junit.gen5.console",
                        "org.junit.gen5.engine",
                        "org.junit.gen5.launcher")
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageWithOneClass_classIsReturned() throws Exception {
		List<Class<?>> classes = classpathScanner.scanForClassesInPackageOnly("org.junit.gen5.commons.util.testpackage",
			aClass -> true);

		// @formatter:off
        assertThat(classes)
                .containsOnly(TestpackagePlaceholder.class)
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	private File getTestClasspathRoot() throws Exception {
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		return new File(location.toURI());
	}

	class MemberClassToBeFound {
	}

	static class NestedClassToBeFound {
	}

}
