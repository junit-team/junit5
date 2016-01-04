/*
 * Copyright 2015-2016 the original author or authors.
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
import org.junit.gen5.junit4runner.JUnit5.ExcludeTags;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Classes({ SampleTestCase.class, SucceedingTestCase.class, JUnit4TestCase.class })
//@UniqueIds({ "junit5:com.example.SampleTestCase#assertAllTest()",
//		"junit5:com.example.SampleTestCase#assertAllFailingTest()",
//		"junit5:com.example.SampleTestCase@AnInnerTestContext" })
//@Packages("com.example.subpackage")
//@ClassNameMatches(".*TestCase.")
//@OnlyIncludeTags({ "fast" })
@ExcludeTags({ "fast" })
//@OnlyEngine("junit5")
public class JUnit4SamplesSuite {

	// When you have the following method, it overrides all annotations
	//		public static TestPlanSpecification createSpecification() {
	//			return TestPlanSpecification.build(
	//				TestPlanSpecification.forNames(SucceedingTestCase.class.getName()));
	//		}
}
