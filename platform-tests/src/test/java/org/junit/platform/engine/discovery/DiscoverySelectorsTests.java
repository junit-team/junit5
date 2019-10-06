/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link DiscoverySelectors}.
 *
 * @since 1.0
 */
class DiscoverySelectorsTests {

	@Test
	void selectUriByName() {
		assertPreconditionViolated(() -> selectUri((String) null));
		assertPreconditionViolated(() -> selectUri("   "));
		assertPreconditionViolated(() -> selectUri("foo:"));

		String uri = "https://junit.org";

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri().toString());
	}

	@Test
	void selectUriByURI() throws Exception {
		assertPreconditionViolated(() -> selectUri((URI) null));
		assertPreconditionViolated(() -> selectUri("   "));

		URI uri = new URI("https://junit.org");

		UriSelector selector = selectUri(uri);
		assertEquals(uri, selector.getUri());
	}

	@Test
	void selectFileByName() {
		assertPreconditionViolated(() -> selectFile((String) null));
		assertPreconditionViolated(() -> selectFile("   "));

		String path = "src/test/resources/do_not_delete_me.txt";

		FileSelector selector = selectFile(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getFile());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectFileByFileReference() throws Exception {
		assertPreconditionViolated(() -> selectFile((File) null));
		assertPreconditionViolated(() -> selectFile(new File("bogus/nonexistent.txt")));

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
	void selectDirectoryByName() {
		assertPreconditionViolated(() -> selectDirectory((String) null));
		assertPreconditionViolated(() -> selectDirectory("   "));

		String path = "src/test/resources";

		DirectorySelector selector = selectDirectory(path);
		assertEquals(path, selector.getRawPath());
		assertEquals(new File(path), selector.getDirectory());
		assertEquals(Paths.get(path), selector.getPath());
	}

	@Test
	void selectDirectoryByFileReference() throws Exception {
		assertPreconditionViolated(() -> selectDirectory((File) null));
		assertPreconditionViolated(() -> selectDirectory(new File("bogus/nonexistent")));

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
		assertPreconditionViolated(() -> selectClasspathResource(null));
		assertPreconditionViolated(() -> selectClasspathResource(""));
		assertPreconditionViolated(() -> selectClasspathResource("    "));
		assertPreconditionViolated(() -> selectClasspathResource("\t"));

		// with unnecessary "/" prefix
		ClasspathResourceSelector selector = selectClasspathResource("/foo/bar/spec.xml");
		assertEquals("foo/bar/spec.xml", selector.getClasspathResourceName());

		// standard use case
		selector = selectClasspathResource("A/B/C/spec.json");
		assertEquals("A/B/C/spec.json", selector.getClasspathResourceName());
	}

	@Test
	void selectModuleByName() {
		ModuleSelector selector = selectModule("java.base");
		assertEquals("java.base", selector.getModuleName());
	}

	@Test
	void selectModuleByNamePreconditions() {
		assertPreconditionViolated(() -> selectModule(null));
		assertPreconditionViolated(() -> selectModule(""));
		assertPreconditionViolated(() -> selectModule("   "));
	}

	@Test
	void selectModulesByNames() {
		List<ModuleSelector> selectors = selectModules(new HashSet<>(Arrays.asList("a", "b")));
		List<String> names = selectors.stream().map(ModuleSelector::getModuleName).collect(Collectors.toList());
		assertThat(names).containsExactlyInAnyOrder("b", "a");
	}

	@Test
	void selectModulesByNamesPreconditions() {
		assertPreconditionViolated(() -> selectModules(null));
		assertPreconditionViolated(() -> selectModules(new HashSet<>(Arrays.asList("a", " "))));
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
	void selectMethodByClassNameAndMethodNamePreconditions() {
		assertPreconditionViolated(() -> selectMethod("TestClass", null));
		assertPreconditionViolated(() -> selectMethod("TestClass", ""));
		assertPreconditionViolated(() -> selectMethod("TestClass", "  "));
		assertPreconditionViolated(() -> selectMethod((String) null, "method"));
		assertPreconditionViolated(() -> selectMethod("", "method"));
		assertPreconditionViolated(() -> selectMethod("   ", "method"));
	}

	@Test
	void selectMethodByClassNameMethodNameAndMethodParameterTypesPreconditions() {
		assertPreconditionViolated(() -> selectMethod("TestClass", null, "int"));
		assertPreconditionViolated(() -> selectMethod("TestClass", "", "int"));
		assertPreconditionViolated(() -> selectMethod("TestClass", "  ", "int"));
		assertPreconditionViolated(() -> selectMethod((String) null, "method", "int"));
		assertPreconditionViolated(() -> selectMethod("", "method", "int"));
		assertPreconditionViolated(() -> selectMethod("   ", "method", "int"));
		assertPreconditionViolated(() -> selectMethod("TestClass", "method", null));
	}

	@Test
	void selectMethodByClassAndMethodNamePreconditions() {
		assertPreconditionViolated(() -> selectMethod(getClass(), (String) null));
		assertPreconditionViolated(() -> selectMethod(getClass(), ""));
		assertPreconditionViolated(() -> selectMethod(getClass(), "  "));
		assertPreconditionViolated(() -> selectMethod((Class<?>) null, "method"));
		assertPreconditionViolated(() -> selectMethod("", "method"));
		assertPreconditionViolated(() -> selectMethod("   ", "method"));
	}

	@Test
	void selectMethodByClassMethodNameAndMethodParameterTypesPreconditions() {
		assertPreconditionViolated(() -> selectMethod((Class<?>) null, "method", "int"));
		assertPreconditionViolated(() -> selectMethod(getClass(), null, "int"));
		assertPreconditionViolated(() -> selectMethod(getClass(), "", "int"));
		assertPreconditionViolated(() -> selectMethod(getClass(), "  ", "int"));
		assertPreconditionViolated(() -> selectMethod(getClass(), "method", null));
	}

	@Test
	void selectMethodByClassAndMethodPreconditions() {
		Method method = getClass().getDeclaredMethods()[0];
		assertPreconditionViolated(() -> selectMethod(null, method));
		assertPreconditionViolated(() -> selectMethod(getClass(), (Method) null));
	}

	@ParameterizedTest(name = "FQMN: ''{0}''")
	@MethodSource("invalidFullyQualifiedMethodNames")
	void selectMethodByFullyQualifiedNamePreconditions(String fqmn, String message) {
		Exception exception = assertThrows(PreconditionViolationException.class, () -> selectMethod(fqmn));
		assertThat(exception).hasMessageContaining(message);
	}

	static Stream<Arguments> invalidFullyQualifiedMethodNames() {
		// @formatter:off
		return Stream.of(
			arguments(null, "must not be null or blank"),
			arguments("", "must not be null or blank"),
			arguments("   ", "must not be null or blank"),
			arguments("com.example", "not a valid fully qualified method name"),
			arguments("com.example.Foo", "not a valid fully qualified method name"),
			arguments("method", "not a valid fully qualified method name"),
			arguments("#method", "not a valid fully qualified method name"),
			arguments("#method()", "not a valid fully qualified method name"),
			arguments("#method(int)", "not a valid fully qualified method name"),
			arguments("java.lang.String#", "not a valid fully qualified method name")
		);
		// @formatter:on
	}

	@Test
	void selectMethodByFullyQualifiedName() throws Exception {
		Class<?> clazz = getClass();
		Method method = clazz.getDeclaredMethod("myTest");
		assertSelectMethodByFullyQualifiedName(clazz, method);
	}

	@Test
	void selectMethodByFullyQualifiedNameForDefaultMethodInInterface() throws Exception {
		Class<?> clazz = TestCaseWithDefaultMethod.class;
		Method method = clazz.getMethod("myTest");
		assertSelectMethodByFullyQualifiedName(clazz, method);
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int.class, "int");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int", "int");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String.class, String.class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String", String.class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[].class, int[].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[]", "int[]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String[].class, String[].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithObjectArrayParameterUsingSourceCodeSyntax() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String[]", "java.lang.String[]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[][].class, int[][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
			throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[][]", "int[][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, String[][].class, String[][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameterUsingSourceCodeSyntax()
			throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String[][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.String[][]", "java.lang.String[][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", int[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, int[][][][][].class, int[][][][][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
			throws Exception {

		Method method = getClass().getDeclaredMethod("myTest", int[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "int[][][][][]", "int[][][][][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameter() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", Double[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, Double[][][][][].class,
			Double[][][][][].class.getName());
	}

	@Test
	void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameterUsingSourceCodeSyntax()
			throws Exception {

		Method method = getClass().getDeclaredMethod("myTest", Double[][][][][].class);
		assertSelectMethodByFullyQualifiedName(getClass(), method, "java.lang.Double[][][][][]",
			"java.lang.Double[][][][][]");
	}

	@Test
	void selectMethodByFullyQualifiedNameEndingInOpeningParenthesis() {
		String className = "org.example.MyClass";
		// The following bizarre method name is not permissible in Java source
		// code; however, it's permitted by the JVM -- for example, in Groovy
		// or Kotlin source code using back ticks.
		String methodName = ")--(";
		String fqmn = className + "#" + methodName;

		MethodSelector selector = selectMethod(fqmn);
		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	/**
	 * Inspired by Spock specifications.
	 */
	@Test
	void selectMethodByFullyQualifiedNameContainingHashtags() {
		String className = "org.example.CalculatorSpec";
		String methodName = "#a plus #b equals #c";
		String fqmn = className + "#" + methodName;

		MethodSelector selector = selectMethod(fqmn);
		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	/**
	 * Inspired by Spock specifications.
	 */
	@Test
	void selectMethodByFullyQualifiedNameContainingHashtagsAndWithParameterList() {
		String className = "org.example.CalculatorSpec";
		String methodName = "#a plus #b equals #c";
		String methodParameters = "int, int, int";
		String fqmn = String.format("%s#%s(%s)", className, methodName, methodParameters);

		MethodSelector selector = selectMethod(fqmn);
		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals(methodParameters, selector.getMethodParameterTypes());
	}

	/**
	 * Inspired by Kotlin tests.
	 */
	@Test
	void selectMethodByFullyQualifiedNameContainingParentheses() {
		String className = "org.example.KotlinTestCase";
		String methodName = "🦆 ~|~test with a really, (really) terrible name & that needs to be changed!~|~";
		String fqmn = className + "#" + methodName;

		MethodSelector selector = selectMethod(fqmn);

		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	/**
	 * Inspired by Kotlin tests.
	 */
	@Test
	void selectMethodByFullyQualifiedNameEndingWithParentheses() {
		String className = "org.example.KotlinTestCase";
		String methodName = "test name ends with parentheses()";
		String fqmn = className + "#" + methodName + "()";

		MethodSelector selector = selectMethod(fqmn);

		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	/**
	 * Inspired by Kotlin tests.
	 */
	@Test
	void selectMethodByFullyQualifiedNameEndingWithParenthesesAndWithParameterList() {
		String className = "org.example.KotlinTestCase";
		String methodName = "test name ends with parentheses()";
		String methodParameters = "int, int, int";
		String fqmn = String.format("%s#%s(%s)", className, methodName, methodParameters);

		MethodSelector selector = selectMethod(fqmn);

		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals(methodParameters, selector.getMethodParameterTypes());
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method) {
		MethodSelector selector = selectMethod(fqmn(clazz, method.getName()));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method, Class<?> parameterType,
			String expectedParameterTypes) {

		MethodSelector selector = selectMethod(fqmn(parameterType));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertEquals(expectedParameterTypes, selector.getMethodParameterTypes());
	}

	private void assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method, String parameterName,
			String expectedParameterTypes) {

		MethodSelector selector = selectMethod(fqmnWithParamNames(parameterName));
		assertEquals(method, selector.getJavaMethod());
		assertEquals(clazz, selector.getJavaClass());
		assertEquals(clazz.getName(), selector.getClassName());
		assertEquals(method.getName(), selector.getMethodName());
		assertEquals(expectedParameterTypes, selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodName() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest");

		MethodSelector selector = selectMethod(getClass(), "myTest");
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodByClassAndMethodNameWithParameterTypes() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);

		MethodSelector selector = selectMethod(getClass(), "myTest", String.class.getName());
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals(method, selector.getJavaMethod());
		assertEquals("myTest", selector.getMethodName());
		assertEquals(String.class.getName(), selector.getMethodParameterTypes());
	}

	@Test
	void selectMethodWithParametersByMethodReference() throws Exception {
		Method method = getClass().getDeclaredMethod("myTest", String.class);

		MethodSelector selector = selectMethod(getClass(), method);
		assertEquals(method, selector.getJavaMethod());
		assertEquals(getClass(), selector.getJavaClass());
		assertEquals(getClass().getName(), selector.getClassName());
		assertEquals("myTest", selector.getMethodName());
		assertEquals(String.class.getName(), selector.getMethodParameterTypes());
	}

	@Test
	void selectClassByNameForSpockSpec() {
		String className = "org.example.CalculatorSpec";
		ClassSelector selector = selectClass(className);
		assertEquals(className, selector.getClassName());
	}

	@Test
	void selectMethodByClassAndNameForSpockSpec() {
		String className = "org.example.CalculatorSpec";
		String methodName = "#a plus #b equals #c";

		MethodSelector selector = selectMethod(className, methodName);
		assertEquals(className, selector.getClassName());
		assertEquals(methodName, selector.getMethodName());
		assertEquals("", selector.getMethodParameterTypes());
	}

	@Test
	void selectClasspathRootsWithNonExistingDirectory() {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some", "local", "path")));

		assertThat(selectors).isEmpty();
	}

	@Test
	void selectClasspathRootsWithNonExistingJarFile() {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(Paths.get("some.jar")));

		assertThat(selectors).isEmpty();
	}

	@Test
	void selectClasspathRootsWithExistingDirectory(@TempDir Path tempDir) {
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(tempDir));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(tempDir.toUri());
	}

	@Test
	void selectClasspathRootsWithExistingJarFile() throws Exception {
		URI jarUri = getClass().getResource("/jartest.jar").toURI();
		Path jarFile = Paths.get(jarUri);

		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(jarFile));

		assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(jarUri);
	}

	@Nested
	class NestedClassAndMethodSelectors {

		private final String enclosingClassName = getClass().getName() + "$ClassWithNestedInnerClass";
		private final String nestedClassName = getClass().getName() + "$AbstractClassWithNestedInnerClass$NestedClass";
		private final String doubleNestedClassName = nestedClassName + "$DoubleNestedClass";
		private final String methodName = "nestedTest";

		@Test
		void selectNestedClassByClassNames() {
			NestedClassSelector selector = selectNestedClass(List.of(enclosingClassName), nestedClassName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(AbstractClassWithNestedInnerClass.NestedClass.class);
		}

		@Test
		void selectDoubleNestedClassByClassNames() {
			NestedClassSelector selector = selectNestedClass(List.of(enclosingClassName, nestedClassName),
				doubleNestedClassName);

			assertThat(selector.getEnclosingClasses()).containsExactly(ClassWithNestedInnerClass.class,
				AbstractClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(
				AbstractClassWithNestedInnerClass.NestedClass.DoubleNestedClass.class);
		}

		@Test
		void selectNestedClassPreconditions() {
			assertPreconditionViolated(() -> selectNestedClass(null, "ClassName"));
			assertPreconditionViolated(() -> selectNestedClass(emptyList(), "ClassName"));
			assertPreconditionViolated(() -> selectNestedClass(List.of("ClassName"), null));
			assertPreconditionViolated(() -> selectNestedClass(List.of("ClassName"), ""));
			assertPreconditionViolated(() -> selectNestedClass(List.of("ClassName"), " "));
		}

		@Test
		void selectNestedMethodByEnclosingClassNamesAndMethodName() throws Exception {
			NestedMethodSelector selector = selectNestedMethod(List.of(enclosingClassName), nestedClassName,
				methodName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(AbstractClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(selector.getNestedClass().getDeclaredMethod(methodName));
		}

		@Test
		void selectNestedMethodByEnclosingClassesAndMethodName() throws Exception {
			NestedMethodSelector selector = selectNestedMethod(List.of(ClassWithNestedInnerClass.class),
				AbstractClassWithNestedInnerClass.NestedClass.class, methodName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(AbstractClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(selector.getNestedClass().getDeclaredMethod(methodName));
		}

		@Test
		void selectNestedMethodByEnclosingClassNamesAndMethodNameWithParameterTypes() throws Exception {
			NestedMethodSelector selector = selectNestedMethod(List.of(enclosingClassName), nestedClassName, methodName,
				String.class.getName());

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(AbstractClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(
				selector.getNestedClass().getDeclaredMethod(methodName, String.class));
		}

		@Test
		void selectDoubleNestedMethodByEnclosingClassNamesAndMethodName() throws Exception {
			NestedMethodSelector selector = selectNestedMethod(List.of(enclosingClassName, nestedClassName),
				doubleNestedClassName, "doubleNestedTest");

			assertThat(selector.getEnclosingClasses()).containsExactly(ClassWithNestedInnerClass.class,
				AbstractClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(
				AbstractClassWithNestedInnerClass.NestedClass.DoubleNestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(selector.getNestedClass().getDeclaredMethod("doubleNestedTest"));
		}

		@Test
		void selectNestedMethodPreconditions() {
			assertPreconditionViolated(() -> selectNestedMethod(null, "ClassName", "methodName"));
			assertPreconditionViolated(() -> selectNestedMethod(null, "ClassName", "methodName", "int"));
			assertPreconditionViolated(() -> selectNestedMethod(emptyList(), "ClassName", "methodName"));
			assertPreconditionViolated(() -> selectNestedMethod(emptyList(), "ClassName", "methodName", "int"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), null, "methodName"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), null, "methodName", "int"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), " ", "methodName"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), " ", "methodName", "int"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), "ClassName", null));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), "ClassName", null, "int"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), "ClassName", " "));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), "ClassName", " ", "int"));
			assertPreconditionViolated(() -> selectNestedMethod(List.of("ClassName"), "ClassName", "methodName", null));
		}

		abstract class AbstractClassWithNestedInnerClass {

			@Nested
			class NestedClass {

				@Test
				void nestedTest() {
				}

				@Test
				void nestedTest(String parameter) {
				}

				@Nested
				class DoubleNestedClass {

					@Test
					void doubleNestedTest() {
					}

				}

			}

		}

		class ClassWithNestedInnerClass extends AbstractClassWithNestedInnerClass {
		}

		class OtherClassWithNestedInnerClass extends AbstractClassWithNestedInnerClass {
		}

	}

	// -------------------------------------------------------------------------

	private void assertPreconditionViolated(Executable precondition) {
		assertThrows(PreconditionViolationException.class, precondition);
	}

	private static String fqmn(Class<?>... params) {
		return fqmn(DiscoverySelectorsTests.class, "myTest", params);
	}

	private static String fqmn(Class<?> clazz, String methodName, Class<?>... params) {
		return ReflectionUtils.getFullyQualifiedMethodName(clazz, methodName, params);
	}

	private static String fqmnWithParamNames(String... params) {
		return String.format("%s#%s(%s)", DiscoverySelectorsTests.class.getName(), "myTest", join(", ", params));
	}
	interface TestInterface {

		@Test
		default void myTest() {
		}

	}
	private static class TestCaseWithDefaultMethod implements TestInterface {

	}

	void myTest() {
	}

	void myTest(int num) {
	}

	void myTest(int[] nums) {
	}

	void myTest(int[][] grid) {
	}

	void myTest(int[][][][][] grid) {
	}

	void myTest(String info) {
	}

	void myTest(String[] info) {
	}

	void myTest(String[][] info) {
	}

	void myTest(Double[][][][][] data) {
	}

}
