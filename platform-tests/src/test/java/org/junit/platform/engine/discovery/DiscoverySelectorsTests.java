/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNames;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.DiscoverySelector;

/**
 * Unit tests for {@link DiscoverySelectors}.
 *
 * @since 1.0
 */
public class DiscoverySelectorsTests {

	private static final Method fullyQualifiedMethod = fullyQualifiedMethod();
	private static final Method fullyQualifiedMethodWithParameters = fullyQualifiedMethodWithParameters();
	private static final Method fullyQualifiedDefaultMethod = fullyQualifiedDefaultMethod();

	private static final String fullyQualifiedMethodName = fullyQualifiedMethodName();
	private static final String fullyQualifiedMethodNameWithParameters = fullyQualifiedMethodNameWithParameters();
	private static final String fullyQualifiedDefaultMethodName = fullyQualifiedDefaultMethodName();

	@Test
	void selectUriByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectUri((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectUri("   "));
		assertThrows(PreconditionViolationException.class, () -> selectUri("foo:"));

		String uri = "http://junit.org";

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri().toString());
	}

	@Test
	void selectUriByURI() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectUri((URI) null));
		assertThrows(PreconditionViolationException.class, () -> selectUri("   "));

		URI uri = new URI("http://junit.org");

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri());
	}

	@Test
	void selectFileByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectFile((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectFile("   "));

		String path = "src/test/resources/do_not_delete_me.txt";

		FileSelector selector = selectFile(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getFile());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectFileByFileReference() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectFile((File) null));
		assertThrows(PreconditionViolationException.class, () -> selectFile(new File("bogus/nonexistent.txt")));

		File currentDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", currentDir.getName());
		File file = new File(relativeDir, "src/test/resources/do_not_delete_me.txt");
		String path = file.getCanonicalFile().getPath();

		FileSelector selector = selectFile(file);
		assertEquals(path, selector.getRawPath());
		assertEquals(file.getCanonicalFile(), selector.getFile());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectDirectoryByName() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectDirectory((String) null));
		assertThrows(PreconditionViolationException.class, () -> selectDirectory("   "));

		String path = "src/test/resources";

		DirectorySelector selector = selectDirectory(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getDirectory());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectDirectoryByFileReference() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> selectDirectory((File) null));
		assertThrows(PreconditionViolationException.class, () -> selectDirectory(new File("bogus/nonexistent")));

		File currentDir = new File(".").getCanonicalFile();
		File relativeDir = new File("..", currentDir.getName());
		File directory = new File(relativeDir, "src/test/resources");
		String path = directory.getCanonicalFile().getPath();

		DirectorySelector selector = selectDirectory(directory);
		assertEquals(path, selector.getRawPath());
		assertEquals(directory.getCanonicalFile(), selector.getDirectory());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectClasspathResources() {
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource(null));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource(""));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource("    "));
		assertThrows(PreconditionViolationException.class, () -> selectClasspathResource("\t"));

		// with unnecessary "/" prefix
		ClasspathResourceSelector selector = selectClasspathResource("/foo/bar/spec.xml");
		assertEquals("foo/bar/spec.xml", selector.getClasspathResourceName());

		// standard use case
		selector = selectClasspathResource("A/B/C/spec.json");
		assertEquals("A/B/C/spec.json", selector.getClasspathResourceName());
	}

	@Test
	void selectPackageByName() {
		JavaPackageSelector selector = selectJavaPackage(getClass().getPackage().getName());
		assertEquals(getClass().getPackage().getName(), selector.getPackageName());
	}

	@Test
	void selectClassByName() {
		JavaClassSelector selector = selectJavaClass(getClass().getName());
		assertEquals(getClass(), selector.getJavaClass());
	}

	@Test
	void selectMethodByFullyQualifiedName() {
		JavaMethodSelector selector = selectJavaMethod(fullyQualifiedMethodName);
		assertEquals(fullyQualifiedMethod, selector.getJavaMethod());
		assertEquals(DiscoverySelectorsTests.class, selector.getJavaClass());
		assertEquals(DiscoverySelectorsTests.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithParameters() {
		JavaMethodSelector selector = selectJavaMethod(fullyQualifiedMethodNameWithParameters);
		assertEquals(fullyQualifiedMethodWithParameters, selector.getJavaMethod());
		assertEquals(DiscoverySelectorsTests.class, selector.getJavaClass());
		assertEquals(DiscoverySelectorsTests.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("java.lang.String", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForDefaultMethodInInterface() {
		JavaMethodSelector selector = selectJavaMethod(fullyQualifiedDefaultMethodName);
		assertEquals(fullyQualifiedDefaultMethod, selector.getJavaMethod());
		assertEquals(TestCaseWithDefaultMethod.class, selector.getJavaClass());
		assertEquals(TestCaseWithDefaultMethod.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodName() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest");
		JavaMethodSelector selector = selectJavaMethod(getClass(), "myTest");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodNameWithParameterTypes() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		JavaMethodSelector selector = selectJavaMethod(getClass(), "myTest", "java.lang.String");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("java.lang.String", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodWithParametersByMethodReference() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		JavaMethodSelector selector = selectJavaMethod(getClass(), method);
		assertEquals(method, selector.getJavaMethod());
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectClassByNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		JavaClassSelector selector = selectJavaClass(spockClassName);
		assertEquals(spockClassName, selector.getClassName());
	}

	@Test
	void selectMethodByClassAndNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";

		JavaMethodSelector selector = selectJavaMethod(spockClassName, spockMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";
		String spockFullyQualifiedMethodName = spockClassName + "#" + spockMethodName;

		JavaMethodSelector selector = selectJavaMethod(spockFullyQualifiedMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForSpockSpecWithParameters() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";
		String spockMethodParameters = "int, int, int";
		String spockFullyQualifiedMethodName = spockClassName + "#" + spockMethodName + "(" + spockMethodParameters
				+ ")";

		JavaMethodSelector selector = selectJavaMethod(spockFullyQualifiedMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertEquals(spockMethodParameters, selector.getMethodParameterTypes());
	}

	@Test
	@SuppressWarnings("deprecation")
	void selectNamesWithPackageName() {
		DiscoverySelector selector = getOnlyElement(selectNames(singleton("org.junit.platform")));
		assertEquals(JavaPackageSelector.class, selector.getClass());
	}

	@Test
	@SuppressWarnings("deprecation")
	void selectNameWithClassName() {
		DiscoverySelector selector = getOnlyElement(selectNames(singleton(getClass().getName())));
		assertEquals(JavaClassSelector.class, selector.getClass());
	}

	@Test
	@SuppressWarnings("deprecation")
	void selectNameWithMethodName() {
		DiscoverySelector selector = getOnlyElement(selectNames(singleton(fullyQualifiedMethodName)));
		assertEquals(JavaMethodSelector.class, selector.getClass());
	}

	private static String fullyQualifiedMethodName() {
		return String.format("%s#%s()", DiscoverySelectorsTests.class.getName(), fullyQualifiedMethod().getName());
	}

	private static String fullyQualifiedMethodNameWithParameters() {
		return String.format("%s#%s(%s)", DiscoverySelectorsTests.class.getName(), fullyQualifiedMethod().getName(),
			String.class.getName());
	}

	private static String fullyQualifiedDefaultMethodName() {
		return String.format("%s#%s()", TestCaseWithDefaultMethod.class.getName(), fullyQualifiedMethod().getName());
	}

	private static Method fullyQualifiedMethod() {
		try {
			return DiscoverySelectorsTests.class.getDeclaredMethod("myTest");
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Method fullyQualifiedMethodWithParameters() {
		try {
			return DiscoverySelectorsTests.class.getDeclaredMethod("myTest", String.class);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Method fullyQualifiedDefaultMethod() {
		try {
			return TestCaseWithDefaultMethod.class.getMethod("myTest");
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	interface TestInterface {

		@Test
		default void myTest() {
		}
	}

	static class TestCaseWithDefaultMethod implements TestInterface {
	}

	void myTest() {
	}

	void myTest(String info) {
	}

}
