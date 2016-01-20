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

public class UniqueId implements Cloneable {

	public static UniqueId parse(String uniqueIdString) {
		return new UniqueIdParser(uniqueIdString, SEGMENT_DELIMITER, TYPE_VALUE_SEPARATOR).parse();
	}

	public static final String TYPE_ENGINE = "engine";

	private static final String SEGMENT_DELIMITER = "/";
	private static final String TYPE_VALUE_SEPARATOR = ":";

	private final List<Segment> segments = new ArrayList<>();

	public UniqueId(String engineId) {
		segments.add(new Segment(TYPE_ENGINE, engineId));
	}

	UniqueId(List<Segment> segments) {
		this.segments.addAll(segments);
	}

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

	public UniqueId append(String type, String value) {
		UniqueId clone = new UniqueId(segments);
		clone.segments.add(new Segment(type, value));
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
