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
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.MAINTAINED;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * Represents a media type as defined by
 * <a href="https://tools.ietf.org/html/rfc2045">RFC 2045</a>.
 *
 * @since 5.12
 * @see TestReporter#publishFile(Path, MediaType)
 * @see TestReporter#publishFile(String, MediaType, ThrowingConsumer)
 * @see ExtensionContext#publishFile(String, MediaType, ThrowingConsumer)
 */
@API(status = MAINTAINED, since = "5.13.3")
public final class MediaType {

	private static final Pattern PATTERN;

	static {
		// https://datatracker.ietf.org/doc/html/rfc2045#section-5.1
		String whitespace = "[ \t]*";
		String token = "[0-9A-Za-z!#$%&'*+.^_`|~-]+";
		String quotedString = "\"(?:[^\"\\\\]|\\.)*\"";
		String parameter = ";" + whitespace + token + "=" + "(?:" + token + "|" + quotedString + ")";
		PATTERN = Pattern.compile(token + "/" + token + "(?:" + whitespace + parameter + ")*");
	}

	/**
	 * The {@code text/plain} media type.
	 */
	public static final MediaType TEXT_PLAIN = create("text", "plain");

	/**
	 * The {@code text/plain; charset=UTF-8} media type.
	 */
	public static final MediaType TEXT_PLAIN_UTF_8 = create("text", "plain", UTF_8);

	/**
	 * The {@code application/json} media type.
	 */
	public static final MediaType APPLICATION_JSON = create("application", "json");

	/**
	 * The {@code application/json; charset=UTF-8} media type.
	 * @deprecated Use {@link #APPLICATION_JSON} instead.
	 */
	@Deprecated(since = "6.0")
	@API(status = DEPRECATED, since = "6.0")
	public static final MediaType APPLICATION_JSON_UTF_8 = create("application", "json", UTF_8);

	/**
	 * The {@code application/octet-stream} media type.
	 */
	public static final MediaType APPLICATION_OCTET_STREAM = create("application", "octet-stream");

	/**
	 * The {@code image/jpeg} media type.
	 */
	public static final MediaType IMAGE_JPEG = create("image", "jpeg");

	/**
	 * The {@code image/png} media type.
	 */
	public static final MediaType IMAGE_PNG = create("image", "png");

	private final String value;

	/**
	 * Parse the given media type value.
	 *
	 * <p>Must be valid according to
	 * <a href="https://tools.ietf.org/html/rfc2045">RFC 2045</a>.
	 *
	 * @param value the media type value to parse; never {@code null} or blank
	 * @return the parsed media type
	 * @throws PreconditionViolationException if the value is not a valid media type
	 */
	public static MediaType parse(String value) {
		return new MediaType(value);
	}

	/**
	 * Create a media type with the given type and subtype.
	 *
	 * @param type the type; never {@code null} or blank
	 * @param subtype the subtype; never {@code null} or blank
	 * @return the media type
	 */
	public static MediaType create(String type, String subtype) {
		Preconditions.notBlank(type, "type must not be null or blank");
		Preconditions.notBlank(subtype, "subtype must not be null or blank");
		return new MediaType(type.strip() + "/" + subtype.strip());
	}

	/**
	 * Create a media type with the given type, subtype, and charset.
	 *
	 * @param type the type; never {@code null} or blank
	 * @param subtype the subtype; never {@code null} or blank
	 * @param charset the charset; never {@code null}
	 * @return the media type
	 */
	public static MediaType create(String type, String subtype, Charset charset) {
		Preconditions.notBlank(type, "type must not be null or blank");
		Preconditions.notBlank(subtype, "subtype must not be null or blank");
		Preconditions.notNull(charset, "charset must not be null");
		return new MediaType(type.strip() + "/" + subtype.strip() + "; charset=" + charset.name());
	}

	private MediaType(String value) {
		String strippedValue = Preconditions.notBlank(value, "value must not be null or blank").strip();
		Matcher matcher = PATTERN.matcher(strippedValue);
		Preconditions.condition(matcher.matches(), () -> "Invalid media type: '" + strippedValue + "'");
		this.value = strippedValue;
	}

	/**
	 * {@return a string representation of this media type}
	 */
	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MediaType that = (MediaType) o;
		return Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.value);
	}

}
