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

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.UniqueId.Segment;

/**
 * Used to parse a unique ID string representation into a {@link UniqueId}
 * or to format a {@link UniqueId} into a string representation.
 *
 * @since 5.0
 */
public class UniqueIdFormat {

	private final char openSegment;
	private final char closeSegment;
	private final char segmentDelimiter;
	private final char typeValueSeparator;
	private final Pattern segmentPattern;

	public static UniqueIdFormat getDefault() {
		return new UniqueIdFormat('[', ':', ']', '/');
	}

	public UniqueIdFormat(char openSegment, char typeValueSeparator, char closeSegment, char segmentDelimiter) {
		this.openSegment = openSegment;
		this.typeValueSeparator = typeValueSeparator;
		this.closeSegment = closeSegment;
		this.segmentDelimiter = segmentDelimiter;
		this.segmentPattern = Pattern.compile(
			String.format("\\%s(.+)\\%s(.+)\\%s", openSegment, typeValueSeparator, closeSegment));
	}

	public UniqueId parse(String source) {
		String[] parts = source.split(Character.toString(this.segmentDelimiter));
		List<Segment> segments = Arrays.stream(parts).map(this::createSegment).collect(Collectors.toList());
		return new UniqueId(segments);
	}

	private Segment createSegment(String segmentString) {
		Matcher segmentMatcher = this.segmentPattern.matcher(segmentString);
		if (!segmentMatcher.matches()) {
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId segment", segmentString));
		}
		String type = checkAllowed(segmentMatcher.group(1));
		String value = checkAllowed(segmentMatcher.group(2));
		return new Segment(type, value);
	}

	private String checkAllowed(String typeOrValue) {
		checkDoesNotContain(typeOrValue, this.segmentDelimiter);
		checkDoesNotContain(typeOrValue, this.typeValueSeparator);
		checkDoesNotContain(typeOrValue, this.openSegment);
		checkDoesNotContain(typeOrValue, this.closeSegment);
		return typeOrValue;
	}

	private void checkDoesNotContain(String typeOrValue, char forbiddenCharacter) {
		Preconditions.condition(typeOrValue.indexOf(forbiddenCharacter) < 0,
			() -> String.format("type or value '%s' must not contain '%s'", typeOrValue, forbiddenCharacter));
	}

	/**
	 * Format and return the string representation of the supplied {@code UniqueId}.
	 */
	public String format(UniqueId uniqueId) {
		// @formatter:off
		return uniqueId.getSegments().stream()
			.map(this::describe)
			.collect(joining(Character.toString(this.segmentDelimiter)));
		// @formatter:on
	}

	private String describe(Segment segment) {
		return String.format("[%s%s%s]", segment.getType(), this.typeValueSeparator, segment.getValue());
	}

}
