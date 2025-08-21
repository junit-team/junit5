/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForMethod;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModule;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModules;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUri;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.commons.test.TestClassLoader;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.DiscoverySelectorIdentifier;

/**
 * Unit tests for {@link DiscoverySelectors}.
 *
 * @since 1.0
 */
class DiscoverySelectorsTests {

	@Nested
	class SelectUriTests {

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectUriByName() {
			assertViolatesPrecondition(() -> selectUri((String) null));
			assertViolatesPrecondition(() -> selectUri("   "));
			assertViolatesPrecondition(() -> selectUri("foo:"));

			var uri = "https://junit.org";

			var selector = selectUri(uri);
			assertEquals(uri, selector.getUri().toString());
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectUriByURI() {
			assertViolatesPrecondition(() -> selectUri((URI) null));
			assertViolatesPrecondition(() -> selectUri("   "));

			var uri = URI.create("https://junit.org");

			var selector = selectUri(uri);
			assertEquals(uri, selector.getUri());
		}

	}

	@Nested
	class SelectFileTests {

		@Test
		void parseUriSelector() {
			var selector = parseIdentifier(selectUri("https://junit.org"));
			assertThat(selector) //
					.asInstanceOf(type(UriSelector.class)) //
					.extracting(UriSelector::getUri) //
					.isEqualTo(URI.create("https://junit.org"));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectFileByName() {
			assertViolatesPrecondition(() -> selectFile((String) null));
			assertViolatesPrecondition(() -> selectFile("   "));

			var path = "src/test/resources/do_not_delete_me.txt";

			var selector = selectFile(path);
			assertEquals(path, selector.getRawPath());
			assertEquals(new File(path), selector.getFile());
			assertEquals(Path.of(path), selector.getPath());
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectFileByNameAndPosition() {
			var filePosition = FilePosition.from(12, 34);
			assertViolatesPrecondition(() -> selectFile((String) null, filePosition));
			assertViolatesPrecondition(() -> selectFile("   ", filePosition));

			var path = "src/test/resources/do_not_delete_me.txt";

			var selector = selectFile(path, filePosition);
			assertEquals(path, selector.getRawPath());
			assertEquals(new File(path), selector.getFile());
			assertEquals(Path.of(path), selector.getPath());
			assertEquals(filePosition, selector.getPosition().orElseThrow());
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectFileByFileReference() throws Exception {
			assertViolatesPrecondition(() -> selectFile((File) null));
			assertViolatesPrecondition(() -> selectFile(new File("bogus/nonexistent.txt")));

			var currentDir = new File(".").getCanonicalFile();
			var relativeDir = new File("..", currentDir.getName());
			var file = new File(relativeDir, "src/test/resources/do_not_delete_me.txt");
			var path = file.getCanonicalFile().getPath();

			var selector = selectFile(file);
			assertEquals(path, selector.getRawPath());
			assertEquals(file.getCanonicalFile(), selector.getFile());
			assertEquals(Path.of(path), selector.getPath());
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectFileByFileReferenceAndPosition() throws Exception {
			var filePosition = FilePosition.from(12, 34);
			assertViolatesPrecondition(() -> selectFile((File) null, filePosition));
			assertViolatesPrecondition(() -> selectFile(new File("bogus/nonexistent.txt"), filePosition));

			var currentDir = new File(".").getCanonicalFile();
			var relativeDir = new File("..", currentDir.getName());
			var file = new File(relativeDir, "src/test/resources/do_not_delete_me.txt");
			var path = file.getCanonicalFile().getPath();

			var selector = selectFile(file, filePosition);
			assertEquals(path, selector.getRawPath());
			assertEquals(file.getCanonicalFile(), selector.getFile());
			assertEquals(Path.of(path), selector.getPath());
			assertEquals(FilePosition.from(12, 34), selector.getPosition().orElseThrow());
		}

	}

	@Nested
	class SelectDirectoryTests {

		@Test
		void parseFileSelectorWithRelativePath() {
			var path = "src/test/resources/do_not_delete_me.txt";
			var selector = parseIdentifier(selectFile(path));

			assertThat(selector) //
					.asInstanceOf(type(FileSelector.class)) //
					.extracting(FileSelector::getRawPath, FileSelector::getFile, FileSelector::getPath,
						FileSelector::getPosition) //
					.containsExactly(path, new File(path), Path.of(path), Optional.empty());
		}

		@Test
		void parseFileSelectorWithAbsolutePath() {
			var path = "src/test/resources/do_not_delete_me.txt";
			var absolutePath = new File(path).getAbsolutePath();
			var selector = parseIdentifier(selectFile(absolutePath));

			assertThat(selector) //
					.asInstanceOf(type(FileSelector.class)) //
					.extracting(FileSelector::getRawPath, FileSelector::getFile, FileSelector::getPath,
						FileSelector::getPosition) //
					.containsExactly(absolutePath, new File(absolutePath), Path.of(absolutePath), Optional.empty());
		}

		@Test
		void parseFileSelectorWithRelativePathAndFilePosition() {
			var path = "src/test/resources/do_not_delete_me.txt";
			var filePosition = FilePosition.from(12, 34);
			var selector = parseIdentifier(selectFile(path, filePosition));

			assertThat(selector) //
					.asInstanceOf(type(FileSelector.class)) //
					.extracting(FileSelector::getRawPath, FileSelector::getFile, FileSelector::getPath,
						FileSelector::getPosition) //
					.containsExactly(path, new File(path), Path.of(path), Optional.of(filePosition));
		}

		@Test
		void parseFileSelectorWithAbsolutePathAndFilePosition() {
			var path = "src/test/resources/do_not_delete_me.txt";
			var absolutePath = new File(path).getAbsolutePath();
			var filePosition = FilePosition.from(12, 34);
			var selector = parseIdentifier(selectFile(absolutePath, filePosition));

			assertThat(selector) //
					.asInstanceOf(type(FileSelector.class)) //
					.extracting(FileSelector::getRawPath, FileSelector::getFile, FileSelector::getPath,
						FileSelector::getPosition) //
					.containsExactly(absolutePath, new File(absolutePath), Path.of(absolutePath),
						Optional.of(filePosition));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectDirectoryByName() {
			assertViolatesPrecondition(() -> selectDirectory((String) null));
			assertViolatesPrecondition(() -> selectDirectory("   "));

			var path = "src/test/resources";

			var selector = selectDirectory(path);
			assertEquals(path, selector.getRawPath());
			assertEquals(new File(path), selector.getDirectory());
			assertEquals(Path.of(path), selector.getPath());
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectDirectoryByFileReference() throws Exception {
			assertViolatesPrecondition(() -> selectDirectory((File) null));
			assertViolatesPrecondition(() -> selectDirectory(new File("bogus/nonexistent")));

			var currentDir = new File(".").getCanonicalFile();
			var relativeDir = new File("..", currentDir.getName());
			var directory = new File(relativeDir, "src/test/resources");
			var path = directory.getCanonicalFile().getPath();

			var selector = selectDirectory(directory);
			assertEquals(path, selector.getRawPath());
			assertEquals(directory.getCanonicalFile(), selector.getDirectory());
			assertEquals(Path.of(path), selector.getPath());
		}

	}

	@Nested
	class SelectClasspathResourceTests {

		@Test
		void parseDirectorySelectorWithRelativePath() {
			var path = "src/test/resources";

			var selector = parseIdentifier(selectDirectory(path));

			assertThat(selector) //
					.asInstanceOf(type(DirectorySelector.class)) //
					.extracting(DirectorySelector::getRawPath, DirectorySelector::getDirectory,
						DirectorySelector::getPath) //
					.containsExactly(path, new File(path), Path.of(path));
		}

		@Test
		void parseDirectorySelectorWithAbsolutePath() {
			var path = new File("src/test/resources").getAbsolutePath();

			var selector = parseIdentifier(selectDirectory(path));

			assertThat(selector) //
					.asInstanceOf(type(DirectorySelector.class)) //
					.extracting(DirectorySelector::getRawPath, DirectorySelector::getDirectory,
						DirectorySelector::getPath) //
					.containsExactly(path, new File(path), Path.of(path));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectClasspathResourcesPreconditions() {
			assertViolatesPrecondition(() -> selectClasspathResource((String) null));
			assertViolatesPrecondition(() -> selectClasspathResource(""));
			assertViolatesPrecondition(() -> selectClasspathResource("/"));
			assertViolatesPrecondition(() -> selectClasspathResource("    "));
			assertViolatesPrecondition(() -> selectClasspathResource("/   "));
			assertViolatesPrecondition(() -> selectClasspathResource("\t"));
			assertViolatesPrecondition(() -> selectClasspathResource("/\t"));
			assertViolatesPrecondition(() -> selectClasspathResource((Set<Resource>) null));
			assertViolatesPrecondition(() -> selectClasspathResource(Collections.emptySet()));
			assertViolatesPrecondition(() -> selectClasspathResource(Collections.singleton(null)));
			assertViolatesPrecondition(() -> selectClasspathResource(Set.of(new StubResource(null))));
			assertViolatesPrecondition(() -> selectClasspathResource(Set.of(new StubResource(""))));
			assertViolatesPrecondition(
				() -> selectClasspathResource(Set.of(new StubResource("a"), new StubResource("b"))));
		}

		@Test
		void selectClasspathResources() {
			// with unnecessary "/" prefix
			var selector = selectClasspathResource("/foo/bar/spec.xml");
			assertEquals("foo/bar/spec.xml", selector.getClasspathResourceName());

			// standard use case
			selector = selectClasspathResource("A/B/C/spec.json");
			assertEquals("A/B/C/spec.json", selector.getClasspathResourceName());
		}

		@Test
		void getSelectedClasspathResources() {
			var selector = selectClasspathResource("org/junit/platform/commons/example.resource");
			var classpathResources = selector.getClasspathResources();
			assertAll(() -> assertThat(classpathResources).hasSize(1), //
				() -> assertThat(classpathResources) //
						.extracting(Resource::getName) //
						.containsExactly("org/junit/platform/commons/example.resource") //
			);
		}

		@Test
		void getMissingClasspathResources() {
			var selector = selectClasspathResource("org/junit/platform/commons/no-such-example.resource");
			assertViolatesPrecondition(selector::getClasspathResources);
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectClasspathResourcesWithFilePosition() {
			var filePosition = FilePosition.from(12, 34);
			assertViolatesPrecondition(() -> selectClasspathResource(null, filePosition));
			assertViolatesPrecondition(() -> selectClasspathResource("", filePosition));
			assertViolatesPrecondition(() -> selectClasspathResource("    ", filePosition));
			assertViolatesPrecondition(() -> selectClasspathResource("\t", filePosition));

			// with unnecessary "/" prefix
			var selector = selectClasspathResource("/foo/bar/spec.xml", filePosition);
			assertEquals("foo/bar/spec.xml", selector.getClasspathResourceName());
			assertEquals(FilePosition.from(12, 34), selector.getPosition().orElseThrow());

			// standard use case
			selector = selectClasspathResource("A/B/C/spec.json", filePosition);
			assertEquals("A/B/C/spec.json", selector.getClasspathResourceName());
			assertEquals(filePosition, selector.getPosition().orElseThrow());
		}

		@Test
		void parseClasspathResources() {
			// with unnecessary "/" prefix
			var selector = parseIdentifier(selectClasspathResource("/foo/bar/spec.xml"));
			assertThat(selector) //
					.asInstanceOf(type(ClasspathResourceSelector.class)) //
					.extracting(ClasspathResourceSelector::getClasspathResourceName,
						ClasspathResourceSelector::getPosition) //
					.containsExactly("foo/bar/spec.xml", Optional.empty());

			// standard use case
			selector = parseIdentifier(selectClasspathResource("A/B/C/spec.json"));
			assertThat(selector) //
					.asInstanceOf(type(ClasspathResourceSelector.class)) //
					.extracting(ClasspathResourceSelector::getClasspathResourceName,
						ClasspathResourceSelector::getPosition) //
					.containsExactly("A/B/C/spec.json", Optional.empty());
		}

		@Test
		void parseClasspathResourcesWithFilePosition() {
			var filePosition = FilePosition.from(12, 34);
			// with unnecessary "/" prefix
			var selector = parseIdentifier(selectClasspathResource("/foo/bar/spec.xml", FilePosition.from(12, 34)));
			assertThat(selector) //
					.asInstanceOf(type(ClasspathResourceSelector.class)) //
					.extracting(ClasspathResourceSelector::getClasspathResourceName,
						ClasspathResourceSelector::getPosition) //
					.containsExactly("foo/bar/spec.xml", Optional.of(filePosition));

			// standard use case
			selector = parseIdentifier(selectClasspathResource("A/B/C/spec.json", FilePosition.from(12, 34)));
			assertThat(selector) //
					.asInstanceOf(type(ClasspathResourceSelector.class)) //
					.extracting(ClasspathResourceSelector::getClasspathResourceName,
						ClasspathResourceSelector::getPosition) //
					.containsExactly("A/B/C/spec.json", Optional.of(filePosition));
		}

		private record StubResource(String name) implements Resource {

			@Override
			public String getName() {
				return name();
			}

			@Override
			public URI getUri() {
				throw new UnsupportedOperationException();
			}
		}
	}

	@Nested
	class SelectModuleTests {

		@Test
		void selectModuleByName() {
			var selector = selectModule("java.base");
			assertEquals("java.base", selector.getModuleName());
			assertTrue(selector.getModule().isEmpty());
		}

		@Test
		void selectModuleByInstance() {
			var base = Object.class.getModule();
			var selector = selectModule(base);
			assertSame(base, selector.getModule().orElseThrow());
			assertEquals("java.base", selector.getModuleName());
		}

		@Test
		void parseModuleByName() {
			var selector = parseIdentifier(selectModule("java.base"));
			assertThat(selector) //
					.asInstanceOf(type(ModuleSelector.class)) //
					.extracting(ModuleSelector::getModuleName) //
					.isEqualTo("java.base");
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectModuleByNamePreconditions() {
			assertViolatesPrecondition(() -> selectModule((String) null));
			assertViolatesPrecondition(() -> selectModule(""));
			assertViolatesPrecondition(() -> selectModule("   "));
		}

		@Test
		void selectModulesByNames() {
			var selectors = selectModules(Set.of("a", "b"));
			var names = selectors.stream().map(ModuleSelector::getModuleName).toList();
			assertThat(names).containsExactlyInAnyOrder("b", "a");
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectModulesByNamesPreconditions() {
			assertViolatesPrecondition(() -> selectModules(null));
			assertViolatesPrecondition(() -> selectModules(Set.of("a", " ")));
		}

	}

	@Nested
	class SelectPackageTests {

		@Test
		void selectPackageByName() {
			var selector = selectPackage(getClass().getPackage().getName());
			assertEquals(getClass().getPackage().getName(), selector.getPackageName());
		}

		@Test
		void parsePackageByName() {
			var selector = parseIdentifier(selectPackage(getClass().getPackage().getName()));
			assertThat(selector) //
					.asInstanceOf(type(PackageSelector.class)) //
					.extracting(PackageSelector::getPackageName) //
					.isEqualTo(getClass().getPackage().getName());
		}

	}

	@Nested
	class SelectClasspathRootsTests {

		@Test
		void selectClasspathRootsWithNonExistingDirectory() {
			var selectors = selectClasspathRoots(Set.of(Path.of("some", "local", "path")));

			assertThat(selectors).isEmpty();
		}

		@Test
		void selectClasspathRootsWithNonExistingJarFile() {
			var selectors = selectClasspathRoots(Set.of(Path.of("some.jar")));

			assertThat(selectors).isEmpty();
		}

		@Test
		void selectClasspathRootsWithExistingDirectory(@TempDir Path tempDir) {
			var selectors = selectClasspathRoots(Set.of(tempDir));

			assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(tempDir.toUri());
		}

		@Test
		void selectClasspathRootsWithExistingJarFile() throws Exception {
			var jarUri = requireNonNull(getClass().getResource("/jartest.jar")).toURI();
			var jarFile = Path.of(jarUri);

			var selectors = selectClasspathRoots(Set.of(jarFile));

			assertThat(selectors).extracting(ClasspathRootSelector::getClasspathRoot).containsExactly(jarUri);
		}

	}

	@Nested
	class SelectClassTests {

		@Test
		void selectClassByName() {
			var selector = selectClass(getClass().getName());
			assertEquals(getClass(), selector.getJavaClass());
		}

		@Test
		void pareClassByName() {
			var selector = parseIdentifier(selectClass(getClass()));
			assertThat(selector) //
					.asInstanceOf(type(ClassSelector.class)) //
					.extracting(ClassSelector::getJavaClass) //
					.isEqualTo(getClass());
		}

		@Test
		void selectClassByNameWithExplicitClassLoader() throws Exception {
			try (var testClassLoader = TestClassLoader.forClasses(getClass())) {
				var selector = selectClass(testClassLoader, getClass().getName());

				assertThat(selector.getJavaClass().getName()).isEqualTo(getClass().getName());
				assertThat(selector.getJavaClass()).isNotEqualTo(getClass());
				assertThat(selector.getClassLoader()).isSameAs(testClassLoader);
				assertThat(selector.getJavaClass().getClassLoader()).isSameAs(testClassLoader);
			}
		}

	}

	@Nested
	class SelectMethodTests {

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(className, methodName)")
		void selectMethodByClassNameAndMethodNamePreconditions() {
			assertViolatesPrecondition(() -> selectMethod("TestClass", null));
			assertViolatesPrecondition(() -> selectMethod("TestClass", ""));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "  "));
			assertViolatesPrecondition(() -> selectMethod((String) null, "method"));
			assertViolatesPrecondition(() -> selectMethod("", "method"));
			assertViolatesPrecondition(() -> selectMethod("   ", "method"));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(className, methodName, parameterTypeNames)")
		void selectMethodByClassNameMethodNameAndParameterTypeNamesPreconditions() {
			assertViolatesPrecondition(() -> selectMethod("TestClass", null, "int"));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "", "int"));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "  ", "int"));
			assertViolatesPrecondition(() -> selectMethod((String) null, "method", "int"));
			assertViolatesPrecondition(() -> selectMethod("", "method", "int"));
			assertViolatesPrecondition(() -> selectMethod("   ", "method", "int"));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "method", (String) null));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(className, methodName, parameterTypes)")
		void selectMethodByClassNameMethodNameAndParameterTypesPreconditions() {
			assertViolatesPrecondition(() -> selectMethod("TestClass", null, int.class));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "", int.class));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "  ", int.class));
			assertViolatesPrecondition(() -> selectMethod((String) null, "method", int.class));
			assertViolatesPrecondition(() -> selectMethod("", "method", int.class));
			assertViolatesPrecondition(() -> selectMethod("   ", "method", int.class));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "method", (Class<?>) null));
			assertViolatesPrecondition(() -> selectMethod("TestClass", "method", new Class<?>[] { int.class, null }));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(class, methodName)")
		void selectMethodByClassAndMethodNamePreconditions() {
			assertViolatesPrecondition(() -> selectMethod(testClass(), (String) null));
			assertViolatesPrecondition(() -> selectMethod(testClass(), ""));
			assertViolatesPrecondition(() -> selectMethod(testClass(), "  "));
			assertViolatesPrecondition(() -> selectMethod((Class<?>) null, "method"));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(class, methodName, parameterTypeNames)")
		void selectMethodByClassMethodNameAndParameterTypeNamesPreconditions() {
			assertViolatesPrecondition(() -> selectMethod((Class<?>) null, "method", "int"));
			assertViolatesPrecondition(() -> selectMethod(testClass(), null, "int"));
			assertViolatesPrecondition(() -> selectMethod(testClass(), "", "int"));
			assertViolatesPrecondition(() -> selectMethod(testClass(), "  ", "int"));
			assertViolatesPrecondition(() -> selectMethod(testClass(), "method", (String) null));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectMethod(class, method)")
		void selectMethodByClassAndMethodPreconditions() {
			var method = getClass().getDeclaredMethods()[0];
			assertViolatesPrecondition(() -> selectMethod(null, method));
			assertViolatesPrecondition(() -> selectMethod(testClass(), (Method) null));
		}

		@ParameterizedTest(name = "FQMN: ''{0}''")
		@MethodSource("invalidFullyQualifiedMethodNames")
		@DisplayName("Preconditions: selectMethod(FQMN)")
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
			Class<?> clazz = testClass();
			var method = clazz.getDeclaredMethod("myTest");
			var selector = assertSelectMethodByFullyQualifiedName(clazz, method);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithExplicitClassLoader() throws Exception {
			try (var testClassLoader = TestClassLoader.forClasses(testClass())) {
				var clazz = testClassLoader.loadClass(testClass().getName());
				assertThat(clazz).isNotEqualTo(testClass());

				var method = clazz.getDeclaredMethod("myTest");
				var selector = selectMethod(testClassLoader, testClass().getName(), "myTest");
				assertThat(selector.getJavaMethod()).isEqualTo(method);
				assertThat(selector.getJavaClass()).isEqualTo(clazz);
				assertThat(selector.getClassName()).isEqualTo(clazz.getName());
				assertThat(selector.getMethodName()).isEqualTo(method.getName());
				assertThat(selector.getParameterTypeNames()).isEmpty();
			}
		}

		@Test
		void selectMethodByFullyQualifiedNameForDefaultMethodInInterface() throws Exception {
			Class<?> clazz = TestCaseWithDefaultMethod.class;
			var method = clazz.getMethod("myTest");
			var selector = assertSelectMethodByFullyQualifiedName(clazz, method);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithPrimitiveParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int.class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, int.class, "int");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithPrimitiveParameterUsingSourceCodeSyntax() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int.class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "int", "int");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithObjectParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String.class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, String.class,
				String.class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithObjectParameterUsingSourceCodeSyntax() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String.class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "java.lang.String",
				String.class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int[].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, int[].class,
				int[].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithPrimitiveArrayParameterUsingSourceCodeSyntax() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int[].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "int[]", "int[]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithObjectArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String[].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, String[].class,
				String[].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithObjectArrayParameterUsingSourceCodeSyntax() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String[].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "java.lang.String[]",
				"java.lang.String[]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int[][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, int[][].class,
				int[][].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithTwoDimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
				throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int[][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "int[][]", "int[][]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String[][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, String[][].class,
				String[][].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithTwoDimensionalObjectArrayParameterUsingSourceCodeSyntax()
				throws Exception {
			var method = testClass().getDeclaredMethod("myTest", String[][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "java.lang.String[][]",
				"java.lang.String[][]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", int[][][][][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, int[][][][][].class,
				int[][][][][].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithMultidimensionalPrimitiveArrayParameterUsingSourceCodeSyntax()
				throws Exception {

			var method = testClass().getDeclaredMethod("myTest", int[][][][][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "int[][][][][]",
				"int[][][][][]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameter() throws Exception {
			var method = testClass().getDeclaredMethod("myTest", Double[][][][][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, Double[][][][][].class,
				Double[][][][][].class.getName());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameWithMultidimensionalObjectArrayParameterUsingSourceCodeSyntax()
				throws Exception {

			var method = testClass().getDeclaredMethod("myTest", Double[][][][][].class);
			var selector = assertSelectMethodByFullyQualifiedName(testClass(), method, "java.lang.Double[][][][][]",
				"java.lang.Double[][][][][]");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByFullyQualifiedNameEndingInOpeningParenthesis() {
			var className = "org.example.MyClass";
			// The following bizarre method name is not permissible in Java source
			// code; however, it'selector permitted by the JVM -- for example, in Groovy
			// or Kotlin source code using back ticks.
			var methodName = ")--(";
			var fqmn = className + "#" + methodName;

			var selector = selectMethod(fqmn);
			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * Inspired by Spock specifications.
		 */
		@Test
		void selectMethodByFullyQualifiedNameContainingHashtags() {
			var className = "org.example.CalculatorSpec";
			var methodName = "#a plus #b equals #c";
			var fqmn = className + "#" + methodName;

			var selector = selectMethod(fqmn);
			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * Inspired by Spock specifications.
		 */
		@Test
		void selectMethodByFullyQualifiedNameContainingHashtagsAndWithParameterList() {
			var className = "org.example.CalculatorSpec";
			var methodName = "#a plus #b equals #c";
			var methodParameters = "int, int, int";
			var fqmn = "%s#%s(%s)".formatted(className, methodName, methodParameters);

			var selector = selectMethod(fqmn);
			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals(methodParameters, selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * Inspired by Kotlin tests.
		 */
		@Test
		void selectMethodByFullyQualifiedNameContainingParentheses() {
			var className = "org.example.KotlinTestCase";
			var methodName = "🦆 ~|~test with a really, (really) terrible name & that needs to be changed!~|~";
			var fqmn = className + "#" + methodName;

			var selector = selectMethod(fqmn);

			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * Inspired by Kotlin tests.
		 */
		@Test
		void selectMethodByFullyQualifiedNameEndingWithParentheses() {
			var className = "org.example.KotlinTestCase";
			var methodName = "test name ends with parentheses()";
			var fqmn = className + "#" + methodName + "()";

			var selector = selectMethod(fqmn);

			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * Inspired by Kotlin tests.
		 */
		@Test
		void selectMethodByFullyQualifiedNameEndingWithParenthesesAndWithParameterList() {
			var className = "org.example.KotlinTestCase";
			var methodName = "test name ends with parentheses()";
			var methodParameters = "int, int, int";
			var fqmn = "%s#%s(%s)".formatted(className, methodName, methodParameters);

			var selector = selectMethod(fqmn);

			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals(methodParameters, selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		private MethodSelector assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method) {
			var selector = selectMethod(fqmn(clazz, method.getName()));
			assertEquals(method, selector.getJavaMethod());
			assertEquals(clazz, selector.getJavaClass());
			assertEquals(clazz.getName(), selector.getClassName());
			assertEquals(method.getName(), selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			return selector;
		}

		private MethodSelector assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method,
				Class<?> parameterType, String expectedParameterTypes) {

			var selector = selectMethod(fqmn(parameterType));
			assertEquals(method, selector.getJavaMethod());
			assertEquals(clazz, selector.getJavaClass());
			assertEquals(clazz.getName(), selector.getClassName());
			assertEquals(method.getName(), selector.getMethodName());
			assertEquals(expectedParameterTypes, selector.getParameterTypeNames());
			return selector;
		}

		private MethodSelector assertSelectMethodByFullyQualifiedName(Class<?> clazz, Method method,
				String parameterName, String expectedParameterTypes) {

			var selector = selectMethod(fqmnWithParamNames(parameterName));
			assertEquals(method, selector.getJavaMethod());
			assertEquals(clazz, selector.getJavaClass());
			assertEquals(clazz.getName(), selector.getClassName());
			assertEquals(method.getName(), selector.getMethodName());
			assertEquals(expectedParameterTypes, selector.getParameterTypeNames());
			return selector;
		}

		@Test
		void selectMethodByClassAndMethodName() throws Exception {
			var method = testClass().getDeclaredMethod("myTest");

			var selector = selectMethod(testClass(), "myTest");
			assertEquals(testClass(), selector.getJavaClass());
			assertEquals(testClass().getName(), selector.getClassName());
			assertEquals(method, selector.getJavaMethod());
			assertEquals("myTest", selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByClassMethodNameAndParameterTypeNames() throws Exception {
			var testClass = testClass();
			var method = testClass.getDeclaredMethod("myTest", String.class, boolean[].class);

			var selector = selectMethod(testClass, "myTest", "java.lang.String, boolean[]");

			assertThat(selector.getClassName()).isEqualTo(testClass.getName());
			assertThat(selector.getJavaClass()).isEqualTo(testClass);
			assertThat(selector.getMethodName()).isEqualTo("myTest");
			assertThat(selector.getJavaMethod()).isEqualTo(method);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String, boolean[]");
			assertThat(selector.getParameterTypes()).containsExactly(String.class, boolean[].class);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByClassNameMethodNameAndParameterTypes() throws Exception {
			var testClass = testClass();
			var method = testClass.getDeclaredMethod("myTest", String.class, boolean[].class);

			var selector = selectMethod(testClass.getName(), "myTest", String.class, boolean[].class);

			assertThat(selector.getClassName()).isEqualTo(testClass.getName());
			assertThat(selector.getJavaClass()).isEqualTo(testClass);
			assertThat(selector.getMethodName()).isEqualTo("myTest");
			assertThat(selector.getJavaMethod()).isEqualTo(method);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String, boolean[]");
			assertThat(selector.getParameterTypes()).containsExactly(String.class, boolean[].class);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodByClassNameMethodNameAndParameterTypeNamesWithExplicitClassLoader() throws Exception {
			var testClass = testClass();

			try (var testClassLoader = TestClassLoader.forClasses(testClass)) {
				var clazz = testClassLoader.loadClass(testClass.getName());
				assertThat(clazz).isNotEqualTo(testClass);

				var method = clazz.getDeclaredMethod("myTest", String.class, boolean[].class);
				var selector = selectMethod(testClassLoader, testClass.getName(), "myTest",
					"java.lang.String, boolean[]");

				assertThat(selector.getClassName()).isEqualTo(clazz.getName());
				assertThat(selector.getJavaClass()).isEqualTo(clazz);
				assertThat(selector.getMethodName()).isEqualTo(method.getName());
				assertThat(selector.getJavaMethod()).isEqualTo(method);
				assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String, boolean[]");
				assertThat(selector.getParameterTypes()).containsExactly(String.class, boolean[].class);
				assertThat(parseIdentifier(selector)).isEqualTo(selector);
			}
		}

		@Test
		void selectMethodByClassMethodNameAndParameterTypes() throws Exception {
			var testClass = testClass();
			var method = testClass.getDeclaredMethod("myTest", String.class, boolean[].class);

			var selector = selectMethod(testClass, "myTest", String.class, boolean[].class);

			assertThat(selector.getClassName()).isEqualTo(testClass.getName());
			assertThat(selector.getJavaClass()).isEqualTo(testClass);
			assertThat(selector.getMethodName()).isEqualTo("myTest");
			assertThat(selector.getJavaMethod()).isEqualTo(method);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String, boolean[]");
			assertThat(selector.getParameterTypes()).containsExactly(String.class, boolean[].class);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectMethodWithParametersByMethodReference() throws Exception {
			var testClass = testClass();
			var method = testClass.getDeclaredMethod("myTest", String.class, boolean[].class);

			var selector = selectMethod(testClass, method);

			assertThat(selector.getClassName()).isEqualTo(testClass.getName());
			assertThat(selector.getJavaClass()).isEqualTo(testClass);
			assertThat(selector.getMethodName()).isEqualTo("myTest");
			assertThat(selector.getJavaMethod()).isEqualTo(method);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String, boolean[]");
			assertThat(selector.getParameterTypes()).containsExactly(String.class, boolean[].class);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectClassByNameForSpockSpec() {
			var className = "org.example.CalculatorSpec";
			var selector = selectClass(className);
			assertEquals(className, selector.getClassName());
		}

		@Test
		void selectMethodByClassAndNameForSpockSpec() {
			var className = "org.example.CalculatorSpec";
			var methodName = "#a plus #b equals #c";

			var selector = selectMethod(className, methodName);
			assertEquals(className, selector.getClassName());
			assertEquals(methodName, selector.getMethodName());
			assertEquals("", selector.getParameterTypeNames());
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		private static Class<?> testClass() {
			return DiscoverySelectorsTests.class;
		}

	}

	@Test
	void parseClasspathRootsWithNonExistingDirectory() {
		var selectorStream = parseIdentifiers(selectClasspathRoots(Set.of(Path.of("some/local/path"))));
		assertThat(selectorStream).isEmpty();
	}

	@Test
	void parseClasspathRootsWithNonExistingJarFile() {
		var selectorStream = parseIdentifiers(selectClasspathRoots(Set.of(Path.of("some.jar"))));
		assertThat(selectorStream).isEmpty();
	}

	@Test
	void parseClasspathRootsWithExistingDirectory(@TempDir Path tempDir) {
		var selectorStream = parseIdentifiers(selectClasspathRoots(Set.of(tempDir)));
		var selector = selectorStream.findAny().orElseThrow();
		assertThat(selector) //
				.asInstanceOf(type(ClasspathRootSelector.class)) //
				.extracting(ClasspathRootSelector::getClasspathRoot) //
				.isEqualTo(tempDir.toUri());
	}

	@Test
	void parseClasspathRootsWithExistingJarFile() throws Exception {
		var jarUri = requireNonNull(getClass().getResource("/jartest.jar")).toURI();
		var jarPath = Path.of(jarUri);

		var selectorStream = parseIdentifiers(selectClasspathRoots(Set.of(jarPath)));
		var selector = selectorStream.findAny().orElseThrow();
		assertThat(selector) //
				.asInstanceOf(type(ClasspathRootSelector.class)) //
				.extracting(ClasspathRootSelector::getClasspathRoot) //
				.isEqualTo(jarUri);
	}

	@Nested
	class SelectNestedClassAndSelectNestedMethodTests {

		private final String enclosingClassName = getClass().getName() + "$ClassWithNestedInnerClass";
		private final String nestedClassName = getClass().getName() + "$ClassWithNestedInnerClass$NestedClass";
		private final String doubleNestedClassName = nestedClassName + "$DoubleNestedClass";
		private final String methodName = "nestedTest";

		@Test
		void selectNestedClassByClassNames() {
			var selector = selectNestedClass(List.of(enclosingClassName), nestedClassName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectNestedClassByClassNamesWithExplicitClassLoader() throws Exception {
			var testClasses = List.of(ClassWithNestedInnerClass.class, ClassWithNestedInnerClass.NestedClass.class);

			try (var testClassLoader = TestClassLoader.forClasses(testClasses)) {
				var selector = selectNestedClass(testClassLoader, List.of(enclosingClassName), nestedClassName);

				assertThat(selector.getEnclosingClasses()).doesNotContain(ClassWithNestedInnerClass.class);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getName).containsOnly(enclosingClassName);
				assertThat(selector.getNestedClass()).isNotEqualTo(ClassWithNestedInnerClass.NestedClass.class);
				assertThat(selector.getNestedClass().getName()).isEqualTo(nestedClassName);

				assertThat(selector.getClassLoader()).isSameAs(testClassLoader);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getClassLoader).containsOnly(
					testClassLoader);
				assertThat(selector.getNestedClass().getClassLoader()).isSameAs(testClassLoader);
				assertThat(parseIdentifier(selector)).isEqualTo(selector);
			}
		}

		@Test
		void selectDoubleNestedClassByClassNames() {
			var selector = selectNestedClass(List.of(enclosingClassName, nestedClassName), doubleNestedClassName);

			assertThat(selector.getEnclosingClasses()).containsExactly(ClassWithNestedInnerClass.class,
				ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(
				ClassWithNestedInnerClass.NestedClass.DoubleNestedClass.class);

			assertThat(selector.getEnclosingClassNames()).containsExactly(enclosingClassName, nestedClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(doubleNestedClassName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		void selectNestedClassPreconditions() {
			assertViolatesPrecondition(() -> selectNestedClass(null, "ClassName"));
			assertViolatesPrecondition(() -> selectNestedClass(List.of(), "ClassName"));
			assertViolatesPrecondition(() -> selectNestedClass(List.of("ClassName"), null));
			assertViolatesPrecondition(() -> selectNestedClass(List.of("ClassName"), ""));
			assertViolatesPrecondition(() -> selectNestedClass(List.of("ClassName"), " "));
		}

		@Test
		void selectNestedMethodByEnclosingClassNamesAndMethodName() throws Exception {
			var selector = selectNestedMethod(List.of(enclosingClassName), nestedClassName, methodName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(selector.getNestedClass().getDeclaredMethod(methodName));

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(methodName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectNestedMethodByEnclosingClassNamesAndMethodNameWithExplicitClassLoader() throws Exception {
			var testClasses = List.of(ClassWithNestedInnerClass.class, ClassWithNestedInnerClass.NestedClass.class);

			try (var testClassLoader = TestClassLoader.forClasses(testClasses)) {
				var selector = selectNestedMethod(testClassLoader, List.of(enclosingClassName), nestedClassName,
					methodName);

				assertThat(selector.getEnclosingClasses()).doesNotContain(ClassWithNestedInnerClass.class);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getName).containsOnly(enclosingClassName);
				assertThat(selector.getNestedClass()).isNotEqualTo(ClassWithNestedInnerClass.NestedClass.class);
				assertThat(selector.getNestedClass().getName()).isEqualTo(nestedClassName);

				assertThat(selector.getClassLoader()).isSameAs(testClassLoader);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getClassLoader).containsOnly(
					testClassLoader);
				assertThat(selector.getNestedClass().getClassLoader()).isSameAs(testClassLoader);

				assertThat(selector.getMethodName()).isEqualTo(methodName);
				assertThat(parseIdentifier(selector)).isEqualTo(selector);
			}
		}

		@Test
		void selectNestedMethodByEnclosingClassesAndMethodName() throws Exception {
			var selector = selectNestedMethod(List.of(ClassWithNestedInnerClass.class),
				ClassWithNestedInnerClass.NestedClass.class, methodName);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(selector.getNestedClass().getDeclaredMethod(methodName));

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(methodName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectNestedMethodByEnclosingClassNamesMethodNameAndParameterTypeNames() throws Exception {
			var selector = selectNestedMethod(List.of(enclosingClassName), nestedClassName, methodName,
				String.class.getName());

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(
				selector.getNestedClass().getDeclaredMethod(methodName, String.class));

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(methodName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * @since 1.0
		 */
		@Test
		void selectNestedMethodByEnclosingClassNamesMethodNameAndParameterTypes() throws Exception {
			var selector = selectNestedMethod(List.of(enclosingClassName), nestedClassName, methodName, String.class);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(
				selector.getNestedClass().getDeclaredMethod(methodName, String.class));
			assertThat(selector.getParameterTypes()).containsExactly(String.class);

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(methodName);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		/**
		 * @since 1.10
		 */
		@Test
		void selectNestedMethodByEnclosingClassNamesMethodNameAndParameterTypeNamesWithExplicitClassLoader()
				throws Exception {

			var enclosingClass = ClassWithNestedInnerClass.class;
			var nestedClass = ClassWithNestedInnerClass.NestedClass.class;

			try (var testClassLoader = TestClassLoader.forClasses(enclosingClass, nestedClass)) {
				var selector = selectNestedMethod(testClassLoader, List.of(enclosingClassName), nestedClassName,
					methodName, String.class.getName());

				assertThat(selector.getEnclosingClasses()).doesNotContain(enclosingClass);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getName).containsOnly(enclosingClassName);
				assertThat(selector.getNestedClass()).isNotEqualTo(nestedClass);
				assertThat(selector.getNestedClass().getName()).isEqualTo(nestedClassName);

				assertThat(selector.getClassLoader()).isSameAs(testClassLoader);
				assertThat(selector.getEnclosingClasses()).extracting(Class::getClassLoader).containsOnly(
					testClassLoader);
				assertThat(selector.getNestedClass().getClassLoader()).isSameAs(testClassLoader);

				assertThat(selector.getMethodName()).isEqualTo(methodName);
				assertThat(selector.getParameterTypeNames()).isEqualTo(String.class.getName());
				assertThat(parseIdentifier(selector)).isEqualTo(selector);
			}
		}

		/**
		 * @since 1.10
		 */
		@Test
		void selectNestedMethodByEnclosingClassesMethodNameAndParameterTypes() throws Exception {
			var selector = selectNestedMethod(List.of(ClassWithNestedInnerClass.class),
				ClassWithNestedInnerClass.NestedClass.class, methodName, String.class);

			assertThat(selector.getEnclosingClasses()).containsOnly(ClassWithNestedInnerClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(
				selector.getNestedClass().getDeclaredMethod(methodName, String.class));
			assertThat(selector.getParameterTypes()).containsExactly(String.class);

			assertThat(selector.getEnclosingClassNames()).containsOnly(enclosingClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(nestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(methodName);
			assertThat(selector.getParameterTypeNames()).isEqualTo("java.lang.String");
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@Test
		void selectDoubleNestedMethodByEnclosingClassNamesAndMethodName() throws Exception {
			var doubleNestedMethodName = "doubleNestedTest";
			var selector = selectNestedMethod(List.of(enclosingClassName, nestedClassName), doubleNestedClassName,
				doubleNestedMethodName);

			assertThat(selector.getEnclosingClasses()).containsExactly(ClassWithNestedInnerClass.class,
				ClassWithNestedInnerClass.NestedClass.class);
			assertThat(selector.getNestedClass()).isEqualTo(
				ClassWithNestedInnerClass.NestedClass.DoubleNestedClass.class);
			assertThat(selector.getMethod()).isEqualTo(
				selector.getNestedClass().getDeclaredMethod(doubleNestedMethodName));

			assertThat(selector.getEnclosingClassNames()).containsExactly(enclosingClassName, nestedClassName);
			assertThat(selector.getNestedClassName()).isEqualTo(doubleNestedClassName);
			assertThat(selector.getMethodName()).isEqualTo(doubleNestedMethodName);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectNestedMethod(enclosingClassNames, nestedClassName, methodName)")
		void selectNestedMethodByEnclosingClassNamesAndMethodNamePreconditions() {
			assertViolatesPrecondition(() -> selectNestedMethod(null, "ClassName", "methodName"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of(), "ClassName", "methodName"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), null, "methodName"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), " ", "methodName"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", null));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", " "));
		}

		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectNestedMethod(enclosingClassNames, nestedClassName, methodName, parameterTypeNames)")
		void selectNestedMethodByEnclosingClassNamesMethodNameAndParameterTypeNamesPreconditions() {
			assertViolatesPrecondition(() -> selectNestedMethod(null, "ClassName", "methodName", "int"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of(), "ClassName", "methodName", "int"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), null, "methodName", "int"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), " ", "methodName", "int"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", null, "int"));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", " ", "int"));
			assertViolatesPrecondition(
				() -> selectNestedMethod(List.of("ClassName"), "ClassName", "methodName", (String) null));
		}

		/**
		 * @since 1.10
		 */
		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectNestedMethod(enclosingClassNames, nestedClassName, methodName, parameterTypes)")
		void selectNestedMethodByEnclosingClassNamesMethodNameAndParameterTypesPreconditions() {
			assertViolatesPrecondition(() -> selectNestedMethod(null, "ClassName", "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of(), "ClassName", "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), null, "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), " ", "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", null, int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", " ", int.class));
			assertViolatesPrecondition(
				() -> selectNestedMethod(List.of("ClassName"), "ClassName", "methodName", (Class<?>) null));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of("ClassName"), "ClassName", "methodName",
				new Class<?>[] { int.class, null }));
		}

		/**
		 * @since 1.10
		 */
		@SuppressWarnings("DataFlowIssue")
		@Test
		@DisplayName("Preconditions: selectNestedMethod(enclosingClasses, nestedClass, methodName, parameterTypes)")
		void selectNestedMethodByEnclosingClassesClassMethodNameAndParameterTypesPreconditions() {
			List<Class<?>> enclosingClassList = List.of(ClassWithNestedInnerClass.class);
			Class<?> nestedClass = ClassWithNestedInnerClass.NestedClass.class;

			assertViolatesPrecondition(() -> selectNestedMethod(null, nestedClass, "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(List.of(), nestedClass, "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(enclosingClassList, null, "methodName", int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(enclosingClassList, nestedClass, null, int.class));
			assertViolatesPrecondition(() -> selectNestedMethod(enclosingClassList, nestedClass, " ", int.class));
			assertViolatesPrecondition(
				() -> selectNestedMethod(enclosingClassList, nestedClass, "methodName", (Class<?>) null));
			assertViolatesPrecondition(() -> selectNestedMethod(enclosingClassList, nestedClass, "methodName",
				new Class<?>[] { int.class, null }));
		}

		static class ClassWithNestedInnerClass {

			// @Nested
			@SuppressWarnings({ "InnerClassMayBeStatic", "unused" })
			class NestedClass {

				// @Test
				void nestedTest() {
				}

				// @Test
				void nestedTest(String parameter) {
				}

				// @Nested
				class DoubleNestedClass {

					// @Test
					void doubleNestedTest() {
					}
				}
			}
		}

	}

	@Nested
	class SelectIterationTests {
		@Test
		void selectsIteration() throws Exception {
			Class<?> clazz = DiscoverySelectorsTests.class;
			var method = clazz.getDeclaredMethod("myTest", int.class);
			var parentSelector = selectMethod(clazz, method);
			var selector = DiscoverySelectors.selectIteration(parentSelector, 23, 42);
			assertThat(selector.getParentSelector()).isSameAs(parentSelector);
			assertThat(selector.getIterationIndices()).containsExactly(23, 42);
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}
	}

	@Nested
	class SelectUniqueIdTests {
		@Test
		void selectsUniqueId() {
			var selector = selectUniqueId(uniqueIdForMethod(DiscoverySelectorsTests.class, "myTest(int)"));
			assertThat(selector.getUniqueId()).isNotNull();
			assertThat(parseIdentifier(selector)).isEqualTo(selector);
		}
	}

	// -------------------------------------------------------------------------

	private void assertViolatesPrecondition(Executable precondition) {
		assertThrows(PreconditionViolationException.class, precondition);
	}

	private static DiscoverySelector parseIdentifier(DiscoverySelector selector) {
		return DiscoverySelectors.parse(toIdentifierString(selector)).orElseThrow();
	}

	private static Stream<? extends DiscoverySelector> parseIdentifiers(
			Collection<? extends DiscoverySelector> selectors) {
		return DiscoverySelectors.parseAll(
			selectors.stream().map(it -> DiscoverySelectorIdentifier.parse(toIdentifierString(it))).toList());
	}

	private static String toIdentifierString(DiscoverySelector selector) {
		return selector.toIdentifier().orElseThrow().toString();
	}

	private static String fqmn(Class<?>... params) {
		return fqmn(DiscoverySelectorsTests.class, "myTest", params);
	}

	private static String fqmn(Class<?> clazz, String methodName, Class<?>... params) {
		return ReflectionUtils.getFullyQualifiedMethodName(clazz, methodName, params);
	}

	private static String fqmnWithParamNames(String... params) {
		return "%s#%s(%s)".formatted(DiscoverySelectorsTests.class.getName(), "myTest", join(", ", params));
	}

	interface TestInterface {

		@Test
		default void myTest() {
		}

	}

	private static class TestCaseWithDefaultMethod implements TestInterface {

	}

	@SuppressWarnings("unused")
	void myTest() {
	}

	@SuppressWarnings("unused")
	void myTest(int num) {
	}

	@SuppressWarnings("unused")
	void myTest(int[] nums) {
	}

	@SuppressWarnings("unused")
	void myTest(int[][] grid) {
	}

	@SuppressWarnings("unused")
	void myTest(int[][][][][] grid) {
	}

	@SuppressWarnings("unused")
	void myTest(String info) {
	}

	@SuppressWarnings("unused")
	void myTest(String info, boolean[] flags) {
	}

	@SuppressWarnings("unused")
	void myTest(String[] info) {
	}

	@SuppressWarnings("unused")
	void myTest(String[][] info) {
	}

	@SuppressWarnings("unused")
	void myTest(Double[][][][][] data) {
	}

}
