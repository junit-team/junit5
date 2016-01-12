/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClass;
import static org.junit.gen5.engine.dsl.ClassTestPlanSpecificationElementBuilder.forClassName;
import static org.junit.gen5.engine.dsl.MethodTestPlanSpecificationElementBuilder.forMethod;
import static org.junit.gen5.engine.dsl.PackageTestPlanSpecificationElementBuilder.forPackage;
import static org.junit.gen5.engine.dsl.TestPlanSpecificationBuilder.testPlanSpecification;
import static org.junit.gen5.engine.dsl.UniqueIdTestPlanSpecificationElementBuilder.forUniqueId;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Files;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.dsl.*;

public class TestPlanSpecificationBuilderTests {
	@Test
	public void packagesAreStoredInSpecification() throws Exception {
		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						forPackage("org.junit.gen5.engine")
				).build();
        // @formatter:on

		List<String> packageSpecifications = testPlanSpecification.getElementsByType(
			PackageSpecification.class).stream().map(PackageSpecification::getPackageName).collect(toList());
		assertThat(packageSpecifications).contains("org.junit.gen5.engine");
	}

	@Test
	public void classesAreStoredInSpecification() throws Exception {
		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						forClassName("org.junit.gen5.engine.TestPlanSpecificationBuilderTests"),
						forClass(SampleTestClass.class)
				)
            .build();
        // @formatter:on

		List<Class<?>> classes = testPlanSpecification.getElementsByType(ClassSpecification.class).stream().map(
			ClassSpecification::getTestClass).collect(toList());
		assertThat(classes).contains(SampleTestClass.class, TestPlanSpecificationBuilderTests.class);
	}

	@Test
	public void methodsByNameAreStoredInSpecification() throws Exception {
		Class<?> testClass = SampleTestClass.class;
		Method testMethod = testClass.getMethod("test");

		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						forMethod("org.junit.gen5.engine.TestPlanSpecificationBuilderTests$SampleTestClass", "test")
				).build();
        // @formatter:on

		List<MethodSpecification> methodSpecifications = testPlanSpecification.getElementsByType(
			MethodSpecification.class);
		assertThat(methodSpecifications).hasSize(1);

		MethodSpecification methodSpecification = methodSpecifications.get(0);
		assertThat(methodSpecification.getTestClass()).isEqualTo(testClass);
		assertThat(methodSpecification.getTestMethod()).isEqualTo(testMethod);
	}

	@Test
	public void methodsByClassAreStoredInSpecification() throws Exception {
		Class<?> testClass = SampleTestClass.class;
		Method testMethod = testClass.getMethod("test");

		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						MethodTestPlanSpecificationElementBuilder.forMethod(SampleTestClass.class, "test")
				).build();
		// @formatter:on

		List<MethodSpecification> methodSpecifications = testPlanSpecification.getElementsByType(
			MethodSpecification.class);
		assertThat(methodSpecifications).hasSize(1);

		MethodSpecification methodSpecification = methodSpecifications.get(0);
		assertThat(methodSpecification.getTestClass()).isEqualTo(testClass);
		assertThat(methodSpecification.getTestMethod()).isEqualTo(testMethod);
	}

	@Test
	public void unavailableFoldersAreNotStoredInSpecification() throws Exception {
		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						ClasspathTestPlanSpecificationElementBuilder.path("/some/local/path")
				).build();
        // @formatter:on

		List<String> folders = testPlanSpecification.getElementsByType(AllTestsSpecification.class).stream().map(
			AllTestsSpecification::getClasspathRoot).map(File::getAbsolutePath).collect(toList());

		assertThat(folders).isEmpty();
	}

	@Test
	public void availableFoldersAreStoredInSpecification() throws Exception {
		File temporaryFolder = Files.newTemporaryFolder();
		try {
			// @formatter:off
			TestPlanSpecification testPlanSpecification = testPlanSpecification()
					.withElements(
							ClasspathTestPlanSpecificationElementBuilder.path(temporaryFolder.getAbsolutePath())
					).build();
			// @formatter:on

			List<String> folders = testPlanSpecification.getElementsByType(AllTestsSpecification.class).stream().map(
				AllTestsSpecification::getClasspathRoot).map(File::getAbsolutePath).collect(toList());

			assertThat(folders).contains(temporaryFolder.getAbsolutePath());
		}
		finally {
			temporaryFolder.delete();
		}
	}

	@Test
	public void uniqueIdsAreStoredInSpecification() throws Exception {
		// @formatter:off
        TestPlanSpecification testPlanSpecification = testPlanSpecification()
				.withElements(
						forUniqueId("engine:bla:foo:bar:id1"),
						forUniqueId("engine:bla:foo:bar:id2")
				).build();
        // @formatter:on

		List<String> uniqueIds = testPlanSpecification.getElementsByType(UniqueIdSpecification.class).stream().map(
			UniqueIdSpecification::getUniqueId).collect(toList());

		assertThat(uniqueIds).contains("engine:bla:foo:bar:id1", "engine:bla:foo:bar:id2");
	}

	private static class SampleTestClass {
		@Test
		public void test() {
		}
	}
}
