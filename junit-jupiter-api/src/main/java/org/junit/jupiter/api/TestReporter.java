/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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
 * publish report entries.
 *
 * @since 5.0
 * @see #publishEntry(Map)
 * @see #publishEntry(String, String)
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface TestReporter {

	/**
	 * Publish the supplied map of key-value pairs as a report entry.
	 *
	 * @param map the key-value pairs to be published; never {@code null};
	 * keys and values within entries in the map also must not be
	 * {@code null} or blank
	 * @see #publishEntry(String, String)
	 * @see #publishMessage(String)
	 */
	void publishEntry(Map<String, String> map);

	/**
	 * Publish the supplied key-value pair as a report entry.
	 *
	 * @param key the key of the entry to publish
	 * @param value the value of the entry to publish
	 * @see #publishEntry(Map)
	 * @see #publishMessage(String)
	 */
	default void publishEntry(String key, String value) {
		this.publishEntry(Collections.singletonMap(key, value));
	}

	/**
	 * Publish the specified message to be consumed by an
	 * {@code org.junit.platform.engine.EngineExecutionListener}.
	 *
	 * <p>This method follows the same key/value logic as {@code publishEntry}.
	 * It uses the string {@code "message"} as key and the specified
	 * {@code message} argument as value.
	 *
	 * @param message the message to be published; never {@code null} or blank
	 * @see #publishEntry(Map)
	 * @see #publishEntry(String, String)
	 */
	default void publishMessage(String message) {
		this.publishEntry("message", message);
	}

}
