/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import java.util.Optional;

class ParseResults {
	static ParseResult success(Expression expression) {
		return new ParseResult() {
			@Override
			public Optional<Expression> expression() {
				return Optional.of(expression);
			}
		};
	}

	static ParseResult error(String errorMessage) {
		return new ParseResult() {
			@Override
			public Optional<String> errorMessage() {
				return Optional.of(errorMessage);
			}
		};
	}
}
