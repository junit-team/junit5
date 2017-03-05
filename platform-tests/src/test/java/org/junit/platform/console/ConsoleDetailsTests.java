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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

	@TestFactory
	@DisplayName("Basic tests and annotations usage")
	List<DynamicTest> basic() throws Exception {
		return build(Basic.class, Files.createTempDirectory("console-details-basic-"));
	}

	private List<DynamicTest> build(Class<?> containerClass, Path temp) {
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
					tests.add(DynamicTest.dynamicTest(displayName, new Executor(temp, dirName, outName, args)));
				}
			}
		}
		return tests;
	}

	private static class Executor implements Executable {
		private final Path temp;
		private final String dirName;
		private final String outName;
		private final String[] args;

		private Executor(Path temp, String dirName, String outName, String... args) {
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
				if (temp != null) {
					Path path = Files.write(temp.resolve(outName), result.out.getBytes(UTF_8));
					System.out.println("Wrote " + path);
					return;
				}
				fail("could not load resource: " + dirName + "/" + outName);
			}

			List<String> expectedLines = Files.readAllLines(Paths.get(url.toURI()), UTF_8);
			int max = expectedLines.size();
			List<String> actualLines = Arrays.asList(result.out.split("\\R", max + 1)).subList(0, max);

			for (int i = 0; i < max; i++) {
				String actualLine = actualLines.get(i);
				String expectedLine = expectedLines.get(i);
				if (expectedLine.equals(actualLine)) {
					continue;
				}
				int lineNumber = i + 1;
				Supplier<String> messageSupplier = () -> {
					StringBuilder builder = new StringBuilder();
					builder.append("\nline number   = ").append(lineNumber);
					builder.append("\nactual string = ").append(actualLine);
					builder.append("\nregex pattern = ").append(expectedLine);
					return builder.toString();
				};
				try {
					Pattern pattern = Pattern.compile(expectedLine);
					Matcher matcher = pattern.matcher(actualLine);
					assertTrue(matcher.matches(), messageSupplier);
				}
				catch (PatternSyntaxException exception) {
					fail("expected line #" + lineNumber + " is not a valid regex pattern" + messageSupplier.get(),
						exception);
				}
			}
		}
	}
}
