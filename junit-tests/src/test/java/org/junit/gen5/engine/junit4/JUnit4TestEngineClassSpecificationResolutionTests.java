/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineAwareTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

class JUnit4TestEngineClassSpecificationResolutionTests {

	JUnit4TestEngine engine = new JUnit4TestEngine();

	@Test
	void resolvesSimpleJUnit4TestClass() {
		TestPlanSpecification specification = build(forClass(SimpleJUnit4TestCase.class));

		EngineAwareTestDescriptor engineDescriptor = engine.discoverTests(specification);

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		TestDescriptor runnerDescriptor = engineDescriptor.getChildren().iterator().next();
		assertEquals("junit4:" + SimpleJUnit4TestCase.class.getName(), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).hasSize(1);

		TestDescriptor childDescriptor = runnerDescriptor.getChildren().iterator().next();
		assertEquals(
			"junit4:" + SimpleJUnit4TestCase.class.getName() + "/test(" + SimpleJUnit4TestCase.class.getName() + ")",
			childDescriptor.getUniqueId());
		assertThat(childDescriptor.getChildren()).isEmpty();
	}

	public static class SimpleJUnit4TestCase {

		@org.junit.Test
		public void test() {
			Assert.fail("this test should fail");
		}

	}
}
