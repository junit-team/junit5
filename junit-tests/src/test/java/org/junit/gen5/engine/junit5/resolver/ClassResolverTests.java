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
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.junit5.resolver.ClassResolver.descriptorForParentAndClass;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.DiscoveryRequestBuilder.request;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.NestingTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class ClassResolverTests {
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistryMock testResolverRegistryMock;
	private ClassResolver resolver;

	@BeforeEach
	void setUp() {
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = new EngineDescriptor(testEngine);

		resolver = new ClassResolver();
		resolver.initialize(testEngine, testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAClassSelector_resolvesTheClass() {
		PackageTestDescriptor testPackage = descriptorForParentAndName(engineDescriptor,
			"org.junit.gen5.engine.junit5.resolver.testpackage");
		engineDescriptor.addChild(testPackage);

		testResolverRegistryMock.fetchParentFunction = selector -> testPackage;

		resolver.resolveAllFrom(engineDescriptor, request().select(forClass(SingleTestClass.class)).build());

		// @formatter:off
        assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        descriptorForParentAndClass(testPackage, SingleTestClass.class)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageAndAClassSelector_resolvesTheClass_AndAttachesItToTheExistingTree() {
		PackageTestDescriptor testPackage = descriptorForParentAndName(engineDescriptor,
			"org.junit.gen5.engine.junit5.resolver.testpackage");
		engineDescriptor.addChild(testPackage);
		testResolverRegistryMock.fetchParentFunction = selector -> testPackage;

		resolver.resolveAllFrom(engineDescriptor, request().select(forClass(SingleTestClass.class)).build());

		assertThat(engineDescriptor.getChildren()).containsOnly(testPackage);
		assertThat(testPackage.getChildren()).containsOnly(
			descriptorForParentAndClass(testPackage, SingleTestClass.class));
	}

	@Test
	void whenNotifiedWithAPackageTestDescriptor_resolvesAllTopLevelClassesInThePackage() throws Exception {
		PackageTestDescriptor testPackage = descriptorForParentAndName(engineDescriptor,
			"org.junit.gen5.engine.junit5.resolver.testpackage");
		engineDescriptor.addChild(testPackage);

		resolver.resolveAllFrom(testPackage, request().build());

		// @formatter:off
		assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        descriptorForParentAndClass(testPackage, SingleTestClass.class),
                        descriptorForParentAndClass(testPackage, NestingTestClass.class)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}
}
