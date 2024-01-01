/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Collections;
import java.util.Map;

import org.apiguardian.api.API;

/**
 * Parameters of type {@code TestReporter} can be injected into
 * {@link BeforeEach @BeforeEach} and {@link AfterEach @AfterEach} lifecycle
 * methods as well as methods annotated with {@link Test @Test},
 * {@link RepeatedTest @RepeatedTest},
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest},
 * {@link TestFactory @TestFactory}, etc.
 *
 * <p>Within such methods the injected {@code TestReporter} can be used to
 * publish <em>report entries</em> for the current container or test to the
 * reporting infrastructure.
 *
 * @since 5.0
 * @see #publishEntry(Map)
 * @see #publishEntry(String, String)
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface TestReporter {

	/**
	 * Publish the supplied map of key-value pairs as a <em>report entry</em>.
	 *
	 * @param map the key-value pairs to be published; never {@code null};
	 * keys and values within entries in the map also must not be
	 * {@code null} or blank
	 * @see #publishEntry(String, String)
	 * @see #publishEntry(String)
	 */
	void publishEntry(Map<String, String> map);

	/**
	 * Publish the supplied key-value pair as a <em>report entry</em>.
	 *
	 * @param key the key of the entry to publish; never {@code null} or blank
	 * @param value the value of the entry to publish; never {@code null} or blank
	 * @see #publishEntry(Map)
	 * @see #publishEntry(String)
	 */
	default void publishEntry(String key, String value) {
		this.publishEntry(Collections.singletonMap(key, value));
	}

	/**
	 * Publish the supplied value as a <em>report entry</em>.
	 *
	 * <p>This method delegates to {@link #publishEntry(String, String)},
	 * supplying {@code "value"} as the key and the supplied {@code value}
	 * argument as the value.
	 *
	 * @param value the value to be published; never {@code null} or blank
	 * @since 5.3
	 * @see #publishEntry(Map)
	 * @see #publishEntry(String, String)
	 */
	@API(status = STABLE, since = "5.3")
	default void publishEntry(String value) {
		this.publishEntry("value", value);
	}

}
