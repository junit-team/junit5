/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

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
		PackageSelector selector = selectPackage(getClass().getPackage().getName());
		assertEquals(getClass().getPackage().getName(), selector.getPackageName());
	}

	@Test
	void selectClassByName() {
		ClassSelector selector = selectClass(getClass().getName());
		assertEquals(getClass(), selector.getJavaClass());
	}

	@Test
	void selectMethodByFullyQualifiedName() {
		MethodSelector selector = selectMethod(fullyQualifiedMethodName);
		assertEquals(fullyQualifiedMethod, selector.getJavaMethod());
		assertEquals(DiscoverySelectorsTests.class, selector.getJavaClass());
		assertEquals(DiscoverySelectorsTests.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithParameters() {
		MethodSelector selector = selectMethod(fullyQualifiedMethodNameWithParameters);
		assertEquals(fullyQualifiedMethodWithParameters, selector.getJavaMethod());
		assertEquals(DiscoverySelectorsTests.class, selector.getJavaClass());
		assertEquals(DiscoverySelectorsTests.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("java.lang.String", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForDefaultMethodInInterface() {
		MethodSelector selector = selectMethod(fullyQualifiedDefaultMethodName);
		assertEquals(fullyQualifiedDefaultMethod, selector.getJavaMethod());
		assertEquals(TestCaseWithDefaultMethod.class, selector.getJavaClass());
		assertEquals(TestCaseWithDefaultMethod.class.getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodName() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest");
		MethodSelector selector = selectMethod(getClass(), "myTest");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodNameWithParameterTypes() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		MethodSelector selector = selectMethod(getClass(), "myTest", "java.lang.String");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("java.lang.String", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodWithParametersByMethodReference() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		MethodSelector selector = selectMethod(getClass(), method);
		assertEquals(method, selector.getJavaMethod());
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectClassByNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		ClassSelector selector = selectClass(spockClassName);
		assertEquals(spockClassName, selector.getClassName());
	}

	@Test
	void selectMethodByClassAndNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";

		MethodSelector selector = selectMethod(spockClassName, spockMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertNull(selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByFullyQualifiedNameForSpockSpec() {
		String spockClassName = "org.example.CalculatorSpec";
		String spockMethodName = "#a plus #b equals #c";
		String spockFullyQualifiedMethodName = spockClassName + "#" + spockMethodName;

		MethodSelector selector = selectMethod(spockFullyQualifiedMethodName);
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

		MethodSelector selector = selectMethod(spockFullyQualifiedMethodName);
		assertEquals(spockClassName, selector.getClassName());
		assertEquals(spockMethodName, selector.getMethodName());
		assertEquals(spockMethodParameters, selector.getMethodParameterTypes());
	}

	@Test
	public void selectClasspathRootsWithNonExistingDirectory() throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some", "local", "path")));

		assertThat(selectors).isEmpty();
	}

	@Test
	public void selectClasspathRootsWithNonExistingJarFile() throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some.jar")));

		assertThat(selectors).isEmpty();
	}

	@Test
	@ExtendWith(TempDirectory.class)
	public void selectClasspathRootsWithExistingDirectory(@Root Path tempDir) throws Exception {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(tempDir));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(tempDir.toUri());
	}

	@Test
	public void selectClasspathRootsWithExistingJarFile() throws Exception {
		URI jarUri = getClass().getResource("/jartest.jar").toURI();
		Path jarFile = Paths.get(jarUri);

		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(jarFile));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(jarUri);
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
