/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.*;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.gen5.api.Test;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.MethodSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestTag;

public class TestPlanSpecificationCreatorTests {

	private CommandLineOptions options = new CommandLineOptions();

	@Test
	public void convertsClassArgument() {
		Class<?> testClass = getClass();
		options.setArguments(singletonList(testClass.getName()));

		TestPlanSpecification specification = convert();

		assertThat(specification.getClasses()).containsOnly(testClass);
	}

	@Test
	public void convertsMethodArgument() throws Exception {
		Class<?> testClass = getClass();
		// TODO #39 Use @TestName
		Method testMethod = testClass.getDeclaredMethod("convertsMethodArgument");
		options.setArguments(singletonList(testClass.getName() + "#" + testMethod.getName()));

		TestPlanSpecification specification = convert();

		assertThat(specification.getMethods()).hasSize(1);
		MethodSpecification methodSpecification = specification.getMethods().get(0);
		assertThat(methodSpecification.getTestClass()).isEqualTo(testClass);
		assertThat(methodSpecification.getTestMethod()).isEqualTo(testMethod);
	}

	@Test
	public void convertsPackageArgument() {
		String packageName = getClass().getPackage().getName();
		options.setArguments(singletonList(packageName));

		TestPlanSpecification specification = convert();

		assertThat(specification.getPackages()).containsOnly(packageName);
	}

	@Test
	public void convertsAllOptionWithoutExplicitRootDirectories() {
		options.setRunAllTests(true);

		TestPlanSpecification specification = convert();

		assertThat(specification.getFolders()).isNotEmpty();
	}

	@Test
	public void convertsAllOptionWithExplicitRootDirectories() {
		options.setRunAllTests(true);
		options.setArguments(asList(".", ".."));

		TestPlanSpecification specification = convert();

		assertThat(specification.getFolders()).containsOnly(new File("."), new File(".."));
	}

	@Test
	public void convertsClassnameFilterOption() {
		options.setRunAllTests(true);
		options.setClassnameFilter(".*Test");

		TestPlanSpecification specification = convert();

		assertThat(specification.getEngineFilters()).hasSize(1);
		assertThat(specification.getEngineFilters().get(0).getDescription()).contains(".*Test");
	}

	@Test
	public void convertsTagFilterOption() {
		options.setRunAllTests(true);
		options.setTagsFilter(asList("fast", "medium", "slow"));
		options.setExcludeTags(asList("slow"));

		TestPlanSpecification specification = convert();

		assertTrue(specification.acceptDescriptor(testDescriptorWithTag("fast")));
		assertTrue(specification.acceptDescriptor(testDescriptorWithTag("medium")));
		assertFalse(specification.acceptDescriptor(testDescriptorWithTag("slow")));
		assertFalse(specification.acceptDescriptor(testDescriptorWithTag("very slow")));
	}

	private TestPlanSpecification convert() {
		TestPlanSpecificationCreator creator = new TestPlanSpecificationCreator();
		return creator.toTestPlanSpecification(options);
	}

	private TestDescriptor testDescriptorWithTag(String tag) {
		TestDescriptor testDescriptor = mock(TestDescriptor.class);
		when(testDescriptor.getTags()).thenReturn(singleton(new TestTag(tag)));
		return testDescriptor;
	}
}
