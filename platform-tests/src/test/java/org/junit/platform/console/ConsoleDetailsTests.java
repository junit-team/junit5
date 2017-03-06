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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
	List<DynamicTest> basic() throws Exception {
		return build(Basic.class, Files.createTempDirectory("console-details-basic-"));
	}

	@TestFactory
	@DisplayName("Skipped and disabled tests")
	List<DynamicTest> skipped() throws Exception {
		return build(Skip.class, Files.createTempDirectory("console-details-skip-"));
	}

	@TestFactory
	@DisplayName("Failed tests")
	List<DynamicTest> failed() throws Exception {
		return build(Fail.class, Files.createTempDirectory("console-details-fail-"));
	}

	private List<DynamicTest> build(Class<?> containerClass, Path temp) throws Exception {
		String containerName = containerClass.getSimpleName();
		List<DynamicTest> tests = new ArrayList<>();
		for (Method method : AnnotationUtils.findAnnotatedMethods(containerClass, Test.class, HierarchyDown)) {
			for (Details details : Details.values()) {
				for (Theme theme : Theme.values()) {
					String caption = String.join("-", containerName, method.getName(), details.toString(),
						theme.toString());
					String dirName = "console/details/" + containerName;
					String outName = caption + ".out.txt";
					String[] args = { //
							"--include-engine", "junit-jupiter", //
							"--details", details.name(), //
							"--details-theme", theme.name(), //
							"--disable-ansi-colors", "true", //
							"--include-classname", containerClass.getCanonicalName(), //
							"--select-method", ReflectionUtils.getFullyQualifiedMethodName(method) //
					};
					String displayName = method.getName() + "() details=" + details.name() + " theme=" + theme.name();
					tests.add(DynamicTest.dynamicTest(displayName, new Runner(temp, dirName, outName, args)));
				}
			}
		}
		try {
			Files.delete(temp);
		}
		catch (DirectoryNotEmptyException ignore) {
			// ignore
		}
		return tests;
	}

	private static class Runner implements Executable {
		private final Path temp;
		private final String dirName;
		private final String outName;
		private final String[] args;

		private Runner(Path temp, String dirName, String outName, String... args) {
			this.temp = temp;
			this.dirName = dirName;
			this.outName = outName;
			this.args = args;
		}

		@Override
		public void execute() throws Throwable {
			ConsoleLauncherWrapper wrapper = new ConsoleLauncherWrapper();
			ConsoleLauncherWrapperResult result = wrapper.execute(Optional.empty(), args);

			URL url = getClass().getClassLoader().getResource(dirName + "/" + outName);
			if (url == null) {
				// if (temp != null) {
				//	Path path = Files.write(temp.resolve(outName), result.out.getBytes(UTF_8));
				//					System.out.println("Wrote " + path);
				//					return;
				// }
				fail("could not load resource: " + dirName + "/" + outName);
			}

			List<String> expectedLines = Files.readAllLines(Paths.get(url.toURI()), UTF_8);
			List<String> actualLines = Arrays.asList(result.out.split("\\R"));

			int expectedSize = expectedLines.size();
			int actualSize = actualLines.size();

			if (expectedSize > actualSize) {
				assertEquals(String.join(System.lineSeparator(), expectedLines), result.out, //
					"more lines expected [" + expectedSize + "] then actually produced [" + actualSize + "]");
			}

			if (expectedSize == actualSize) {
				for (int i = 0; i < expectedSize; i++) {
					assertTrue(compare(expectedLines.get(i), actualLines.get(i)));
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
				// "S T A C K T R A C E" marker found in expected line: fast forward actual line until next match
				if (expectedLine.equals("S T A C K T R A C E")) {
					int peekExpectedIndex = e + 1;
					if (peekExpectedIndex >= expectedSize) {
						// trivial case: marker was last line in expected list
						return;
					}
					expectedLine = expectedLines.get(peekExpectedIndex);
					int ahead = a;
					while (!compare(expectedLine, actualLine, false)) {
						actualLine = actualLines.get(ahead++);
						if (ahead >= actualSize) {
							fail("ran out of bounds");
						}
					}
					a = ahead - 2; // "side-effect" assignment to for-loop variable on purpose
					continue;
				}
				// now, assert equality of expect and actual line
				String message = "\nexpected:" + e + " = " + expectedLine + "\nactual:" + a + " = " + actualLine;
				assertTrue(compare(expectedLine, actualLine), message);
			}

		}

		private boolean compare(String expectedLine, String actualLine) {
			return compare(expectedLine, actualLine, true);
		}

		private boolean compare(String expectedLine, String actualLine, boolean failOnPatternSyntaxException) {
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

}
