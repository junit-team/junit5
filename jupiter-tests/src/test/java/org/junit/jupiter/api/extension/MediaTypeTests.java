/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.EqualsAndHashCodeAssertions.assertEqualsAndHashCode;
import static org.junit.platform.commons.test.PreconditionAssertions.assertPreconditionViolationFor;
import static org.junit.platform.commons.test.PreconditionAssertions.assertPreconditionViolationNotNullFor;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class MediaTypeTests {

	@Test
	void parse() {
		MediaType mediaType = MediaType.parse("text/plain");
		assertEquals("text/plain", mediaType.toString());
	}

	@Test
	void create() {
		MediaType mediaType = MediaType.create("application", "json");
		assertEquals("application/json", mediaType.toString());
	}

	@Test
	void createWithCharset() {
		MediaType mediaType = MediaType.create("application", "json", StandardCharsets.UTF_8);
		assertEquals("application/json; charset=UTF-8", mediaType.toString());
	}

	@Test
	void parseWithInvalidMediaType() {
		assertPreconditionViolationFor(() -> MediaType.parse("invalid")).withMessage("Invalid media type: 'invalid'");
	}

	@SuppressWarnings("DataFlowIssue")
	@Test
	void parseWithNullMediaType() {
		assertPreconditionViolationNotNullFor("value", () -> MediaType.parse(null));
	}

	@Test
	void equals() {
		MediaType mediaType1 = MediaType.TEXT_PLAIN;
		MediaType mediaType2 = MediaType.parse("text/plain");
		MediaType mediaType3 = MediaType.parse("application/json");

		assertEqualsAndHashCode(mediaType1, mediaType2, mediaType3);
	}

}
