/*
 * Copyright 2015 the original author or authors.
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
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.TestTag;

public class TestPlanSpecificationCreatorTests {

	private CommandLineOptions options = new CommandLineOptions();
	private TestPlanSpecificationElementVisitor visitor = mock(TestPlanSpecificationElementVisitor.class);

	@Test
	public void convertsClassArgument() {
		Class<?> testClass = getClass();
		options.setArguments(singletonList(testClass.getName()));

		convertAndVisit();

		verify(visitor).visitClass(testClass);
	}

	@Test
	public void convertsMethodArgument() throws Exception {
		Class<?> testClass = getClass();
		// TODO #39 Use @TestName
		Method testMethod = testClass.getDeclaredMethod("convertsMethodArgument");
		options.setArguments(singletonList(testClass.getName() + "#" + testMethod.getName()));

		convertAndVisit();

		verify(visitor).visitMethod(testClass, testMethod);
	}

	@Test
	public void convertsPackageArgument() {
		String packageName = getClass().getPackage().getName();
		options.setArguments(singletonList(packageName));

		convertAndVisit();

		verify(visitor).visitPackage(packageName);
	}

	@Test
	public void convertsAllOptionWithoutExplicitRootDirectories() {
		options.setRunAllTests(true);

		convertAndVisit();

		verify(visitor, atLeastOnce()).visitAllTests(notNull(File.class));
	}

	@Test
	public void convertsAllOptionWithExplicitRootDirectories() {
		options.setRunAllTests(true);
		options.setArguments(asList(".", ".."));

		convertAndVisit();

		verify(visitor, times(1)).visitAllTests(new File("."));
		verify(visitor, times(1)).visitAllTests(new File(".."));
		verifyNoMoreInteractions(visitor);
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

	private void convertAndVisit() {
		TestPlanSpecification specification = convert();
		specification.accept(visitor);
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
