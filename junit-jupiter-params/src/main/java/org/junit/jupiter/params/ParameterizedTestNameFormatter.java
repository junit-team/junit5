/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static java.util.stream.Collectors.joining;

import java.text.MessageFormat;
import java.util.stream.IntStream;

class ParameterizedTestNameFormatter {

	private final String namePattern;

	ParameterizedTestNameFormatter(String namePattern) {
		this.namePattern = namePattern;
	}

	String format(int invocationIndex, Object... arguments) {
		String result = namePattern.replace("{index}", String.valueOf(invocationIndex));
		if (result.contains("{arguments}")) {
			// @formatter:off
			String replacement = IntStream.range(0, arguments.length)
					.mapToObj(index -> "{" + index + "}")
					.collect(joining(", "));
			// @formatter:on
			result = result.replace("{arguments}", replacement);
		}
		return MessageFormat.format(result, arguments);
	}
}
