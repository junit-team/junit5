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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * {@code UniqueId} encapsulates the creation, parsing, and display of unique IDs
 * for {@link TestDescriptor TestDescriptors}.
 *
 * <p>Instances of this class have value semantics and are immutable.</p>
 *
 * @since 5.0
 */
@API(Experimental)
public class UniqueId implements Cloneable {

	private static final String TYPE_ENGINE = "engine";

	/**
	 * Parse a {@code UniqueId} from the supplied string representation using the
	 * default format.
	 *
	 * @return a properly constructed {@code UniqueId}
	 * @throws org.junit.gen5.commons.JUnitException if the string cannot be parsed
	 */
	public static UniqueId parse(String uniqueIdString) {
		return UniqueIdFormat.getDefault().parse(uniqueIdString);
	}

	/**
	 * Create an engine's unique ID by from its {@code engineId} using the default
	 * format.
	 */
	public static UniqueId forEngine(String engineId) {
		return root(TYPE_ENGINE, engineId);
	}

	/**
	 * Create a root unique ID from the supplied {@code segmentType} and
	 * {@code nodeValue} using the default format.
	 */
	public static UniqueId root(String segmentType, String nodeValue) {
		List<Segment> segments = Collections.singletonList(new Segment(segmentType, nodeValue));
		return new UniqueId(UniqueIdFormat.getDefault(), segments);
	}

	private final UniqueIdFormat uniqueIdFormat;

	private final List<Segment> segments = new ArrayList<>();

	UniqueId(UniqueIdFormat uniqueIdFormat, List<Segment> segments) {
		this.uniqueIdFormat = uniqueIdFormat;
		this.segments.addAll(segments);
	}

	/**
	 * Create and deliver the string representation of the {@code UniqueId}
	 */
	public String getUniqueString() {
		return uniqueIdFormat.format(this);
	}

	public Optional<Segment> getRoot() {
		return getSegments().stream().findFirst();
	}

	public Optional<String> getEngineId() {
		return getRoot().filter(segment -> segment.getType().equals(TYPE_ENGINE)).map(Segment::getValue);
	}

	public List<Segment> getSegments() {
		return new ArrayList<>(segments);
	}

	/**
	 * Construct a new  {@code UniqueId} by appending a {@link Segment} to the end of the current instance
	 * with {@code segmentType} and {@code value}.
	 *
	 * <p>The current instance is left unchanged.</p>
	 *
	 * <p>Both {@code segmentType} and {@code segmentType} must not contain any of the special characters used
	 * for constructing the string representation. This allows more robust parsing.</p>
	 */
	public UniqueId append(String segmentType, String value) {
		Segment segment = new Segment(segmentType, value);
		return append(segment);
	}

	public UniqueId append(Segment segment) {
		UniqueId clone = new UniqueId(this.uniqueIdFormat, this.segments);
		clone.segments.add(segment);
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

	@Override
	public String toString() {
		return getUniqueString();
	}

	public static class Segment {

		private final String type;
		private final String value;

		Segment(String type, String value) {
			this.type = type;
			this.value = value;
		}

		public String getType() {
			return this.type;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Segment that = (Segment) o;
			return Objects.equals(this.type, that.type) && Objects.equals(this.value, that.value);

		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
				.append("type", this.type)
				.append("value", this.value)
				.toString();
			// @formatter:on
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.value);
		}
	}

}
