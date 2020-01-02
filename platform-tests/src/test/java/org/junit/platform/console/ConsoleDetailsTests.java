/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.getFullyQualifiedMethodName;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.console.options.Details;
import org.junit.platform.console.options.Theme;

/**
 * @since 1.0
 */
class ConsoleDetailsTests {

	@TestFactory
	@DisplayName("Basic tests and annotations usage")
	List<DynamicNode> basic() {
		return scanContainerClassAndCreateDynamicTests(BasicTestCase.class);
	}

	@TestFactory
	@DisplayName("Skipped and disabled tests")
	List<DynamicNode> skipped() {
		return scanContainerClassAndCreateDynamicTests(SkipTestCase.class);
	}

	@TestFactory
	@DisplayName("Failed tests")
	List<DynamicNode> failed() {
		return scanContainerClassAndCreateDynamicTests(FailTestCase.class);
	}

	@TestFactory
	@DisplayName("Tests publishing report entries")
	List<DynamicNode> reports() {
		return scanContainerClassAndCreateDynamicTests(ReportTestCase.class);
	}

	private List<DynamicNode> scanContainerClassAndCreateDynamicTests(Class<?> containerClass) {
		String containerName = containerClass.getSimpleName().replace("TestCase", "");
		// String containerName = containerClass.getSimpleName();
		List<DynamicNode> nodes = new ArrayList<>();
		Map<Details, List<DynamicTest>> map = new EnumMap<>(Details.class);
		for (Method method : findMethods(containerClass, m -> m.isAnnotationPresent(Test.class))) {
			String methodName = method.getName();
			Class<?>[] types = method.getParameterTypes();
			for (Details details : Details.values()) {
				List<DynamicTest> tests = map.computeIfAbsent(details, key -> new ArrayList<>());
				for (Theme theme : Theme.values()) {
					String caption = containerName + "-" + methodName + "-" + details + "-" + theme;
					String[] args = { //
							"--include-engine", "junit-jupiter", //
							"--details", details.name(), //
							"--details-theme", theme.name(), //
							"--disable-ansi-colors", //
							"--disable-banner", //
							"--include-classname", containerClass.getCanonicalName(), //
							"--select-method", getFullyQualifiedMethodName(containerClass, methodName, types) //
					};
					String displayName = methodName + "() " + theme.name();
					String dirName = "console/details/" + containerName.toLowerCase();
					String outName = caption + ".out.txt";
					Runner runner = new Runner(dirName, outName, args);
					URI source = toUri(dirName, outName).orElse(null);
					tests.add(dynamicTest(displayName, source, runner));
				}
			}
		}
		URI source = new File("src/test/resources/console/details").toURI();
		map.forEach((details, tests) -> nodes.add(dynamicContainer(details.name(), source, tests.stream())));
		return nodes;
	}

	@DisplayName("Basic")
	static class BasicTestCase {

		@Test
		void empty() {
		}

		@Test
		@DisplayName(".oO fancy display name Oo.")
		void changeDisplayName() {
		}

	}

	@DisplayName("Skip")
	static class SkipTestCase {

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
	static class FailTestCase {

		@Test
		void failWithSingleLineMessage() {
			fail("single line fail message");
		}

		@Test
		void failWithMultiLineMessage() {
			fail("multi\nline\nfail\nmessage");
		}

	}

	@DisplayName("Report")
	static class ReportTestCase {

		@Test
		void reportSingleMessage(TestReporter reporter) {
			reporter.publishEntry("foo");
		}

		@Test
		void reportMultipleMessages(TestReporter reporter) {
			reporter.publishEntry("foo");
			reporter.publishEntry("bar");
		}

		@Test
		void reportSingleEntryWithSingleMapping(TestReporter reporter) {
			reporter.publishEntry("foo", "bar");
		}

		@Test
		void reportMultiEntriesWithSingleMapping(TestReporter reporter) {
			reporter.publishEntry("foo", "bar");
			reporter.publishEntry("far", "boo");
		}

		@Test
		void reportMultiEntriesWithMultiMappings(TestReporter reporter) {
			Map<String, String> values = new LinkedHashMap<>();
			values.put("user name", "dk38");
			values.put("award year", "1974");
			reporter.publishEntry(values);
			reporter.publishEntry("single", "mapping");
			Map<String, String> more = new LinkedHashMap<>();
			more.put("user name", "st77");
			more.put("award year", "1977");
			more.put("last seen", "2001");
			reporter.publishEntry(more);
		}

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

			Optional<URI> optionalUri = toUri(dirName, outName);
			if (!optionalUri.isPresent()) {
				if (Boolean.getBoolean("org.junit.platform.console.ConsoleDetailsTests.writeResultOut")) {
					// do not use Files.createTempDirectory(prefix) as we want one folder for one container
					Path temp = Paths.get(System.getProperty("java.io.tmpdir"), dirName.replace('/', '-'));
					Files.createDirectories(temp);
					Path path = Files.write(temp.resolve(outName), result.out.getBytes(UTF_8));
					assumeTrue(false,
						format("resource `%s` not found\nwrote console stdout to: %s/%s", dirName, outName, path));
				}
				fail("could not load resource named `" + dirName + "/" + outName + "`");
			}

			Path path = Paths.get(optionalUri.get());
			assumeTrue(Files.exists(path), "path does not exist: " + path);
			assumeTrue(Files.isReadable(path), "can not read: " + path);

			List<String> expectedLines = Files.readAllLines(path, UTF_8);
			List<String> actualLines = asList(result.out.split("\\R"));

			assertLinesMatch(expectedLines, actualLines);
		}
	}

	static Optional<URI> toUri(String dirName, String outName) {
		String resourceName = dirName + "/" + outName;
		URL url = ConsoleDetailsTests.class.getClassLoader().getResource(resourceName);
		if (url == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(url.toURI());
		}
		catch (URISyntaxException e) {
			return Optional.empty();
		}
	}

}
