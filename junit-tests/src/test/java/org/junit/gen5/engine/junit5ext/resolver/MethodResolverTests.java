/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import java.util.List;

import org.junit.Test;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.descriptor.ClassDescriptor;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;

public class MethodResolverTests {
	private MethodResolver resolver = new MethodResolver();

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalParent_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(null, build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void withIllegalSpecification_throwIllegalArgumentException() throws Exception {
		resolver.resolveFor(new ClassDescriptor(SinglePassingTestSampleClass.class, "id", "name"), null);
	}

	@Test
	public void givenArbitraryTestDescriptor_nothingIsResolved() throws Exception {
		GroupDescriptor parent = new GroupDescriptor("id", "name");
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		List<MutableTestDescriptor> result = resolver.resolveFor(parent, testPlanSpecification);

		assertThat(result).hasSize(0);
	}

	@Test
	public void givenClassTestGroup_resolvesMethodsWithinTestClassAnnotatedWithTest() throws Exception {
		ClassDescriptor parent = new ClassDescriptor(SinglePassingTestSampleClass.class, "id", "name");
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		List<MutableTestDescriptor> result = resolver.resolveFor(parent, testPlanSpecification);

		assertThat(result).hasSize(1);
		MutableTestDescriptor resolvedChild = result.get(0);

		assertThat(resolvedChild.getParent().isPresent());
		assertThat(resolvedChild.getParent().get()).isEqualTo(parent);
		assertThat(resolvedChild.getUniqueId()).isEqualTo(
			String.format("%s#%s", parent.getUniqueId(), "singlePassingTest()"));
		assertThat(resolvedChild.getDisplayName()).isEqualTo("singlePassingTest");
	}
}
