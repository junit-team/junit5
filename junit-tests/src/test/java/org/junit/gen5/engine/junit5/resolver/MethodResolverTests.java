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
import static org.junit.gen5.commons.util.ReflectionUtils.findMethod;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.engine.junit5.resolver.ClassResolver.descriptorForParentAndClass;
import static org.junit.gen5.engine.junit5.resolver.EngineResolver.descriptorForEngine;
import static org.junit.gen5.engine.junit5.resolver.MethodResolver.descriptorForParentAndMethod;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class MethodResolverTests {
	private String testPackageName;
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistryMock testResolverRegistryMock;
	private MethodResolver resolver;

	@BeforeEach
	void setUp() {
		testPackageName = SingleTestClass.class.getPackage().getName();
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = descriptorForEngine(testEngine);

		resolver = new MethodResolver();
		resolver.bindTestResolveryRegistry(testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAMethodSelector_resolvesTheMethod() throws Exception {
		Class<SingleTestClass> testClass = SingleTestClass.class;
		Method testMethod = findMethod(testClass, "test1").get();

		PackageTestDescriptor packageDescriptor = descriptorForParentAndName(engineDescriptor, testPackageName);
		engineDescriptor.addChild(packageDescriptor);
		ClassTestDescriptor testClassDescriptor = descriptorForParentAndClass(engineDescriptor, testClass);
		packageDescriptor.addChild(testClassDescriptor);

		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testClassDescriptor;

		resolver.resolveAllFrom(engineDescriptor, request().select(forMethod(testClass, testMethod)).build());

		// @formatter:off
        assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        descriptorForParentAndMethod(testClassDescriptor, testClass, testMethod)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void whenNotifiedWithAClassTestDescriptor_resolvesTestMesthodsInTheClass() throws Exception {
		Class<SingleTestClass> testClass = SingleTestClass.class;
		Method testMethod1 = findMethod(testClass, "test1").get();
		Method testMethod2 = findMethod(testClass, "test2").get();

		PackageTestDescriptor packageDescriptor = descriptorForParentAndName(engineDescriptor, testPackageName);
		engineDescriptor.addChild(packageDescriptor);
		ClassTestDescriptor testClassDescriptor = descriptorForParentAndClass(engineDescriptor, testClass);
		packageDescriptor.addChild(testClassDescriptor);

		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testClassDescriptor;

		resolver.resolveAllFrom(testClassDescriptor, request().select(forClass(testClass)).build());

		// @formatter:off
        assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        descriptorForParentAndMethod(testClassDescriptor, testClass, testMethod1),
                        descriptorForParentAndMethod(testClassDescriptor, testClass, testMethod2)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}
}
