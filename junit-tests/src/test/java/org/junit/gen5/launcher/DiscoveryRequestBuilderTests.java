/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.ClassSelector.forClassName;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.DiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.assertj.core.util.Files;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.ClasspathSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;

public class DiscoveryRequestBuilderTests {

	@Test
	public void packagesAreStoredInDiscoveryRequest() throws Exception {
		// @formatter:off
        TestDiscoveryRequest discoveryRequest = request()
				.select(
						forPackageName("org.junit.gen5.engine")
				).build();
        // @formatter:on

		List<String> packageSelectors = discoveryRequest.getSelectorsByType(PackageSelector.class).stream().map(
			PackageSelector::getPackageName).collect(toList());
		assertThat(packageSelectors).contains("org.junit.gen5.engine");
	}

	@Test
	public void classesAreStoredInDiscoveryRequest() throws Exception {
		// @formatter:off
        TestDiscoveryRequest discoveryRequest = request()
				.select(
						forClassName(DiscoveryRequestBuilderTests.class.getName()),
						forClass(SampleTestClass.class)
				)
            .build();
        // @formatter:on

		List<Class<?>> classes = discoveryRequest.getSelectorsByType(ClassSelector.class).stream().map(
			ClassSelector::getTestClass).collect(toList());
		assertThat(classes).contains(SampleTestClass.class, DiscoveryRequestBuilderTests.class);
	}

	@Test
	public void methodsByNameAreStoredInDiscoveryRequest() throws Exception {
		Class<?> testClass = SampleTestClass.class;
		Method testMethod = testClass.getMethod("test");

		// @formatter:off
        TestDiscoveryRequest discoveryRequest = request()
				.select(forMethod(SampleTestClass.class.getName(), "test"))
				.build();
        // @formatter:on

		List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);
		assertThat(methodSelectors).hasSize(1);

		MethodSelector methodSelector = methodSelectors.get(0);
		assertThat(methodSelector.getTestClass()).isEqualTo(testClass);
		assertThat(methodSelector.getTestMethod()).isEqualTo(testMethod);
	}

	@Test
	public void methodsByClassAreStoredInDiscoveryRequest() throws Exception {
		Class<?> testClass = SampleTestClass.class;
		Method testMethod = testClass.getMethod("test");

		// @formatter:off
        DiscoveryRequest discoveryRequest = (DiscoveryRequest) request()
				.select(
						MethodSelector.forMethod(SampleTestClass.class, "test")
				).build();
		// @formatter:on

		List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);
		assertThat(methodSelectors).hasSize(1);

		MethodSelector methodSelector = methodSelectors.get(0);
		assertThat(methodSelector.getTestClass()).isEqualTo(testClass);
		assertThat(methodSelector.getTestMethod()).isEqualTo(testMethod);
	}

	@Test
	public void unavailableFoldersAreNotStoredInDiscoveryRequest() throws Exception {
		// @formatter:off
        TestDiscoveryRequest discoveryRequest = request()
				.select(
						ClasspathSelector.forPath("/some/local/path")
				).build();
        // @formatter:on

		List<String> folders = discoveryRequest.getSelectorsByType(ClasspathSelector.class).stream().map(
			ClasspathSelector::getClasspathRoot).map(File::getAbsolutePath).collect(toList());

		assertThat(folders).isEmpty();
	}

	@Test
	public void availableFoldersAreStoredInDiscoveryRequest() throws Exception {
		File temporaryFolder = Files.newTemporaryFolder();
		try {
			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
					.select(
							ClasspathSelector.forPath(temporaryFolder.getAbsolutePath())
					).build();
			// @formatter:on

			List<String> folders = discoveryRequest.getSelectorsByType(ClasspathSelector.class).stream().map(
				ClasspathSelector::getClasspathRoot).map(File::getAbsolutePath).collect(toList());

			assertThat(folders).contains(temporaryFolder.getAbsolutePath());
		}
		finally {
			temporaryFolder.delete();
		}
	}

	@Test
	public void uniqueIdsAreStoredInDiscoveryRequest() throws Exception {
		// @formatter:off
        TestDiscoveryRequest discoveryRequest = request()
				.select(
						forUniqueId("engine:bla:foo:bar:id1"),
						forUniqueId("engine:bla:foo:bar:id2")
				).build();
        // @formatter:on

		List<String> uniqueIds = discoveryRequest.getSelectorsByType(UniqueIdSelector.class).stream().map(
			UniqueIdSelector::getUniqueId).collect(toList());

		assertThat(uniqueIds).contains("engine:bla:foo:bar:id1", "engine:bla:foo:bar:id2");
	}

	private static class SampleTestClass {

		@Test
		public void test() {
		}
	}
}
