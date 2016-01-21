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
import static org.junit.gen5.launcher.main.DiscoveryRequestBuilder.request;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
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
		engineDescriptor = new EngineDescriptor(testEngine);

		resolver = new MethodResolver();
		resolver.initialize(testEngine, testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}
}
