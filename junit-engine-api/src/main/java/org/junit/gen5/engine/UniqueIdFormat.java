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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.UniqueId.Segment;

/**
 * Used to parse a unique ID string representation into a {@link UniqueId}
 * or to format a {@link UniqueId uniqueId} into a string representation.
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
		String[] parts = source.split(Character.toString(segmentDelimiter));
		List<Segment> segments = Arrays.stream(parts).map(this::createSegment).collect(Collectors.toList());
		return new UniqueId(segments);
	}

	private Segment createSegment(String segmentString) {
		Matcher segmentMatcher = segmentPattern.matcher(segmentString);
		if (!segmentMatcher.matches())
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId segment", segmentString));
		String type = checkAllowed(segmentMatcher.group(1));
		String value = checkAllowed(segmentMatcher.group(2));
		return new Segment(type, value);
	}

	private String checkAllowed(String typeOrValue) {
		checkDoesNotContain(typeOrValue, segmentDelimiter);
		checkDoesNotContain(typeOrValue, typeValueSeparator);
		checkDoesNotContain(typeOrValue, openSegment);
		checkDoesNotContain(typeOrValue, closeSegment);
		return typeOrValue;
	}

	private void checkDoesNotContain(String typeOrValue, char forbiddenString) {
		Preconditions.condition(typeOrValue.indexOf(forbiddenString) < 0,
			String.format("type or value '%s' must not contain '%s'", typeOrValue, forbiddenString));
	}

	/**
	 * Create and deliver the string representation of the {@code UniqueId}
	 */
	public String format(UniqueId uniqueId) {
		Stream<String> segmentStream = uniqueId.getSegments().stream().map(this::describe);
		return StringUtils.join(segmentStream, Character.toString(segmentDelimiter));
	}

	private String describe(Segment segment) {
		return String.format("[%s%s%s]", segment.getType(), typeValueSeparator, segment.getValue());
	}

}
