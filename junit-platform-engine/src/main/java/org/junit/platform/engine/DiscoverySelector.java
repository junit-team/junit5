/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.engine.discovery.DiscoverySelectorIdentifierParser;

/**
 * A selector defines what a {@link TestEngine} can use to discover tests
 * &mdash; for example, the name of a Java class, the path to a file or
 * directory, etc.
 *
 * @since 1.0
 * @see EngineDiscoveryRequest
 * @see org.junit.platform.engine.discovery.DiscoverySelectors
 */
@API(status = STABLE, since = "1.0")
public interface DiscoverySelector {

	/**
	 * Return a {@link String} representation of this selector.
	 * <p>
	 * The returned string has to be parsable by a corresponding
	 * {@link DiscoverySelectorIdentifierParser}.
	 * <p>
	 * @return a {@link String} representation of this selector or empty if it is not supported.
	 */
	default Optional<String> toSelectorString() {
		return Optional.empty();
	}

}
