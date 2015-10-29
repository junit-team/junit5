/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for {@link TestPlanSpecification}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanSpecificationTests {

	@Test
	public void testPlanBuilderDemo() {
		TestPlanSpecification specification = TestPlanSpecification.build(
			TestPlanSpecification.forUniqueId("junit5:org.example.UserTests#fullname()"));

		assertNotNull(specification);
	}

}
