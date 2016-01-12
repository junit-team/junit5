/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.dsl;

import static org.junit.gen5.engine.ClassFilters.classNameMatches;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import org.junit.gen5.engine.EngineFilter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;

/**
 * The {@code TestPlanSpecificationBuilder} provides a light-weight DSL for
 * generating a {@link TestPlanSpecification}.
 *
 * <p>Example:
 *
 * <pre>
 *   testPlan()
 *     .withElements(
 *       packageName("org.junit.gen5"),
 *       packageName("com.junit.samples"),
 *       testClass(TestDescriptorTests.class),
 *       testClassByName("com.junit.samples.SampleTestCase"),
 *       testMethod("com.junit.samples.SampleTestCase", "test2"),
 *       testMethod(TestDescriptorTests.class, "test1"),
 *       testMethod(TestDescriptorTests.class, "test1"),
 *       testMethod(TestDescriptorTests.class, "testWithParams", ParameterType.class),
 *       testMethod(TestDescriptorTests.class, testMethod),
 *       path("/my/local/path1"),
 *       path("/my/local/path2"),
 *       uniqueId("unique-id-1"),
 *       uniqueId("unique-id-2")
 *     )
 *     .withFilters(
 *       engineIds("junit5"),
 *       classNamePattern("org.junit.gen5.tests"),
 *       classNamePattern("org.junit.sample"),
 *       tagsIncluded("Fast"),
 *       tagsExcluded("Slow")
 *     )
 *     .withOptions(
 *     	 option("key1", someValueObject1),
 *     	 option("key2", someValueObject2),
 *     	 option("key3", someValueObject3)
 *     )
 *   ).build();
 * </pre>
 */
public final class TestPlanSpecificationBuilder {
	private List<TestPlanSpecificationElement> specElements = new LinkedList<>();
	private List<EngineFilter> engineFilters = new LinkedList<>();
	private List<Predicate<TestDescriptor>> descriptorFilters = new LinkedList<>();

	public static TestPlanSpecificationBuilder testPlanSpecification() {
		return new TestPlanSpecificationBuilder();
	}

	public TestPlanSpecificationBuilder withElements(TestPlanSpecificationElement... elements) {
		if (elements != null) {
			withElements(Arrays.asList(elements));
		}
		return this;
	}

	public TestPlanSpecificationBuilder withElements(List<TestPlanSpecificationElement> elements) {
		if (elements != null) {
			this.specElements.addAll(elements);
		}
		return this;
	}

	public TestPlanSpecificationBuilder withEngineFilters(EngineFilter... filters) {
		if (filters != null) {
			this.engineFilters.addAll(Arrays.asList(filters));
		}
		return this;
	}

	public TestPlanSpecificationBuilder withDescriptionFilters(Predicate<TestDescriptor>... filters) {
		if (filters != null) {
			this.descriptorFilters.addAll(Arrays.asList(filters));
		}
		return this;
	}

	public TestPlanSpecification build() {
		TestPlanSpecification testPlanSpecification = new TestPlanSpecification();
		testPlanSpecification.addElements(this.specElements);
		testPlanSpecification.addEngineFilters(this.engineFilters);
		return testPlanSpecification;
	}
}
