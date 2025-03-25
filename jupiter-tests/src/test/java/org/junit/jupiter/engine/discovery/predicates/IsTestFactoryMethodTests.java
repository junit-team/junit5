/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * Unit tests for {@link IsTestFactoryMethod}.
 *
 * @since 5.0
 */
class IsTestFactoryMethodTests {

	final List<DiscoveryIssue> discoveryIssues = new ArrayList<>();
	final Predicate<Method> isTestFactoryMethod = new IsTestFactoryMethod(discoveryIssues::add);

	@Test
	void factoryMethodReturningCollectionOfDynamicTests() {
		assertThat(isTestFactoryMethod).accepts(method("dynamicTestsFactory"));
	}

	@Test
	void bogusFactoryMethodReturningVoid() {
		var method = method("bogusVoidFactory");

		assertThat(isTestFactoryMethod).rejects(method);

		var issue = getOnlyElement(discoveryIssues);
		assertThat(issue.severity()).isEqualTo(DiscoveryIssue.Severity.WARNING);
		assertThat(issue.message()).isEqualTo(
			"@TestFactory method '%s' must return a value. It will be not be executed.", method.toGenericString());
		assertThat(issue.source()).contains(MethodSource.from(method));
	}

	// TODO [#949] Enable test once IsTestFactoryMethod properly checks return type.
	@Disabled("Disabled until IsTestFactoryMethod properly checks return type")
	@Test
	void bogusFactoryMethodReturningObject() {
		assertThat(isTestFactoryMethod).rejects(method("bogusObjectFactory"));
	}

	// TODO [#949] Enable test once IsTestFactoryMethod properly checks return type.
	@Disabled("Disabled until IsTestFactoryMethod properly checks return type")
	@Test
	void bogusFactoryMethodReturningCollectionOfStrings() {
		assertThat(isTestFactoryMethod).rejects(method("bogusStringsFactory"));
	}

	private static Method method(String name) {
		return ReflectionSupport.findMethod(ClassWithTestFactoryMethods.class, name).orElseThrow();
	}

	@SuppressWarnings("unused")
	private static class ClassWithTestFactoryMethods {

		@TestFactory
		Collection<DynamicTest> dynamicTestsFactory() {
			return new ArrayList<>();
		}

		@TestFactory
		void bogusVoidFactory() {
		}

		@TestFactory
		Object bogusObjectFactory() {
			return new Object();
		}

		@TestFactory
		Collection<String> bogusStringsFactory() {
			return new ArrayList<>();
		}

	}

}
