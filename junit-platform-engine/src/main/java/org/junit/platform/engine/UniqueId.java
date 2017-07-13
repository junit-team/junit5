/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@code UniqueId} encapsulates the creation, parsing, and display of unique IDs
 * for {@link TestDescriptor TestDescriptors}.
 *
 * <p>Instances of this class have value semantics and are immutable.
 *
 * @since 1.0
 */
@API(Experimental)
public class UniqueId implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String ENGINE_SEGMENT_TYPE = "engine";

	/**
	 * Parse a {@code UniqueId} from the supplied string representation using the
	 * default format.
	 *
	 * @param uniqueId the string representation to parse; never {@code null} or blank
	 * @return a properly constructed {@code UniqueId}
	 * @throws JUnitException if the string cannot be parsed
	 */
	public static UniqueId parse(String uniqueId) throws JUnitException {
		Preconditions.notBlank(uniqueId, "Unique ID string must not be null or blank");
		return UniqueIdFormat.getDefault().parse(uniqueId);
	}

	/**
	 * Create an engine's unique ID from its {@code engineId} using the default
	 * format.
	 *
	 * <p>The engine ID will be stored in a {@link Segment} with
	 * {@link Segment#getType type} {@code "engine"}.
	 *
	 * @param engineId the engine ID; never {@code null} or blank
	 * @see #root(String, String)
	 */
	public static UniqueId forEngine(String engineId) {
		Preconditions.notBlank(engineId, "engineId must not be null or blank");
		return root(ENGINE_SEGMENT_TYPE, engineId);
	}

	/**
	 * Create a root unique ID from the supplied {@code segmentType} and
	 * {@code value} using the default format.
	 *
	 * @param segmentType the segment type; never {@code null} or blank
	 * @param value the value; never {@code null} or blank
	 * @see #forEngine(String)
	 */
	public static UniqueId root(String segmentType, String value) {
		Preconditions.notBlank(segmentType, "segmentType must not be null or blank");
		Preconditions.notBlank(value, "value must not be null or blank");
		return new UniqueId(UniqueIdFormat.getDefault(), new Segment(segmentType, value));
	}

	private final UniqueIdFormat uniqueIdFormat;
	private final List<Segment> segments;

	private UniqueId(UniqueIdFormat uniqueIdFormat, Segment segment) {
		this.uniqueIdFormat = uniqueIdFormat;
		this.segments = singletonList(segment);
	}

	/**
	 * Initialize a {@code UniqueId} instance.
	 *
	 * @implNote A defensive copy of the segment list is <b>not</b> created by
	 * this implementation. All callers should immediately drop the reference
	 * to the list instance that they pass into this constructor.
	 */
	UniqueId(UniqueIdFormat uniqueIdFormat, List<Segment> segments) {
		this.uniqueIdFormat = uniqueIdFormat;
		this.segments = unmodifiableList(segments);
	}

	final Optional<Segment> getRoot() {
		return this.segments.isEmpty() ? Optional.empty() : Optional.of(this.segments.get(0));
	}

	/**
	 * Get the engine ID stored in this {@code UniqueId}, if available.
	 *
	 * @see #forEngine(String)
	 */
	public final Optional<String> getEngineId() {
		return getRoot().filter(segment -> segment.getType().equals(ENGINE_SEGMENT_TYPE)).map(Segment::getValue);
	}

	/**
	 * Get the immutable list of {@linkplain Segment segments} that make up this
	 * {@code UniqueId}.
	 */
	public final List<Segment> getSegments() {
		return this.segments;
	}

	/**
	 * Construct a new {@code UniqueId} by appending a new {@link Segment}, based
	 * on the supplied {@code segmentType} and {@code value}, to the end of this
	 * {@code UniqueId}.
	 *
	 * <p>This {@code UniqueId} will not be modified.
	 *
	 * <p>Neither the {@code segmentType} nor the {@code value} may contain any
	 * of the special characters used for constructing the string representation
	 * of this {@code UniqueId}.
	 *
	 * @param segmentType the type of the segment; never {@code null} or blank
	 * @param value the value of the segment; never {@code null} or blank
	 */
	public final UniqueId append(String segmentType, String value) {
		Preconditions.notBlank(segmentType, "segmentType must not be null or blank");
		Preconditions.notBlank(value, "value must not be null or blank");
		List<Segment> baseSegments = new ArrayList<>(this.segments);
		baseSegments.add(new Segment(segmentType, value));
		return new UniqueId(this.uniqueIdFormat, baseSegments);
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

		UniqueId that = (UniqueId) o;
		return this.segments.equals(that.segments);
	}

	@Override
	public int hashCode() {
		return this.segments.hashCode();
	}

	/**
	 * Generate the unique, formatted string representation of this {@code UniqueId}
	 * using the configured {@link UniqueIdFormat}.
	 */
	@Override
	public String toString() {
		return this.uniqueIdFormat.format(this);
	}

	/**
	 * A segment of a {@link UniqueId} comprises a <em>type</em> and a
	 * <em>value</em>.
	 */
	@API(Experimental)
	public static class Segment implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String type;
		private final String value;

		/**
		 * Create a new {@code Segment} using the supplied {@code type} and
		 * {@code value}.
		 *
		 * @param type the type of this segment
		 * @param value the value of this segment
		 */
		Segment(String type, String value) {
			this.type = type;
			this.value = value;
		}

		/**
		 * Get the type of this segment.
		 */
		public String getType() {
			return this.type;
		}

		/**
		 * Get the value of this segment.
		 */
		public String getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.value);
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

	}

}
