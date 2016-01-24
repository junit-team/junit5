/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.ReflectionUtils.findMethod;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.ClasspathSelector.forPath;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.engine.junit5.resolver.ClassResolver.resolveClass;
import static org.junit.gen5.engine.junit5.resolver.EngineResolver.resolveEngine;
import static org.junit.gen5.engine.junit5.resolver.MethodResolver.resolveMethod;
import static org.junit.gen5.engine.junit5.resolver.NestedMemberClassResolver.resolveNestedClass;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.resolvePackage;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.NestingTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.TestsWithParametersTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.notatestclass.NotATestClassPlaceholder;
import org.junit.gen5.engine.junit5.resolver.testpackage.subpackage1.Subpackage1Placeholder;
import org.junit.gen5.engine.junit5.resolver.testpackage.subpackage2.Subpackage2Placeholder;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.launcher.TestDiscoveryRequest;

public class PreconfigureTestResolverRegistryRegressionTests {
	private EngineDescriptor engineDescriptor;
	private TestDescriptor packageLevel1;
	private TestDescriptor packageLevel2;
	private TestDescriptor packageLevel3;
	private TestDescriptor packageLevel4;
	private TestDescriptor packageLevel5;
	private TestDescriptor packageLevel6;
	private TestDescriptor packageLevel7;
	private TestDescriptor packageLevel8a;
	private TestDescriptor packageLevel8b;
	private TestDescriptor packageLevel8c;

	private String testPackageName = "org.junit.gen5.engine.junit5.resolver.testpackage";
	private TestEngine testEngine;
	private TestResolverRegistry registry;

	@BeforeEach
	void setUp() {
		testEngine = new JUnit5TestEngine();
		registry = new PreconfiguredTestResolverRegistry();

		engineDescriptor = resolveEngine(testEngine);
		packageLevel1 = resolvePackage(engineDescriptor, "org");
		packageLevel2 = resolvePackage(packageLevel1, "org.junit");
		packageLevel3 = resolvePackage(packageLevel2, "org.junit.gen5");
		packageLevel4 = resolvePackage(packageLevel3, "org.junit.gen5.engine");
		packageLevel5 = resolvePackage(packageLevel4, "org.junit.gen5.engine.junit5");
		packageLevel6 = resolvePackage(packageLevel5, "org.junit.gen5.engine.junit5.resolver");
		packageLevel7 = resolvePackage(packageLevel6, testPackageName);
		packageLevel8a = resolvePackage(packageLevel7, testPackageName + ".subpackage1");
		packageLevel8b = resolvePackage(packageLevel7, testPackageName + ".subpackage2");
		packageLevel8c = resolvePackage(packageLevel7, testPackageName + ".notatestclass");
	}

	@Test
	void givenAPackageSelector_resolvesAllTestsInThePackageHierarchy() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forPackageName(testPackageName)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		verifyOccurrencesOf_Packages_Classes_And_Methods(10, 9, 7);
	}

	@Test
	void givenAPackageSelector_allNodesAreMarkedCorrectly() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forPackageName(testPackageName)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);

		assertThat(engineDescriptor.isRoot()).isTrue();
		assertThat(engineDescriptor.isContainer()).isTrue();
		assertThat(engineDescriptor.isTest()).isFalse();

		// @formatter:off
        this.engineDescriptor.allDescendants().stream()
                .filter(PackageTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();
                });

        this.engineDescriptor.allDescendants().stream()
                .filter(ClassTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();
                });

        this.engineDescriptor.allDescendants().stream()
                .filter(MethodTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isFalse();
                    assertThat(testDescriptor.isTest()).isTrue();
                });
        // @formatter:on
	}

	@Test
	void givenAPackageSelector_allPackagesAreFound() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forPackageName(testPackageName)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);

		List<TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants().stream().filter(
			PackageTestDescriptor.class::isInstance).collect(toList());
		assertThat(packageDescriptors).containsOnly(packageLevel1, packageLevel2, packageLevel3, packageLevel4,
			packageLevel5, packageLevel6, packageLevel7, packageLevel8a, packageLevel8b,
			packageLevel8c).doesNotHaveDuplicates();
	}

	@Test
	void givenAClassSelector_resolvesOnlyTheTestClassWithItsPackageHierarchy() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forClass(SingleTestClass.class)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);
		verifyOccurrencesOf_Packages_Classes_And_Methods(7, 1, 2);
	}

	@Test
	void givenAMethodSelector_resolvesOnlyTheTestMethodWithItsClassAndPackageHierarchy() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forMethod(SingleTestClass.class, "test1")).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);
		verifyOccurrencesOf_Packages_Classes_And_Methods(7, 1, 1);
	}

	@Test
	void givenAnUniqueIdSelectorForAMethod_resolvesOnlyTheTestMethodWithItsHierarchy() throws Exception {
		Class<SingleTestClass> testClass = SingleTestClass.class;
		Method testMethod = findMethod(testClass, "test1").get();
		ClassTestDescriptor classTestDescriptor = resolveClass(packageLevel7, testClass);
		MethodTestDescriptor methodTestDescriptor = resolveMethod(classTestDescriptor, testClass, testMethod);

		UniqueIdSelector uniqueIdSelector = forUniqueId(methodTestDescriptor.getUniqueId());
		TestDiscoveryRequest discoveryRequest = request().select(uniqueIdSelector).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);
		verifyOccurrencesOf_Packages_Classes_And_Methods(7, 1, 1);
	}

	@Test
	void givenAnUniqueIdSelectorForAMethodWithParameters_resolvesOnlyTheTestMethodWithItsHierarchy() throws Exception {
		Class<TestsWithParametersTestClass> testClass = TestsWithParametersTestClass.class;
		Method testMethod = findMethod(testClass, "test1", TestInfo.class).get();
		ClassTestDescriptor classTestDescriptor = resolveClass(packageLevel7, testClass);
		MethodTestDescriptor methodTestDescriptor = resolveMethod(classTestDescriptor, testClass, testMethod);

		UniqueIdSelector uniqueIdSelector = forUniqueId(methodTestDescriptor.getUniqueId());
		TestDiscoveryRequest discoveryRequest = request().select(uniqueIdSelector).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);
		verifyOccurrencesOf_Packages_Classes_And_Methods(7, 1, 1);
	}

	@Test
	void givenAnUniqueIdSelectorForANestedClassTestMethod_resolvesOnlyTheTestMethodWithItsHierarchy() throws Exception {
		Class<?> testClass = NestingTestClass.NestedInnerClass.DoubleNestedInnerClass.class;
		Method testMethod = findMethod(testClass, "nestedInnerClassTest").get();

		ClassTestDescriptor classLevel1 = resolveClass(packageLevel7, NestingTestClass.class);
		NestedClassTestDescriptor classLevel2 = resolveNestedClass(classLevel1,
			NestingTestClass.NestedInnerClass.class);
		NestedClassTestDescriptor classLevel3 = resolveNestedClass(classLevel2, testClass);
		MethodTestDescriptor methodTestDescriptor = resolveMethod(classLevel3, testClass, testMethod);

		UniqueIdSelector uniqueIdSelector = forUniqueId(methodTestDescriptor.getUniqueId());
		TestDiscoveryRequest discoveryRequest = request().select(uniqueIdSelector).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);
		verifyOccurrencesOf_Packages_Classes_And_Methods(7, 3, 1);
	}

	@Test
	void givenAClasspathSelector_resolvesTheWholeTestSuiteWithinTheClasspath() throws Exception {
		List<DiscoverySelector> selector = forPath(SingleTestClass.class.getResource("/").getFile());
		registry.notifyResolvers(engineDescriptor, request().select(selector).build());

		// TODO Move testpackage to another classpath root and test for this
		// TODO This would make this test more reliable and robust.
		// TODO The assertions below could be replaced with the next line:
		//      verifyOccurrencesOf_Packages_Classes_And_Methods(10, 7, 5);

		// @formatter:off
		List<TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants().stream()
                .filter(PackageTestDescriptor.class::isInstance)
                .collect(toList());
		assertThat(packageDescriptors)
                .contains(
                        packageLevel1, packageLevel2, packageLevel3, packageLevel4, packageLevel5,
                        packageLevel6, packageLevel7, packageLevel8a, packageLevel8b, packageLevel8c)
                .doesNotHaveDuplicates();

		List<TestDescriptor> testDescriptors = this.engineDescriptor.allDescendants().stream()
                .filter(ClassTestDescriptor.class::isInstance)
                .collect(toList());
		assertThat(testDescriptors)
                .contains(
			            resolveClass(packageLevel7, SingleTestClass.class),
			            resolveClass(packageLevel7, NestingTestClass.class),
			            resolveClass(packageLevel7, NestingTestClass.NestedStaticClass.class),
                        resolveClass(packageLevel8a, Subpackage1Placeholder.class),
                        resolveClass(packageLevel8b, Subpackage2Placeholder.class),
                        resolveClass(packageLevel8c, NotATestClassPlaceholder.class))
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	// @formatter:off
	private void verifyOccurrencesOf_Packages_Classes_And_Methods(
            int numberOfPackages, int numberOfClasses, int numberOfMethods) {
        Assertions.assertAll(
            () -> assertThat(this.engineDescriptor.allDescendants().stream()
                .filter(PackageTestDescriptor.class::isInstance)
                .count()).isEqualTo(numberOfPackages),

            () -> assertThat(this.engineDescriptor.allDescendants().stream()
                .filter(ClassTestDescriptor.class::isInstance)
                .count()).isEqualTo(numberOfClasses),

            () -> assertThat(this.engineDescriptor.allDescendants().stream()
                .filter(MethodTestDescriptor.class::isInstance)
                .count()).isEqualTo(numberOfMethods)
        );
	}
    // @formatter:on
}
