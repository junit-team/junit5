/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.lang.String.format;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.runner.Description;

/**
 * @since 4.12
 */
class UniqueIdReader implements Function<Description, Serializable> {

	private final Logger logger;
	private final String fieldName;

	UniqueIdReader(Logger logger) {
		this(logger, "fUniqueId");
	}

	// For tests only
	UniqueIdReader(Logger logger, String fieldName) {
		this.logger = logger;
		this.fieldName = fieldName;
	}

	@Override
	public Serializable apply(Description description) {
		Optional<Object> result = readFieldValue(Description.class, fieldName, description);
		return result.map(Serializable.class::cast).orElseGet(() -> {
			logger.warning(() -> format("Could not read unique id for Description, using display name instead: %s",
				description.toString()));
			return description.getDisplayName();
		});
	}

}
