/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * Configuration parameters that {@link TestEngine TestEngines} may use to
 * influence test discovery and execution.
 *
 * <p>For example, the JUnit Jupiter engine uses a configuration parameter to
 * enable IDEs and build tools to deactivate conditional test execution.
 *
 * @see TestEngine
 * @see EngineDiscoveryRequest
 * @see ExecutionRequest
 * @since 1.0
 */
@API(Experimental)
public interface ConfigurationParameters {

	/**
	 * Get the configuration property stored under the specified {@code key}.
	 *
	 * <p>If no such key is present in this {@code ConfigurationParameters},
	 * an attempt will be made to look up the value as a Java system property.
	 *
	 * @param key the key to look up; never {@code null} or blank
	 * @return an {@code Optional} containing the potential value
	 */
	Optional<String> get(String key);

	/**
	 * Get the number of configuration properties stored directly in this
	 * {@code ConfigurationParameters}.
	 */
	int size();

}
