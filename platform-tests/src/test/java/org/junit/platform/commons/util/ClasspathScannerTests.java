/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.LogRecordListener;

/**
 * Unit tests for {@link ClasspathScanner}.
 *
 * @since 1.0
 */
@TrackLogRecords
class ClasspathScannerTests {

	private static final ClassFilter allClasses = ClassFilter.of(type -> true);

	private final List<Class<?>> loadedClasses = new ArrayList<>();

	private final BiFunction<String, ClassLoader, Try<Class<?>>> trackingClassLoader = (name,
			classLoader) -> ReflectionUtils.tryToLoadClass(name, classLoader).ifSuccess(loadedClasses::add);

	private final ClasspathScanner classpathScanner = new ClasspathScanner(ClassLoaderUtils::getDefaultClassLoader,
		trackingClassLoader);

	@Test
	void scanForClassesInClasspathRootWhenMalformedClassnameInternalErrorOccursWithNullDetailedMessage(
			LogRecordListener listener) throws Exception {

		Predicate<Class<?>> malformedClassNameSimulationFilter = clazz -> {
			if (clazz.getSimpleName().equals(ClassForMalformedClassNameSimulation.class.getSimpleName())) {
				throw new InternalError();
			}
			return true;
		};

		assertClassesScannedWhenExceptionIsThrown(malformedClassNameSimulationFilter);
		assertDebugMessageLogged(listener, "Failed to load java.lang.Class for path .+ during classpath scanning.");
	}

	@Test
	void scanForClassesInClasspathRootWhenMalformedClassnameInternalErrorOccurs(LogRecordListener listener)
			throws Exception {

		Predicate<Class<?>> malformedClassNameSimulationFilter = clazz -> {
			if (clazz.getSimpleName().equals(ClassForMalformedClassNameSimulation.class.getSimpleName())) {
				throw new InternalError("Malformed class name");
			}
			return true;
		};

		assertClassesScannedWhenExceptionIsThrown(malformedClassNameSimulationFilter);
		assertDebugMessageLogged(listener, "The java.lang.Class loaded from path .+ has a malformed class name .+");
	}

	@Test
	void scanForClassesInClasspathRootWhenOtherInternalErrorOccurs(LogRecordListener listener) throws Exception {
		Predicate<Class<?>> otherInternalErrorSimulationFilter = clazz -> {
			if (clazz.getSimpleName().equals(ClassForOtherInternalErrorSimulation.class.getSimpleName())) {
				throw new InternalError("other internal error");
			}
			return true;
		};

		assertClassesScannedWhenExceptionIsThrown(otherInternalErrorSimulationFilter);
		assertDebugMessageLogged(listener, "Failed to load java.lang.Class for path .+ during classpath scanning.");
	}

	@Test
	void scanForClassesInClasspathRootWhenGenericRuntimeExceptionOccurs(LogRecordListener listener) throws Exception {
		Predicate<Class<?>> runtimeExceptionSimulationFilter = clazz -> {
			if (clazz.getSimpleName().equals(ClassForGenericRuntimeExceptionSimulation.class.getSimpleName())) {
				throw new RuntimeException("a generic exception");
			}
			return true;
		};

		assertClassesScannedWhenExceptionIsThrown(runtimeExceptionSimulationFilter);
		assertDebugMessageLogged(listener, "Failed to load java.lang.Class for path .+ during classpath scanning.");
	}

	private void assertClassesScannedWhenExceptionIsThrown(Predicate<Class<?>> filter) throws Exception {
		ClassFilter classFilter = ClassFilter.of(filter);
		List<Class<?>> classes = this.classpathScanner.scanForClassesInClasspathRoot(getTestClasspathRoot(),
			classFilter);
		assertThat(classes.size()).isGreaterThanOrEqualTo(150);
	}

	private void assertDebugMessageLogged(LogRecordListener listener, String regex) {
		// @formatter:off
		assertThat(listener.stream(ClasspathScanner.class, Level.FINE)
				.map(LogRecord::getMessage)
				.filter(m -> m.matches(regex))
		).hasSize(1);
		// @formatter:on
	}

	@Test
	void scanForClassesInClasspathRootWhenOutOfMemoryErrorOccurs() {
		Predicate<Class<?>> outOfMemoryErrorSimulationFilter = clazz -> {
			if (clazz.getSimpleName().equals(ClassForOutOfMemoryErrorSimulation.class.getSimpleName())) {
				throw new OutOfMemoryError();
			}
			return true;
		};
		ClassFilter classFilter = ClassFilter.of(outOfMemoryErrorSimulationFilter);

		assertThrows(OutOfMemoryError.class,
			() -> this.classpathScanner.scanForClassesInClasspathRoot(getTestClasspathRoot(), classFilter));
	}

	@Test
	void scanForClassesInClasspathRootWithinJarFile() throws Exception {
		scanForClassesInClasspathRootWithinJarFile("/jartest.jar");
	}

	@Test
	void scanForClassesInClasspathRootWithinJarWithSpacesInPath() throws Exception {
		scanForClassesInClasspathRootWithinJarFile("/folder with spaces/jar test with spaces.jar");
	}

	private void scanForClassesInClasspathRootWithinJarFile(String resourceName) throws Exception {
		URL jarfile = getClass().getResource(resourceName);

		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { jarfile })) {
			ClasspathScanner classpathScanner = new ClasspathScanner(() -> classLoader,
				ReflectionUtils::tryToLoadClass);

			List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(jarfile.toURI(), allClasses);
			List<String> classNames = classes.stream().map(Class::getName).collect(Collectors.toList());
			assertThat(classNames).hasSize(3) //
					.contains("org.junit.platform.jartest.notincluded.NotIncluded",
						"org.junit.platform.jartest.included.recursive.RecursivelyIncluded",
						"org.junit.platform.jartest.included.Included");
		}
	}

	@Test
	void scanForClassesInPackage() {
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.platform.commons", allClasses);
		assertThat(classes.size()).isGreaterThanOrEqualTo(20);
		assertTrue(classes.contains(NestedClassToBeFound.class));
		assertTrue(classes.contains(MemberClassToBeFound.class));
	}

	@Test
	void findAllClassesInPackageWithinJarFile() throws Exception {
		URL jarfile = getClass().getResource("/jartest.jar");

		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { jarfile })) {
			ClasspathScanner classpathScanner = new ClasspathScanner(() -> classLoader,
				ReflectionUtils::tryToLoadClass);

			List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.platform.jartest.included",
				allClasses);
			assertThat(classes).hasSize(2);
			List<String> classNames = classes.stream().map(Class::getSimpleName).collect(Collectors.toList());
			assertTrue(classNames.contains("Included"));
			assertTrue(classNames.contains("RecursivelyIncluded"));
		}
	}

	@Test
	void scanForClassesInDefaultPackage() {
		ClassFilter classFilter = ClassFilter.of(this::inDefaultPackage);
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("", classFilter);

		assertThat(classes.size()).as("number of classes found in default package").isGreaterThanOrEqualTo(1);
		assertTrue(classes.stream().allMatch(this::inDefaultPackage));
		assertTrue(classes.stream().anyMatch(clazz -> "DefaultPackageTestCase".equals(clazz.getName())));
	}

	@Test
	void scanForClassesInPackageWithFilter() {
		ClassFilter thisClassOnly = ClassFilter.of(clazz -> clazz == ClasspathScannerTests.class);
		List<Class<?>> classes = classpathScanner.scanForClassesInPackage("org.junit.platform.commons", thisClassOnly);
		assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	void scanForClassesInPackageForNullBasePackage() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInPackage(null, allClasses));
	}

	@Test
	void scanForClassesInPackageForWhitespaceBasePackage() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInPackage("    ", allClasses));
	}

	@Test
	void scanForClassesInPackageForNullClassFilter() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInPackage("org.junit.platform.commons", null));
	}

	@Test
	void scanForClassesInPackageWhenIOExceptionOccurs() {
		ClasspathScanner scanner = new ClasspathScanner(ThrowingClassLoader::new, ReflectionUtils::tryToLoadClass);
		List<Class<?>> classes = scanner.scanForClassesInPackage("org.junit.platform.commons", allClasses);
		assertThat(classes).isEmpty();
	}

	@Test
	void scanForClassesInPackageOnlyLoadsClassesThatAreIncludedByTheClassNameFilter() {
		Predicate<String> classNameFilter = name -> ClasspathScannerTests.class.getName().equals(name);
		ClassFilter classFilter = ClassFilter.of(classNameFilter, type -> true);

		classpathScanner.scanForClassesInPackage("org.junit.platform.commons", classFilter);

		assertThat(loadedClasses).containsExactly(ClasspathScannerTests.class);
	}

	@Test
	void findAllClassesInClasspathRoot() throws Exception {
		ClassFilter thisClassOnly = ClassFilter.of(clazz -> clazz == ClasspathScannerTests.class);
		URI root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, thisClassOnly);
		assertSame(ClasspathScannerTests.class, classes.get(0));
	}

	@Test
	void findAllClassesInDefaultPackageInClasspathRoot() throws Exception {
		ClassFilter classFilter = ClassFilter.of(this::inDefaultPackage);
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(getTestClasspathRoot(), classFilter);

		assertEquals(1, classes.size(), "number of classes found in default package");
		Class<?> testClass = classes.get(0);
		assertTrue(inDefaultPackage(testClass));
		assertEquals("DefaultPackageTestCase", testClass.getName());
	}

	@Test
	void doesNotLoopInfinitelyWithCircularSymlinks(@TempDir Path tempDir) throws Exception {

		// Abort if running on Microsoft Windows since we are testing symbolic links
		assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));

		Path directory = Files.createDirectory(tempDir.resolve("directory"));
		Path symlink1 = Files.createSymbolicLink(tempDir.resolve("symlink1"), directory);
		Files.createSymbolicLink(directory.resolve("symlink2"), symlink1);

		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(symlink1.toUri(), allClasses);

		assertThat(classes).isEmpty();
	}

	private boolean inDefaultPackage(Class<?> clazz) {
		// OpenJDK returns NULL for the default package.
		Package pkg = clazz.getPackage();
		return pkg == null || "".equals(clazz.getPackage().getName());
	}

	@Test
	void findAllClassesInClasspathRootWithFilter() throws Exception {
		URI root = getTestClasspathRoot();
		List<Class<?>> classes = classpathScanner.scanForClassesInClasspathRoot(root, allClasses);

		assertThat(classes.size()).isGreaterThanOrEqualTo(20);
		assertTrue(classes.contains(ClasspathScannerTests.class));
	}

	@Test
	void findAllClassesInClasspathRootForNullRoot() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInClasspathRoot(null, allClasses));
	}

	@Test
	void findAllClassesInClasspathRootForNonExistingRoot() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInClasspathRoot(Paths.get("does_not_exist").toUri(), allClasses));
	}

	@Test
	void findAllClassesInClasspathRootForNullClassFilter() {
		assertThrows(PreconditionViolationException.class,
			() -> classpathScanner.scanForClassesInClasspathRoot(getTestClasspathRoot(), null));
	}

	@Test
	void onlyLoadsClassesInClasspathRootThatAreIncludedByTheClassNameFilter() throws Exception {
		ClassFilter classFilter = ClassFilter.of(name -> ClasspathScannerTests.class.getName().equals(name),
			type -> true);
		URI root = getTestClasspathRoot();

		classpathScanner.scanForClassesInClasspathRoot(root, classFilter);

		assertThat(loadedClasses).containsExactly(ClasspathScannerTests.class);
	}

	private URI getTestClasspathRoot() throws Exception {
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
		return location.toURI();
	}

	class MemberClassToBeFound {
	}

	static class NestedClassToBeFound {
	}

	static class ClassForMalformedClassNameSimulation {
	}

	static class ClassForOtherInternalErrorSimulation {
	}

	static class ClassForGenericRuntimeExceptionSimulation {
	}

	static class ClassForOutOfMemoryErrorSimulation {
	}

	private static class ThrowingClassLoader extends ClassLoader {

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			throw new IOException("Demo I/O error");
		}
	}

}
