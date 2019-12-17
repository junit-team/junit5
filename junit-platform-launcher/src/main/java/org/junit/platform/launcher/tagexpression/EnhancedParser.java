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

/**
 *  A simple decorator for a {@link TagExpressionParser} implementation
 *  which adds support for the special expressions 'any()' and 'none()'.
 *
 * @since 1.6
 */
class EnhancedParser implements TagExpressionParser {

	private final TagExpressionParser target = new ShuntingYardBasedParser();

	@Override
	public ParseResult parse(String infixTagExpression) {
		if (infixTagExpression != null) {
			if (infixTagExpression.equals("any()"))
				return ParseResults.success(TagExpressions.any());

			if (infixTagExpression.equals("none()"))
				return ParseResults.success(TagExpressions.none());
		}

		return this.target.parse(infixTagExpression);
	}
}
