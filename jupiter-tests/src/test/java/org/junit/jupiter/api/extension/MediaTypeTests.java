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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.EqualsAndHashCodeAssertions.assertEqualsAndHashCode;
import static org.junit.platform.commons.test.PreconditionAssertions.assertPreconditionViolationFor;
import static org.junit.platform.commons.test.PreconditionAssertions.assertPreconditionViolationNotNullFor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MediaTypeTests {

	@Test
	void equals() {
		var mediaType1 = MediaType.TEXT_PLAIN;
		var mediaType2 = MediaType.parse(mediaType1.toString());
		var mediaType3 = MediaType.APPLICATION_JSON;

		assertEqualsAndHashCode(mediaType1, mediaType2, mediaType3);
	}

	@Nested
	class ParseTests {

		@Test
		@SuppressWarnings("DataFlowIssue") // MediaType.parse() parameter is not @Nullable
		void parseWithNullMediaType() {
			assertPreconditionViolationNotNullFor("value", () -> MediaType.parse(null));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "   ", "/", " / ", "type", "type/", "/subtype" })
		void parseWithInvalidMediaType(String mediaType) {
			assertPreconditionViolationFor(() -> MediaType.parse(mediaType))//
					.withMessage("Invalid media type: '%s'", mediaType.strip());
		}

		@ParameterizedTest
		@ValueSource(strings = { "text/plain", "   text/plain   ", "text/plain; charset=UTF-8",
				"\t text/plain; charset=UTF-8 \t" })
		void parse(String value) {
			assertThat(MediaType.parse(value)).hasToString(value.strip());
		}
	}

	@Nested
	class CreateTests {

		@Test
		@SuppressWarnings("DataFlowIssue") // MediaType.create() parameters are not @Nullable
		void createWithNullType() {
			assertPreconditionViolationNotNullFor("type", () -> MediaType.create(null, "json"));
		}

		@Test
		@SuppressWarnings("DataFlowIssue") // MediaType.create() parameters are not @Nullable
		void createWithNullSubtype() {
			assertPreconditionViolationNotNullFor("subtype", () -> MediaType.create("json", null));
		}

		@Test
		@SuppressWarnings("DataFlowIssue") // MediaType.create() parameters are not @Nullable
		void createWithNullCharset() {
			assertPreconditionViolationNotNullFor("charset", () -> MediaType.create("application", "json", null));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "   ", "/", " / ", "type/", "/subtype" })
		void createWithInvalidType(String type) {
			assertPreconditionViolationFor(() -> MediaType.create(type, "json"))//
					.withMessage("Invalid media type: '%s/json'", type.strip());
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "   ", "/", " / ", "type/", "/subtype" })
		void createWithInvalidSubtype(String subtype) {
			assertPreconditionViolationFor(() -> MediaType.create("application", subtype))//
					.withMessage("Invalid media type: 'application/%s'", subtype.strip());
		}

		@Test
		void create() {
			assertThat(MediaType.create("application", "json")).hasToString("application/json");
		}

		@Test
		void createWithCharset() {
			assertThat(MediaType.create("text", "plain", UTF_8)).hasToString("text/plain; charset=UTF-8");
		}
	}

}
