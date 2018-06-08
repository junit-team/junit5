/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.config;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * View of {@link ConfigurationParameters} that applies a supplied prefix to all
 * queries.
 *
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public class PrefixedConfigurationParameters implements ConfigurationParameters {

	private final ConfigurationParameters delegate;
	private final String prefix;

	/**
	 * Create a new view of the supplied {@link ConfigurationParameters} that
	 * applies the supplied prefix to all queries.
	 */
	public PrefixedConfigurationParameters(ConfigurationParameters delegate, String prefix) {
		this.delegate = delegate;
		this.prefix = prefix;
	}

	@Override
	public Optional<String> get(String key) {
		return delegate.get(prefixed(key));
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return delegate.getBoolean(prefixed(key));
	}

	@Override
	public <T> Optional<T> get(String key, Function<String, T> transformer) {
		return delegate.get(prefixed(key), transformer);
	}

	private String prefixed(String key) {
		return prefix + key;
	}

	@Override
	public int size() {
		return delegate.size();
	}
}
