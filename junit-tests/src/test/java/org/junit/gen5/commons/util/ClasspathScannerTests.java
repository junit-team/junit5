/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClasspathScannerTests {

	private ClasspathScanner classpathScanner;

	@Before
	public void init() {
		classpathScanner = new ClasspathScanner(ReflectionUtils::getDefaultClassLoader, ReflectionUtils::loadClass);
	}

	@Test
	public void findAllClassesInThisPackage() throws Exception {
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.gen5.commons", clazz -> true);
		Assert.assertTrue("Should be at least 20 classes", classes.size() >= 20);
		Assert.assertTrue(classes.contains(NestedClassToBeFound.class));
		Assert.assertTrue(classes.contains(MemberClassToBeFound.class));
	}

	@Test
	public void findAllClassesInThisPackageWithFilter() throws Exception {
		Predicate<Class<?>> thisClassOnly = clazz -> clazz == ClasspathScannerTests.class;
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.gen5.commons", thisClassOnly);
		Assert.assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	public void isPackage() throws Exception {
		Assert.assertTrue(classpathScanner.isPackage("org.junit.gen5.commons"));
		Assert.assertFalse(classpathScanner.isPackage("org.doesnotexist"));
	}

	@Test
	public void findAllClassesInClasspathRoot() throws Exception {
		Predicate<Class<?>> thisClassOnly = clazz -> clazz == ClasspathScannerTests.class;
		File root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, thisClassOnly);
		Assert.assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	public void findAllClassesInClasspathRootWithFilter() throws Exception {
		File root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, clazz -> true);

		Assert.assertTrue("Should be at least 20 classes", classes.size() >= 20);
		Assert.assertTrue(classes.contains(ClasspathScannerTests.class));
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
