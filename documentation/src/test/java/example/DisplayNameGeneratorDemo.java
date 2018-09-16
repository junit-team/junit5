/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGeneration.Style;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(Style.DEFAULT)
class DisplayNameGeneratorDemo {

	@Nested
	@DisplayNameGeneration(Style.UNDERSCORE)
	class A_year_is_not_supported {

		@Test
		void if_it_is_0() {
		}

		@ParameterizedTest(name = "For example, year {0} is not supported.")
		@ValueSource(ints = { -1, -4 })
		void if_it_is_negative(int year) {
		}
	}

	@Nested
	@DisplayNameGeneration(generator = Shout.class)
	class A_year_is_a_leap_year {

		@Test
		void if_it_is_divisible_by_4_but_not_by_100() {
		}
	}

	static class Shout implements DisplayNameGenerator {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return Style.DEFAULT.generateDisplayNameForClass(testClass);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return nestedClass.getSimpleName().toUpperCase().replace('_', ' ') + "!";
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return testMethod.getName().toUpperCase().replace('_', ' ') + "?!";
		}
	}
}
