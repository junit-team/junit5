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
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.*;

public class DiscoveryRequestCreatorTests {
	private CommandLineOptions options = new CommandLineOptions();
	private DiscoverySelectorVisitor visitor = mock(DiscoverySelectorVisitor.class);

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

		DiscoveryRequest discoveryRequest = convert();

		List<ClassFilter> filter = discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class);
		assertThat(filter).hasSize(1);
		assertThat(filter.get(0).toString()).contains(".*Test");
	}

	@Test
	public void convertsTagFilterOption() {
		options.setRunAllTests(true);
		options.setTagsFilter(asList("fast", "medium", "slow"));
		options.setExcludeTags(asList("slow"));

		DiscoveryRequest discoveryRequest = convert();

		assertTrue(discoveryRequest.acceptDescriptor(testDescriptorWithTag("fast")));
		assertTrue(discoveryRequest.acceptDescriptor(testDescriptorWithTag("medium")));
		assertFalse(discoveryRequest.acceptDescriptor(testDescriptorWithTag("slow")));
		assertFalse(discoveryRequest.acceptDescriptor(testDescriptorWithTag("very slow")));
	}

	private void convertAndVisit() {
		convert().accept(visitor);
	}

	private DiscoveryRequest convert() {
		DiscoveryRequestCreator creator = new DiscoveryRequestCreator();
		return creator.toDiscoveryRequest(options);
	}

	private TestDescriptor testDescriptorWithTag(String tag) {
		TestDescriptor testDescriptor = mock(TestDescriptor.class);
		when(testDescriptor.getTags()).thenReturn(singleton(new TestTag(tag)));
		return testDescriptor;
	}
}
