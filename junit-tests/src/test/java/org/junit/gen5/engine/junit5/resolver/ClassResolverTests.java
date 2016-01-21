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
import static org.junit.gen5.engine.junit5.resolver.ClassResolver.descriptorForParentAndClass;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.descriptorForParentAndName;
import static org.junit.gen5.launcher.main.DiscoveryRequestBuilder.request;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.junit5.descriptor.NewPackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.NestingTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistrySpy;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class ClassResolverTests {
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistrySpy testResolverRegistrySpy;
	private ClassResolver resolver;

	@BeforeEach
	void setUp() {
		testResolverRegistrySpy = new TestResolverRegistrySpy();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = new EngineDescriptor(testEngine);

		resolver = new ClassResolver();
		resolver.initialize(testEngine, testResolverRegistrySpy);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistrySpy.testDescriptors).isEmpty();
	}

	@Test
	void whenNotifiedWithAPackageTestDescriptor_resolvesAllTopLevelClassesInThePackage() throws Exception {
		NewPackageTestDescriptor testPackage = descriptorForParentAndName(engineDescriptor,
			"org.junit.gen5.engine.junit5.resolver.testpackage");
		engineDescriptor.addChild(testPackage);

		resolver.resolveAllFrom(testPackage, request().build());

		// @formatter:off
		assertThat(testResolverRegistrySpy.testDescriptors)
                .containsOnly(
                        descriptorForParentAndClass(testPackage, SingleTestClass.class),
                        descriptorForParentAndClass(testPackage, NestingTestClass.class)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}

}
