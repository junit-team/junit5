/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.support;

import static java.lang.String.format;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.runner.Description;

/**
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public class UniqueIdReader implements Function<Description, Serializable> {

	private final Logger logger;
	private final String fieldName;

	public UniqueIdReader(Logger logger) {
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
			logger.warn(() -> format("Could not read unique ID for Description; using display name instead: %s",
				description.toString()));
			return description.getDisplayName();
		});
	}

}
