/*
 * Copyright 2015-2017 the original author or authors.
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
 * Parameters of type {@code TestReporter} can be injected into methods of
 * test classes annotated with {@link BeforeEach @BeforeEach},
 * {@link AfterEach @AfterEach}, and {@link Test @Test}.
 *
 * <p>Within such methods this instance of type {@code TestReporter} can be
 * used to publish report entries.
 *
 * @since 5.0
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface TestReporter {

	/**
	 * Publish the supplied values as a report entry.
	 *
	 * @param  values the map to be published for this entry
	 */
	void publishEntry(Map<String, String> values);

	default void publishEntry(String key, String value) {
		this.publishEntry(Collections.singletonMap(key, value));
	}

}
