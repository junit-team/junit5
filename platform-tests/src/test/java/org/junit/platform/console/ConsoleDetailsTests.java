/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.Details;
import org.junit.platform.console.options.Theme;

/**
 * @since 1.0
 */
class ConsoleDetailsTests {

	@DisplayName("Basic")
	static class Basic {

		@Test
		void empty() {
		}

		@Test
		@DisplayName(".oO fancy display name Oo.")
		void changeDisplayName() {
		}

	}

	@DisplayName("Skip")
	static class Skip {

		@Test
		@Disabled("single line skip reason")
		void skipWithSingleLineReason() {
		}

		@Test
		@Disabled("multi\nline\nfail\nmessage")
		void skipWithMultiLineMessage() {
		}

	}

	@DisplayName("Fail")
	static class Fail {

		@Test
		void failWithSingleLineMessage() {
			fail("single line fail message");
		}

		@Test
		void failWithMultiLineMessage() {
			fail("multi\nline\nfail\nmessage");
		}

	}

	@TestFactory
	@DisplayName("Basic tests and annotations usage")
	List<DynamicTest> basic() {
		return scanContainerClassAndCreateDynamicTests(Basic.class);
	}

	@TestFactory
	@DisplayName("Skipped and disabled tests")
	List<DynamicTest> skipped() {
		return scanContainerClassAndCreateDynamicTests(Skip.class);
	}

	@TestFactory
	@DisplayName("Failed tests")
	List<DynamicTest> failed() {
		return scanContainerClassAndCreateDynamicTests(Fail.class);
	}

	private List<DynamicTest> scanContainerClassAndCreateDynamicTests(Class<?> containerClass) {
		String containerName = containerClass.getSimpleName();
		List<DynamicTest> tests = new ArrayList<>();
		for (Method method : AnnotationUtils.findAnnotatedMethods(containerClass, Test.class, HierarchyDown)) {
			for (Details details : Details.values()) {
				for (Theme theme : Theme.values()) {
					String caption = containerName + "-" + method.getName() + "-" + details + "-" + theme;
					String dirName = "console/details/" + containerName.toLowerCase();
					String outName = caption + ".out.txt";
					String[] args = { //
							"--include-engine", "junit-jupiter", //
							"--details", details.name(), //
							"--details-theme", theme.name(), //
							"--disable-ansi-colors", "true", //
							"--include-classname", containerClass.getCanonicalName(), //
							"--select-method", ReflectionUtils.getFullyQualifiedMethodName(method) //
					};
					String displayName = method.getName() + " theme=" + theme.name() + "() details=" + details.name();
					tests.add(DynamicTest.dynamicTest(displayName, new Runner(dirName, outName, args)));
				}
			}
		}
		return tests;
	}

	private static class Runner implements Executable {
		private final String dirName;
		private final String outName;
		private final String[] args;

		private Runner(String dirName, String outName, String... args) {
			this.dirName = dirName;
			this.outName = outName;
			this.args = args;
		}

		@Override
		public void execute() throws Throwable {
			ConsoleLauncherWrapper wrapper = new ConsoleLauncherWrapper();
			ConsoleLauncherWrapperResult result = wrapper.execute(Optional.empty(), args);

			Optional<URL> optionalUrl = getResourceUrl(dirName + "/" + outName);
			if (!optionalUrl.isPresent()) {
				if (Boolean.getBoolean("org.junit.platform.console.ConsoleDetailsTests.writeResultOut")) {
					// do not use Files.createTempDirectory(prefix) as we want one folder for one container
					Path temp = Paths.get(System.getProperty("java.io.tmpdir"), dirName.replace('/', '-'));
					Files.createDirectories(temp);
					Path path = Files.write(temp.resolve(outName), result.out.getBytes(UTF_8));
					System.out.println("wrote resource " + path);
					return;
				}
				fail("could not load resource: " + dirName + "/" + outName);
				return;
			}

			URL url = optionalUrl.orElseThrow(AssertionError::new);
			Path path = Paths.get(url.toURI());
			assumeTrue(path.toFile().exists(), "path does not exist: " + path);
			assumeTrue(path.toFile().canRead(), "can not read: " + path);

			List<String> expectedLines = Files.readAllLines(path, UTF_8);
			List<String> actualLines = asList(result.out.split("\\R"));

			int expectedSize = expectedLines.size();
			int actualSize = actualLines.size();

			if (expectedSize > actualSize) {
				assertEquals(String.join(System.lineSeparator(), expectedLines), result.out, //
					"more lines expected [" + expectedSize + "] then actually produced [" + actualSize + "]");
			}

			if (expectedSize == actualSize) {
				for (int i = 0; i < expectedSize; i++) {
					assertMatches(expectedLines.get(i), actualLines.get(i), i, i);
				}
				return;
			}

			assert expectedSize < actualSize;

			for (int e = 0, a = 0; e < expectedSize && a < actualSize; e++, a++) {
				String expectedLine = expectedLines.get(e);
				String actualLine = actualLines.get(a);
				// trivial case: take the fast path when both lines are equal
				if (expectedLine.equals(actualLine)) {
					continue;
				}
				// "S T A C K T R A C E" marker found as expected line: fast forward actual line until next match
				if (expectedLine.equals("S T A C K T R A C E")) {
					int nextExpectedIndex = e + 1;
					if (nextExpectedIndex >= expectedSize) {
						// trivial case: marker was last line in expected list
						return;
					}
					expectedLine = expectedLines.get(nextExpectedIndex);
					int ahead = a;
					while (!matches(expectedLine, actualLine, false)) {
						actualLine = actualLines.get(ahead++);
						if (ahead >= actualSize) {
							fail("ran out of bounds");
						}
					}
					a = ahead - 2; // "side-effect" assignment to for-loop variable on purpose
					continue;
				}
				// now, assert equality of expect and actual line
				assertMatches(expectedLine, actualLine, e, a);
			}

		}

		private void assertMatches(String expectedLine, String actualLine, int expectedIndex, int actualIndex) {
			assertTrue(matches(expectedLine, actualLine), //
				() -> String.format("%nexpected:%d = %s%nactual:%d = %s", //
					expectedIndex, expectedLine, actualIndex, actualLine));
		}

		private boolean matches(String expectedLine, String actualLine) {
			return matches(expectedLine, actualLine, true);
		}

		private boolean matches(String expectedLine, String actualLine, boolean failOnPatternSyntaxException) {
			if (expectedLine.equals(actualLine)) {
				return true;
			}
			try {
				Pattern pattern = Pattern.compile(expectedLine);
				Matcher matcher = pattern.matcher(actualLine);
				return matcher.matches();
			}
			catch (PatternSyntaxException exception) {
				if (failOnPatternSyntaxException) {
					fail("expected line is not a valid regex pattern" + expectedLine, exception);
				}
				return false;
			}
		}

	}

	private static Optional<URL> getResourceUrl(String resource) throws Exception {
		List<ClassLoader> classLoaders = new ArrayList<>();
		classLoaders.add(Thread.currentThread().getContextClassLoader());
		classLoaders.add(ConsoleDetailsTests.class.getClassLoader());
		classLoaders.add(ClassLoader.getSystemClassLoader());
		for (ClassLoader classLoader : classLoaders) {
			URL url = classLoader.getResource(resource);
			if (url != null) {
				return Optional.of(url);
			}
		}
		// now scan and find...
		Optional<Path> path = Files.find(Paths.get("build/resources/test"), 9,
			(p, bfa) -> p.endsWith(resource) && bfa.isRegularFile()).findAny();
		if (path.isPresent()) {
			return Optional.of(path.get().toAbsolutePath().toUri().toURL());
		}
		return Optional.empty();
	}
}
