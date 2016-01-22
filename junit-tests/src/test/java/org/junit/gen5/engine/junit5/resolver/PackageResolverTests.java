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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.junit5.resolver.EngineResolver.descriptorForEngine;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class PackageResolverTests {
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

	private TestResolverRegistryMock testResolverRegistryMock;
	private PackageResolver resolver;

	@BeforeEach
	void setUp() {
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = descriptorForEngine(testEngine);
		packageLevel1 = descriptorForParentAndName(engineDescriptor, "org");
		packageLevel2 = descriptorForParentAndName(packageLevel1, "org.junit");
		packageLevel3 = descriptorForParentAndName(packageLevel2, "org.junit.gen5");
		packageLevel4 = descriptorForParentAndName(packageLevel3, "org.junit.gen5.engine");
		packageLevel5 = descriptorForParentAndName(packageLevel4, "org.junit.gen5.engine.junit5");
		packageLevel6 = descriptorForParentAndName(packageLevel5, "org.junit.gen5.engine.junit5.resolver");
		packageLevel7 = descriptorForParentAndName(packageLevel6, testPackageName);
		packageLevel8a = descriptorForParentAndName(packageLevel7, testPackageName + ".subpackage1");
		packageLevel8b = descriptorForParentAndName(packageLevel7, testPackageName + ".subpackage2");
		packageLevel8c = descriptorForParentAndName(packageLevel7, testPackageName + ".notatestclass");

		resolver = new PackageResolver();
		resolver.bindTestResolveryRegistry(testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAPackageSelector_resolvesThePackage() throws Exception {
		PackageSelector selector = forPackageName(testPackageName);
		resolver.resolveAllFrom(engineDescriptor, request().select(selector).build());
		assertThat(testResolverRegistryMock.testDescriptors).containsOnly(packageLevel7);
	}

	@Test
	void givenDuplicatedPackageSelector_resolvesThePackageOnlyOnce() throws Exception {
		PackageSelector selector = forPackageName(testPackageName);
		resolver.resolveAllFrom(engineDescriptor, request().select(selector, selector).build());
		assertThat(testResolverRegistryMock.testDescriptors).containsOnlyOnce(packageLevel7);
	}

	@Test
	void givenAPackageSelector_resolvesPackagesRecursively() throws Exception {
		PackageSelector selector = forPackageName(testPackageName);
		packageLevel6.addChild(packageLevel7);
		resolver.resolveAllFrom(packageLevel7, request().select(selector).build());
		assertThat(testResolverRegistryMock.testDescriptors).containsOnly(packageLevel8a, packageLevel8b,
			packageLevel8c);
	}

	@Test
	void givenAPackageSelector_fetchesTheTreeUpToTheRoot() {
		PackageSelector selector = forPackageName(testPackageName);
		Optional<TestDescriptor> testDescriptor = resolver.fetchBySelector(selector, engineDescriptor);

		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
		assertThat(testDescriptor.isPresent()).isTrue();
		assertThat(this.engineDescriptor.getChildren()).hasSize(1);

		Set<? extends TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants();
		assertThat(packageDescriptors).containsOnly(packageLevel1, packageLevel2, packageLevel3, packageLevel4,
			packageLevel5, packageLevel6, packageLevel7).doesNotHaveDuplicates();
	}

	@Test
	void givenAPackageSelector_itPicksUpAlreadyCreatedDescriptorsAndReturnsThem() {
		engineDescriptor.addChild(packageLevel1);

		PackageSelector selector = forPackageName(testPackageName);
		Optional<TestDescriptor> testDescriptor = resolver.fetchBySelector(selector, engineDescriptor);

		assertThat(testDescriptor.isPresent()).isTrue();
		assertThat(this.engineDescriptor.getChildren()).hasSize(1);

		Set<? extends TestDescriptor> packageDescriptors = this.engineDescriptor.allDescendants();
		assertThat(packageDescriptors).containsOnly(packageLevel1, packageLevel2, packageLevel3, packageLevel4,
			packageLevel5, packageLevel6, packageLevel7).doesNotHaveDuplicates();
	}
}
