/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.DiscoverySelectorIdentifier;

/**
 * Parser for {@link DiscoverySelectorIdentifier DiscoverySelectorIdentifiers}
 * with a specific prefix.
 * <p>
 * Implementations of this interface can be registered using the Java service
 * loader mechanism to extend the set of supported prefixes for
 * {@link DiscoverySelectorIdentifier DiscoverySelectorIdentifiers}.
 *
 * @since 1.11
 * @see DiscoverySelectors#parse(String)
 */
@API(status = EXPERIMENTAL, since = "1.11")
public interface DiscoverySelectorIdentifierParser {

	/**
	 * Get the prefix that this parser can handle.
	 *
	 * @return the prefix that this parser can handle; never {@code null}
	 */
	String getPrefix();

	/**
	 * Parse the supplied {@link DiscoverySelectorIdentifier}.
	 * <p>
	 * The JUnit Platform will only invoke this method if the supplied
	 * {@link DiscoverySelectorIdentifier} has a prefix that matches the value
	 * returned by {@link #getPrefix()}.
	 *
	 * @param identifier the {@link DiscoverySelectorIdentifier} to parse
	 * @param context the {@link Context} to use for parsing
	 * @return an {@link Optional} containing the parsed {@link DiscoverySelector}; never {@code null}
	 */
	Optional<? extends DiscoverySelector> parse(DiscoverySelectorIdentifier identifier, Context context);

	/**
	 * Context for parsing {@link DiscoverySelectorIdentifier DiscoverySelectorIdentifiers}.
	 */
	interface Context {

		/**
		 * Parse the supplied selector.
		 * <p>
		 * This method is intended to be used by implementations of
		 * {@link DiscoverySelectorIdentifierParser#parse} for selectors that
		 * contain other selectors.
		 */
		Optional<? extends DiscoverySelector> parse(String selector);

	}

}
