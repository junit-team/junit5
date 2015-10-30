/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import org.junit.gen5.junit4runner.JUnit5;
import org.junit.gen5.junit4runner.JUnit5.Classes;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Classes({ SampleTestCase.class, SucceedingTestCase.class })
public class JUnit4SamplesSuite {

	// When you have the following method, it overrides the Classes annotation
	//		public static TestPlanSpecification createSpecification() {
	//			return TestPlanSpecification.build(
	//				TestPlanSpecification.forClassNames(SucceedingTestCase.class.getName()));
	//		}
}
