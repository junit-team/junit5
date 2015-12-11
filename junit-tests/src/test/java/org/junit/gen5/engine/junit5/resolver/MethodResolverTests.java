/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import java.util.List;

import org.junit.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5.testdoubles.TestDescriptorStub;

public class MethodResolverTests {
	private MethodResolver resolver = new MethodResolver();

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalParent_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(null, build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSpecification_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(new ClassTestDescriptor("id", SinglePassingTestSampleClass.class), null);
	}

	@Test
	public void givenArbitraryTestDescriptor_nothingIsResolved() throws Exception {
		TestDescriptor parent = new TestDescriptorStub();
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		List<TestDescriptor> result = resolver.resolveFor(parent, testPlanSpecification);

		assertThat(result).hasSize(0);
	}

	@Test
	public void givenClassTestGroup_resolvesMethodsWithinTestClassAnnotatedWithTest() throws Exception {
		ClassTestDescriptor parent = new ClassTestDescriptor("id", SinglePassingTestSampleClass.class);
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		List<TestDescriptor> result = resolver.resolveFor(parent, testPlanSpecification);

		assertThat(result).hasSize(1);
		TestDescriptor resolvedChild = result.get(0);

		assertThat(resolvedChild.getParent().isPresent());
		assertThat(resolvedChild.getParent().get()).isEqualTo(parent);
		assertThat(resolvedChild.getUniqueId()).isEqualTo(
			String.format("%s#%s", parent.getUniqueId(), "singlePassingTest()"));
		assertThat(resolvedChild.getDisplayName()).isEqualTo("singlePassingTest");
	}
}
