/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @since 1.1
 */
class Tokenizer {

	private static final Pattern PATTERN = Pattern.compile("\\s*(?:(?:(?:any|none)\\(\\))|[()!|&]|(?:[^\\s()!|&]+))",
		CASE_INSENSITIVE);

	List<Token> tokenize(String infixTagExpression) {
		if (infixTagExpression == null) {
			return emptyList();
		}
		List<Token> parts = new ArrayList<>();
		try (Scanner scanner = new Scanner(infixTagExpression)) {
			String match;
			while ((match = scanner.findWithinHorizon(PATTERN, 0)) != null) {
				parts.add(new Token(scanner.match().start(), match));
			}
		}
		return parts;
	}

}
