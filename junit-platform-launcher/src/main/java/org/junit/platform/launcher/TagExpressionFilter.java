/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.tagexpression.TagExpression;

/**
 * Factory method for creating {@link PostDiscoveryFilter PostDiscoveryFilter}
 * based on a <em>tag expression</em>.
 *
 * @since 1.1
 * @see #includeMatching(String)
 */
@API(status = EXPERIMENTAL, since = "1.1")
public class TagExpressionFilter {

	private static final Logger logger = LoggerFactory.getLogger(TagExpressionFilter.class);

	///CLOVER:OFF
	private TagExpressionFilter() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create an <em>include</em> filter based on the supplied {@code infixTagExpression}.
	 *
	 * <p>Containers and tests will only be executed if their tags match the supplied <em>infixTagExpression</em>.
	 *
	 * @param infixTagExpression to parse and evaluate against a {@link TestDescriptor}; never {@code null} or empty
	 * @throws PreconditionViolationException if the supplied infixTagExpression can not be parsed.
	 */
	public static PostDiscoveryFilter includeMatching(String infixTagExpression) {
		TagExpression tagExpression = TagExpression.parseFrom(infixTagExpression).tagExpressionOrThrow(
			(message) -> new PreconditionViolationException(
				"Unable to parse tag expression [" + infixTagExpression + "]: " + message));
		logger.config(() -> "parsed tag expression: " + tagExpression);
		return descriptor -> FilterResult.includedIf(tagExpression.evaluate(descriptor.getTags()));
	}

}
