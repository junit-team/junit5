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
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.DiscoveryRequestBuilder.request;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class PackageResolverTests {
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistryMock testResolverRegistryMock;
	private PackageResolver resolver;

	@BeforeEach
	void setUp() {
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = new EngineDescriptor(testEngine);

		resolver = new PackageResolver();
		resolver.initialize(testEngine, testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAPackageSelector_resolvesThePackage() throws Exception {
		PackageSelector selector = forPackageName("org.junit.gen5.engine.junit5.resolver.testpackage");

		resolver.resolveAllFrom(engineDescriptor, request().select(selector).build());

		assertThat(testResolverRegistryMock.testDescriptors).containsOnly(
			descriptorForParentAndName(engineDescriptor, "org.junit.gen5.engine.junit5.resolver.testpackage"));
		verifyDescriptor(testResolverRegistryMock.testDescriptors.get(0),
			"org.junit.gen5.engine.junit5.resolver.testpackage", engineDescriptor);
	}

	@Test
	void givenDuplicatedPackageSelector_resolvesThePackageOnlyOnce() throws Exception {
		PackageSelector selector = forPackageName("org.junit.gen5.engine.junit5.resolver.testpackage");

		resolver.resolveAllFrom(engineDescriptor, request().select(selector, selector).build());

		assertThat(testResolverRegistryMock.testDescriptors).containsOnlyOnce(
			descriptorForParentAndName(engineDescriptor, "org.junit.gen5.engine.junit5.resolver.testpackage"));
		verifyDescriptor(testResolverRegistryMock.testDescriptors.get(0),
			"org.junit.gen5.engine.junit5.resolver.testpackage", engineDescriptor);
	}

	@Test
	void givenAPackageSelector_resolvesPackagesRecursively() throws Exception {
		PackageSelector selector = forPackageName("org.junit.gen5.engine.junit5.resolver.testpackage");
		NewPackageTestDescriptor firstLevelPackage = descriptorForParentAndName(engineDescriptor,
			"org.junit.gen5.engine.junit5.resolver.testpackage");
		engineDescriptor.addChild(firstLevelPackage);

		resolver.resolveAllFrom(firstLevelPackage, request().select(selector).build());

		assertThat(testResolverRegistryMock.testDescriptors).containsOnly(
			descriptorForParentAndName(firstLevelPackage, "subpackage1"),
			descriptorForParentAndName(firstLevelPackage, "subpackage2"));

		verifyDescriptor(testResolverRegistryMock.testDescriptors.get(0), "subpackage1", firstLevelPackage);
		verifyDescriptor(testResolverRegistryMock.testDescriptors.get(1), "subpackage2", firstLevelPackage);
	}

	private void verifyDescriptor(TestDescriptor testDescriptor, String packageName, TestDescriptor parent) {
		assertThat(testDescriptor.getUniqueId()).isEqualTo(parent.getUniqueId() + "/[package:" + packageName + "]");
		assertThat(testDescriptor.getName()).isEqualTo(packageName);
		assertThat(testDescriptor.getDisplayName()).isEqualTo(packageName);
		assertThat(testDescriptor.isRoot()).isFalse();
		assertThat(testDescriptor.isContainer()).isTrue();
		assertThat(testDescriptor.isTest()).isFalse();
		assertThat(testDescriptor.getParent().isPresent()).isTrue();
		assertThat(testDescriptor.getParent().get()).isEqualTo(parent);
	}
}
