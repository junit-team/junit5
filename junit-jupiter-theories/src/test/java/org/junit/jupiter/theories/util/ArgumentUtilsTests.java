/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.domain.DataPointDetails;

class ArgumentUtilsTests {
	private ArgumentUtils utilsUnderTest;

	@BeforeEach
	public void beforeEach() {
		utilsUnderTest = new ArgumentUtils();
	}

	@Test
	public void testGetArgumentsDescription() throws Exception {
		//Setup
		Method testMethod = FakeTestClass.class.getMethod("fakeTestMethod", String.class, String.class);
		Map<Integer, DataPointDetails> dataPointDetailsMap = new HashMap<>();

		String value1 = "value1";
		String source1Name = "source1";
		DataPointDetails argument1Details = new DataPointDetails(value1, Arrays.asList("arg1Qual1", "arg2Qual2"),
			source1Name);
		dataPointDetailsMap.put(0, argument1Details);

		String value2 = "value2";
		String source2Name = "source2";
		DataPointDetails argument2Details = new DataPointDetails(value2, Arrays.asList("arg2Qual1", "arg2Qual2"),
			source2Name);
		dataPointDetailsMap.put(1, argument2Details);

		String delimiter = "@@@";

		//Test
		String result = utilsUnderTest.getArgumentsDescriptions(testMethod, dataPointDetailsMap, delimiter);

		//Verify
		String[] splitResult = result.split(delimiter);

		assertThat(splitResult[0]).contains(value1, source1Name, String.class.getSimpleName(), "0",
			argument1Details.toString());

		assertThat(splitResult[1]).contains(value2, source2Name, String.class.getSimpleName(), "1",
			argument2Details.toString());
	}

	private static class FakeTestClass {
		public void fakeTestMethod(String param1, String param2) {
			//NO-OP
		}
	}
}
