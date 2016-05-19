/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@API(Experimental)
public interface ConfigurationParameters {

	/**
	 * Get the configuration property stored under the specified {@code key}.
	 *
	 * <p>If no such key is present in this {@code ConfigurationParameters},
	 * an attempt will be made to look up the value as a Java system property.
	 *
	 * @param key the key to look up; never {@code null} or empty
	 * @return an {@code Optional} containing the potential value
	 */
	Optional<String> get(String key);

	/**
	 * Get the number of configuration properties stored directly in this
	 * {@code ConfigurationParameters}.
	 */
	int getSize();

}
