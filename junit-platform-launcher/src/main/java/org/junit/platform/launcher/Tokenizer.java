/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Tokenizer {
	private final List<String> reserved = new ArrayList<>();

	public Tokenizer() {
		reserved.add("(");
		reserved.add(")");
		reserved.add("not");
		reserved.add("or");
		reserved.add("and");
	}

	public List<String> tokenizeWithPostProcessing(String infixTagExpression) {
		String[] parts = infixTagExpression.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").split("\\s");
		List<String> tokens = new LinkedList<>();
		for (int i = 0; i < parts.length; i++) {
			String currentPart = parts[i];
			if (isPartOfATag(currentPart)) {
				for (int j = i + 1; j < parts.length; j++) {
					String nextPart = parts[j];
					if (isPartOfATag(nextPart)) {
						currentPart = currentPart + " " + nextPart;
						++i;
					}
					else {
						break;
					}
				}
			}

			String tokenCandidate = currentPart.trim();
			if (!tokenCandidate.isEmpty()) {
				tokens.add(tokenCandidate);
			}
		}

		return tokens;
	}

	private boolean isPartOfATag(String currentPart) {
		return !reserved.contains(currentPart);
	}
}
