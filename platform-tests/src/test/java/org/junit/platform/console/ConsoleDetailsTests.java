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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.console.options.Details;
import org.junit.platform.console.tasks.Theme;

/**
 * @since 1.0
 */
class ConsoleDetailsTests {

	static class Container {

		@Test
		@Expect(details = Details.TREE, theme = Theme.UNICODE, charsetName = "UTF-8", //
				lines = { "╷", //
						"└─ JUnit Jupiter ✔", //
						"   └─ ConsoleDetailsTests$Container ✔", //
						"      └─ failWithSingleLineMessage() ✘ single line fail message" //
				})
		@Expect(details = Details.TREE, theme = Theme.ASCII, charsetName = "ISO_8859_1", //
				lines = { ".", //
						"'-- JUnit Jupiter [OK]", //
						"  '-- ConsoleDetailsTests$Container [OK]", //
						"    '-- failWithSingleLineMessage() [X] single line fail message" //
				})
		@Expect(details = Details.FLAT, theme = Theme.UNICODE, charsetName = "UTF-8", //
				lines = { //
						"Test execution started. Number of static tests: 1", //
						"Started:     JUnit Jupiter ([engine:junit-jupiter])", //
						"Started:     ConsoleDetailsTests$Container ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container])", //
						"Started:     failWithSingleLineMessage() ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container]/[method:failWithSingleLineMessage()])", //
						"Finished:    failWithSingleLineMessage() ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container]/[method:failWithSingleLineMessage()])", //
						"             => Exception: org.opentest4j.AssertionFailedError: single line fail message" //
				})
		void failWithSingleLineMessage() {
			Assertions.fail("single line fail message");
		}

		@Test
		@Expect(details = Details.TREE, theme = Theme.UNICODE, charsetName = "UTF-8", //
				lines = { "╷", //
						"└─ JUnit Jupiter ✔", //
						"   └─ ConsoleDetailsTests$Container ✔", //
						"      └─ failWithMultiLineMessage() ✘ multi", //
						"line", //
						"fail", //
						"message" //
				})
		@Expect(details = Details.TREE, theme = Theme.ASCII, charsetName = "ISO_8859_1", //
				lines = { ".", //
						"'-- JUnit Jupiter [OK]", //
						"  '-- ConsoleDetailsTests$Container [OK]", //
						"    '-- failWithMultiLineMessage() [X] multi", //
						"line", //
						"fail", //
						"message" //
				})
		@Expect(details = Details.FLAT, theme = Theme.UNICODE, charsetName = "UTF-8", //
				lines = { //
						"Test execution started. Number of static tests: 1", //
						"Started:     JUnit Jupiter ([engine:junit-jupiter])", //
						"Started:     ConsoleDetailsTests$Container ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container])", //
						"Started:     failWithMultiLineMessage() ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container]/[method:failWithMultiLineMessage()])", //
						"Finished:    failWithMultiLineMessage() ([engine:junit-jupiter]/[class:org.junit.platform.console.ConsoleDetailsTests$Container]/[method:failWithMultiLineMessage()])", //
						"             => Exception: org.opentest4j.AssertionFailedError: multi", //
						"             line", //
						"             fail", //
						"             message" //
				})
		void failWithMultiLineMessage() {
			Assertions.fail("multi\nline\nfail\nmessage");
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Expectations {
		Expect[] value();
	}

	@Repeatable(Expectations.class)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Expect {
		Details details();

		Theme theme();

		String charsetName();

		String[] lines();
	}

	@TestFactory
	List<DynamicTest> foreach() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Method method : AnnotationUtils.findAnnotatedMethods(Container.class, Test.class, HierarchyDown)) {
			for (Expect expect : method.getAnnotationsByType(Expect.class)) {
				String displayName = method.getName() + " " + expect.details() + " " + expect.charsetName();
				DynamicTest test = DynamicTest.dynamicTest(displayName, () -> {
					String[] args = { //
							"--include-engine", "junit-jupiter", //
							"--details", expect.details().name(), //
							"--disable-ansi-colors", "true", //
							"--include-classname", ".*", //
							"--select-method", method.getDeclaringClass().getName() + "#" + method.getName() //
					};
					Theme.setDefaultTheme(expect.theme());
					ConsoleLauncherWrapper wrapper = new ConsoleLauncherWrapper(Charset.forName(expect.charsetName()));
					ConsoleLauncherWrapperResult result = wrapper.execute(Optional.empty(), args);
					String expected = String.join(System.lineSeparator(), expect.lines());
					int max = expect.lines().length;
					List<String> actualLines = Arrays.asList(result.out.split("\\R", max + 1)).subList(0, max);
					String actual = String.join(System.lineSeparator(), actualLines);
					assertEquals(expected, actual, result.out);
				});
				tests.add(test);
			}
		}
		return tests;
	}

}
