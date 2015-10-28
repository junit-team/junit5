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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

/**
 * Unit tests for {@link TestPlanSpecification.Builder}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanSpecificationBuilderTests {

	@Test
	public void testPlanBuilderDemo() {
		TestPlanSpecification testPlanConfiguration = TestPlanSpecification.builder()//
			.uniqueIds("junit5:org.example.UserTests#fullname()")//
			.build();

		assertThat(testPlanConfiguration).isNotNull();
	}

}
