/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.of;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
class ArgumentsTests {

	@Test
	void supportsVarargs() {
		Arguments arguments = of(1, "2", 3.0);

		assertArrayEquals(new Object[] { 1, "2", 3.0 }, arguments.get());
	}

	@Test
	void returnsSameArrayUsedForCreating() {
		Object[] input = { 1, "2", 3.0 };

		Arguments arguments = of(input);

		assertThat(arguments.get()).isSameAs(input);
	}

	@Test
	void hasEmptyDescriptionByDefault() {
		Arguments arguments = of(1);

		assertThat(arguments.description()).isEmpty();
	}

	@Test
	void setDescriptionValidString() {
		String description = "test case #1";

		Arguments arguments = of(1).description(description);

		assertThat(arguments.description()).isEqualTo(description);
	}

	@Test
	void setDescriptionTrimsWhitespaces() {
		String description = " \ttest case #1\n";

		Arguments arguments = of(1).description(description);

		assertThat(arguments.description()).isEqualTo("test case #1");
	}

	@Test
	void setDescriptionCreatesNewObject() {
		String validDescription = "test case #1";

		Arguments argumentsNoDesc = of(1);
		Arguments argumentsWithDesc = argumentsNoDesc.description(validDescription);

		assertThat(argumentsNoDesc.description()).isEmpty();
		assertThat(argumentsWithDesc).isNotEqualTo(argumentsNoDesc);
	}

	@Test
	void replaceDescription() {
		String first = "Test case #1";
		Arguments arguments = of(1).description(first);

		String second = "A better description!";
		arguments = arguments.description(second);

		assertThat(arguments.description()).isEqualTo(second);
	}

	@Test
	void setDescriptionRejectsNull() {
		Arguments arguments = of(1);

		assertThrows(PreconditionViolationException.class,
				() -> arguments.description(null)
		);

		// Description must remain empty.
		assertThat(arguments.description()).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" ",
			"  ",
			"\n\t"
	})
	void setDescriptionRejectsBlankStrings(String illegalDescription) {
		Arguments arguments = of(1);

		assertThrows(PreconditionViolationException.class,
				() -> arguments.description(illegalDescription)
		);

		// Description must remain empty.
		assertThat(arguments.description()).isEmpty();
	}
}
