/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterEachMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeEachMethods;
import static org.junit.platform.commons.util.FunctionUtils.where;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Unit tests for {@link LifecycleMethodUtils}.
 *
 * @since 5.0
 */
class LifecycleMethodUtilsTests {

	List<DiscoveryIssue> discoveryIssues = new ArrayList<>();

	@Test
	void findNonVoidBeforeAllMethodsWithStandardLifecycle() throws Exception {
		var methods = findBeforeAllMethods(TestCaseWithNonVoidLifecyleMethods.class, true, discoveryIssues::add);
		assertThat(methods).isEmpty();

		var expectedIssue = DiscoveryIssue.builder(Severity.ERROR,
			"@BeforeAll method 'java.lang.Double org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.cc()' must not return a value.") //
				.source(MethodSource.from(TestCaseWithNonVoidLifecyleMethods.class.getDeclaredMethod("cc"))) //
				.build();
		assertThat(discoveryIssues).containsExactly(expectedIssue);
	}

	@Test
	void findNonVoidAfterAllMethodsWithStandardLifecycle() throws Exception {
		var methods = findAfterAllMethods(TestCaseWithNonVoidLifecyleMethods.class, true, discoveryIssues::add);
		assertThat(methods).isEmpty();

		var expectedIssue = DiscoveryIssue.builder(Severity.ERROR,
			"@AfterAll method 'java.lang.String org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.dd()' must not return a value.") //
				.source(MethodSource.from(TestCaseWithNonVoidLifecyleMethods.class.getDeclaredMethod("dd"))) //
				.build();
		assertThat(discoveryIssues).containsExactly(expectedIssue);
	}

	@Test
	void findNonVoidBeforeEachMethodsWithStandardLifecycle() throws Exception {
		var methods = findBeforeEachMethods(TestCaseWithNonVoidLifecyleMethods.class, discoveryIssues::add);
		assertThat(methods).isEmpty();

		var expectedIssue = DiscoveryIssue.builder(Severity.ERROR,
			"@BeforeEach method 'java.lang.String org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.aa()' must not return a value.") //
				.source(MethodSource.from(TestCaseWithNonVoidLifecyleMethods.class.getDeclaredMethod("aa"))) //
				.build();
		assertThat(discoveryIssues).containsExactly(expectedIssue);
	}

	@Test
	void findNonVoidAfterEachMethodsWithStandardLifecycle() throws Exception {
		var methods = findAfterEachMethods(TestCaseWithNonVoidLifecyleMethods.class, discoveryIssues::add);
		assertThat(methods).isEmpty();

		var expectedIssue = DiscoveryIssue.builder(Severity.ERROR,
			"@AfterEach method 'int org.junit.jupiter.engine.descriptor.TestCaseWithNonVoidLifecyleMethods.bb()' must not return a value.") //
				.source(MethodSource.from(TestCaseWithNonVoidLifecyleMethods.class.getDeclaredMethod("bb"))) //
				.build();
		assertThat(discoveryIssues).containsExactly(expectedIssue);
	}

	@Test
	void findBeforeEachMethodsWithStandardLifecycle() {
		List<Method> methods = findBeforeEachMethods(TestCaseWithStandardLifecycle.class, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactlyInAnyOrder("nine", "ten");
		assertThat(discoveryIssues).isEmpty();
	}

	@Test
	void findAfterEachMethodsWithStandardLifecycle() {
		List<Method> methods = findAfterEachMethods(TestCaseWithStandardLifecycle.class, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactlyInAnyOrder("eleven", "twelve");
	}

	@Test
	void findBeforeAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
		List<Method> methods = findBeforeAllMethods(TestCaseWithStandardLifecycle.class, false, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactly("one");
		assertThat(discoveryIssues).isEmpty();
	}

	@Test
	void findBeforeAllMethodsWithStandardLifecycleAndRequiringStatic() throws Exception {
		var methods = findBeforeAllMethods(TestCaseWithStandardLifecycle.class, true, discoveryIssues::add);
		assertThat(methods).isEmpty();

		var expectedIssue = DiscoveryIssue.builder(Severity.ERROR,
			"@BeforeAll method 'void org.junit.jupiter.engine.descriptor.TestCaseWithStandardLifecycle.one()' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).") //
				.source(MethodSource.from(TestCaseWithStandardLifecycle.class.getDeclaredMethod("one"))) //
				.build();
		assertThat(discoveryIssues).containsExactly(expectedIssue);
	}

	@Test
	void findBeforeAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
		List<Method> methods = findBeforeAllMethods(TestCaseWithLifecyclePerClass.class, false, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactlyInAnyOrder("three", "four");
		assertThat(discoveryIssues).isEmpty();
	}

	@Test
	void findAfterAllMethodsWithStandardLifecycleAndWithoutRequiringStatic() {
		List<Method> methods = findAfterAllMethods(TestCaseWithStandardLifecycle.class, false, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactlyInAnyOrder("five", "six");
		assertThat(discoveryIssues).isEmpty();
	}

	@Test
	void findAfterAllMethodsWithStandardLifecycleAndRequiringStatic() {
		var methods = findAfterAllMethods(TestCaseWithStandardLifecycle.class, true, discoveryIssues::add);
		assertThat(methods).isEmpty();

		assertThat(discoveryIssues) //
				.filteredOn(where(DiscoveryIssue::severity, isEqual(Severity.ERROR))) //
				.isNotEmpty();
	}

	@Test
	void findAfterAllMethodsWithLifeCyclePerClassAndRequiringStatic() {
		List<Method> methods = findAfterAllMethods(TestCaseWithLifecyclePerClass.class, false, discoveryIssues::add);

		assertThat(namesOf(methods)).containsExactlyInAnyOrder("seven", "eight");
	}

	private static List<String> namesOf(List<Method> methods) {
		return methods.stream().map(Method::getName).toList();
	}

}

class TestCaseWithStandardLifecycle {

	@SuppressWarnings("JUnitMalformedDeclaration")
	@BeforeAll
	void one() {
	}

	@BeforeEach
	void nine() {
	}

	@BeforeEach
	void ten() {
	}

	@AfterEach
	void eleven() {
	}

	@AfterEach
	void twelve() {
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@AfterAll
	void five() {
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@AfterAll
	void six() {
	}

}

@TestInstance(Lifecycle.PER_CLASS)
class TestCaseWithLifecyclePerClass {

	@BeforeAll
	void three() {
	}

	@BeforeAll
	void four() {
	}

	@AfterAll
	void seven() {
	}

	@AfterAll
	void eight() {
	}

}

class TestCaseWithNonVoidLifecyleMethods {

	@SuppressWarnings("JUnitMalformedDeclaration")
	@BeforeEach
	String aa() {
		return null;
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@AfterEach
	int bb() {
		return 1;
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@BeforeAll
	Double cc() {
		return null;
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@AfterAll
	String dd() {
		return "";
	}

}
