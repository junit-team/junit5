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

// tag::external_MethodSource_example[]
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExternalMethodSourceDemo {

	@ParameterizedTest
	@MethodSource("example.StringsProviders#blankStrings")
	void testWithExternalMethodSource(String blankString) {
		// test with blank string
	}
}

class StringsProviders {

	static Stream<String> blankStrings() {
		return Stream.of("", " ", " \n ");
	}
}
// end::external_MethodSource_example[]
