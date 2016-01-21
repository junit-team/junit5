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
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.DiscoveryRequestBuilder.request;

import java.util.List;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
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

	private String testPackageName = "org.junit.gen5.engine.junit5.resolver.testpackage";
	private TestEngine testEngine;
	private TestResolverRegistry registry;

	@BeforeEach
	void setUp() {
		testEngine = new TestEngineStub();
		registry = new PreconfiguredTestResolverRegistry(testEngine);

		engineDescriptor = new EngineDescriptor(testEngine);
		packageLevel1 = descriptorForParentAndName(engineDescriptor, "org");
		packageLevel2 = descriptorForParentAndName(packageLevel1, "org.junit");
		packageLevel3 = descriptorForParentAndName(packageLevel2, "org.junit.gen5");
		packageLevel4 = descriptorForParentAndName(packageLevel3, "org.junit.gen5.engine");
		packageLevel5 = descriptorForParentAndName(packageLevel4, "org.junit.gen5.engine.junit5");
		packageLevel6 = descriptorForParentAndName(packageLevel5, "org.junit.gen5.engine.junit5.resolver");
		packageLevel7 = descriptorForParentAndName(packageLevel6, testPackageName);
		packageLevel8a = descriptorForParentAndName(packageLevel7, testPackageName + ".subpackage1");
		packageLevel8b = descriptorForParentAndName(packageLevel7, testPackageName + ".subpackage2");
	}

	@Test
	void givenAPackageSelector_resolvesAllClassesInThePackageHierarchy() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forPackageName(testPackageName)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);

		List<TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants().stream().filter(
			PackageTestDescriptor.class::isInstance).collect(toList());
		assertThat(packageDescriptors).containsOnly(packageLevel1, packageLevel2, packageLevel3, packageLevel4,
			packageLevel5, packageLevel6, packageLevel7, packageLevel8a, packageLevel8b).doesNotHaveDuplicates();

		assertThat(engineDescriptor.isRoot()).isTrue();
		assertThat(engineDescriptor.isContainer()).isTrue();
		assertThat(engineDescriptor.isTest()).isFalse();
		assertThat(engineDescriptor.getChildren()).hasSize(1);

		// @formatter:off
        this.engineDescriptor.allDescendants().stream()
                .filter(PackageTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();

                    if (!testDescriptor.getName().equals(testPackageName)) {
                        assertThat(testDescriptor.getChildren()).hasSize(1);
                    }
                    else {
                        assertThat(testDescriptor.getChildren()).hasSize(5);
                    }
                });
        // @formatter:on

		// @formatter:off
        this.engineDescriptor.allDescendants().stream()
                .filter(ClassTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();
                    assertThat(testDescriptor.getChildren()).isEmpty();
                });
        // @formatter:on

	}

	@Test
	void givenAClassSelector_resolvesOnlyTheTestClassWithItsPackageHierarchy() throws Exception {
		TestDiscoveryRequest discoveryRequest = request().select(forClass(SingleTestClass.class)).build();
		registry.notifyResolvers(engineDescriptor, discoveryRequest);

		List<TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants().stream().filter(
			PackageTestDescriptor.class::isInstance).collect(toList());
		assertThat(packageDescriptors).containsOnly(packageLevel1, packageLevel2, packageLevel3, packageLevel4,
			packageLevel5, packageLevel6, packageLevel7).doesNotHaveDuplicates();

		assertThat(engineDescriptor.isRoot()).isTrue();
		assertThat(engineDescriptor.isContainer()).isTrue();
		assertThat(engineDescriptor.isTest()).isFalse();
		assertThat(engineDescriptor.getChildren()).hasSize(1);

		// @formatter:off
        this.engineDescriptor.allDescendants().stream()
                .filter(PackageTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();
                    assertThat(testDescriptor.getChildren()).hasSize(1);
                });
        // @formatter:on

		// @formatter:off
        this.engineDescriptor.allDescendants().stream()
                .filter(ClassTestDescriptor.class::isInstance)
                .forEach(testDescriptor -> {
                    assertThat(testDescriptor.isRoot()).isFalse();
                    assertThat(testDescriptor.isContainer()).isTrue();
                    assertThat(testDescriptor.isTest()).isFalse();
                    assertThat(testDescriptor.getChildren()).isEmpty();
                });
        // @formatter:on
	}
}
