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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;

/**
 * Either contains a successfully parsed {@link TagExpression} or an <em>error message</em> describing the parse error.
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public interface ParseResult {

	default TagExpression tagExpressionOrThrow(Function<String, RuntimeException> error) {
		if (errorMessage().isPresent()) {
			throw error.apply(errorMessage().get());
		}
		return tagExpression().get();
	}

	default Optional<String> errorMessage() {
		return Optional.empty();
	}

	default Optional<TagExpression> tagExpression() {
		return Optional.empty();
	}

}
