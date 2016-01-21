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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;

/**
 * {@code UniqueId} is a class to encapsulate the creation, parsing and display of unique IDs for {@link TestDescriptor}.
 *
 * <p>Instances of this class have value semantics and are immutable.</p>
 */
public class UniqueId implements Cloneable {

	/**
	 * Create a {@code UniqueId} by parsing its string representation {@code uniqueIdString}.
	 *
	 * <p>Throws {@link org.junit.gen5.commons.JUnitException} if the string cannot be parsed.
	 *
	 * @return a properly constructed {@code UniqueId}
	 */
	public static UniqueId parse(String uniqueIdString) {
		return new UniqueIdParser(uniqueIdString, SEGMENT_DELIMITER, TYPE_VALUE_SEPARATOR).parse();
	}

	private static final String SEGMENT_DELIMITER = "/";
	private static final String TYPE_VALUE_SEPARATOR = ":";

	private final List<Segment> segments = new ArrayList<>();

	/**
	 * Create an engine's unique ID by providing the node type {@code segmentType} and {@code engineId}
	 */
	public static UniqueId forEngine(String segmentType, String engineId) {
		List<Segment> segments = Collections.singletonList(new Segment(segmentType, engineId));
		return new UniqueId(segments);
	}

	UniqueId(List<Segment> segments) {
		this.segments.addAll(segments);
	}

	/**
	 * Create and deliver the string representation of the {@code UniqueId}
	 */
	public String getUniqueString() {
		Stream<String> segmentStream = segments.stream().map(this::describe);
		return StringUtils.join(segmentStream, SEGMENT_DELIMITER);
	}

	private String describe(Segment segment) {
		return String.format("[%s%s%s]", segment.getType(), TYPE_VALUE_SEPARATOR, segment.getValue());
	}

	public List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	/**
	 * Construct a new  {@code UniqueId} by appending a {@link Segment} to the end of the current instance
	 * with {@code segmentType} and {@code value}.
	 *
	 * <p>The current instance is left unchanged.</p>
	 *
	 * <p>Both {@code segmentType} and {@code segmentType} must not contain any of the special characters used
	 * fot constructing the string representation. This allows more robust parsing.</p>
	 */
	public UniqueId append(String segmentType, String value) {
		UniqueId clone = new UniqueId(segments);
		clone.segments.add(new Segment(segmentType, value));
		return clone;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UniqueId uniqueId = (UniqueId) o;
		return segments.equals(uniqueId.segments);

	}

	@Override
	public int hashCode() {
		return segments.hashCode();
	}

	public static class Segment {

		private final String type;
		private final String value;

		Segment(String type, String value) {
			checkPrecondition(type);
			checkPrecondition(value);

			this.type = type;
			this.value = value;
		}

		private void checkPrecondition(String type) {
			Preconditions.notEmpty(type, "type or value must not be empty");
			Preconditions.condition(!type.contains(SEGMENT_DELIMITER),
				String.format("type or value must not contain '%s'", SEGMENT_DELIMITER));
			Preconditions.condition(!type.contains(TYPE_VALUE_SEPARATOR),
				String.format("type or value must not contain '%s'", TYPE_VALUE_SEPARATOR));
			Preconditions.condition(!type.contains("["), "type or value must not contain '['");
			Preconditions.condition(!type.contains("]"), "type or value must not contain ']'");
		}

		public String getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Segment segment = (Segment) o;
			return type.equals(segment.type) && value.equals(segment.value);

		}

		@Override
		public int hashCode() {
			return 31 * type.hashCode() + value.hashCode();
		}
	}
}
