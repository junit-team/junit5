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
import static org.junit.gen5.engine.junit5.resolver.NestedMemberClassResolver.descriptorForParentAndNestedClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.NestingTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class NestedMemberClassResolverTests {
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistryMock testResolverRegistryMock;
	private NestedMemberClassResolver resolver;

	@BeforeEach
	void setUp() {
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = new EngineDescriptor(testEngine);

		resolver = new NestedMemberClassResolver();
		resolver.bindTestResolveryRegistry(testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAClassSelector_resolvesTheClass() {
		ClassTestDescriptor testClassDescriptor = descriptorForParentAndClass(engineDescriptor, NestingTestClass.class);
		engineDescriptor.addChild(testClassDescriptor);
		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testClassDescriptor;

		resolver.resolveAllFrom(engineDescriptor,
			request().select(forClass(NestingTestClass.NestedInnerClass.class)).build());

		// @formatter:off
		assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(descriptorForParentAndNestedClass(
                        testClassDescriptor, NestingTestClass.NestedInnerClass.class))
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageAndAClassSelector_resolvesTheClass_AndAttachesItToTheExistingTree() {
		ClassTestDescriptor testClassDescriptor = descriptorForParentAndClass(engineDescriptor, NestingTestClass.class);
		engineDescriptor.addChild(testClassDescriptor);
		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testClassDescriptor;

		resolver.resolveAllFrom(engineDescriptor,
			request().select(forClass(NestingTestClass.NestedInnerClass.class)).build());

		// @formatter:off
		assertThat(engineDescriptor.getChildren()).containsOnly(testClassDescriptor);
		assertThat(testClassDescriptor.getChildren())
                .containsOnly(descriptorForParentAndNestedClass(
                        testClassDescriptor, NestingTestClass.NestedInnerClass.class));
        // @formatter:on
	}

	@Test
	void whenNotifiedWithAClassTestDescriptor_resolvesAllNestedClassesInTheClass() throws Exception {
		ClassTestDescriptor testClassDescriptor = descriptorForParentAndClass(engineDescriptor, NestingTestClass.class);
		engineDescriptor.addChild(testClassDescriptor);
		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testClassDescriptor;

		resolver.resolveAllFrom(testClassDescriptor, request().build());

		// @formatter:off
		assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(descriptorForParentAndNestedClass(
                        testClassDescriptor, NestingTestClass.NestedInnerClass.class))
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void whenNotifiedWithANestedClassTestDescriptor_resolvesAllNestedClassesInTheClass() throws Exception {
		NestedClassTestDescriptor nestedTestClassDescriptor = descriptorForParentAndNestedClass(engineDescriptor,
			NestingTestClass.NestedInnerClass.class);
		engineDescriptor.addChild(nestedTestClassDescriptor);
		testResolverRegistryMock.fetchParentFunction = (selector, root) -> nestedTestClassDescriptor;

		resolver.resolveAllFrom(nestedTestClassDescriptor, request().build());

		// @formatter:off
		assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(descriptorForParentAndNestedClass(
                        nestedTestClassDescriptor, NestingTestClass.NestedInnerClass.DoubleNestedInnerClass.class))
                .doesNotHaveDuplicates();
        // @formatter:on
	}
}
