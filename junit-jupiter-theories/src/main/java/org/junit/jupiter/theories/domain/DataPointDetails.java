/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.domain;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;

/**
 * Domain object that contains all of the information about a data point.
 */
@API(status = INTERNAL, since = "5.2")
public class DataPointDetails {
	private final Object value;
	private final List<String> qualifiers;
	private final String sourceName;

	/**
	 * Constructor.
	 *
	 * @param value this data point's value
	 * @param qualifiers qualifiers (if any) for this data point
	 * @param sourceName the name of the source that produced this data point
	 */
	public DataPointDetails(Object value, List<String> qualifiers, String sourceName) {
		this.value = value;
		this.qualifiers = Collections.unmodifiableList(new ArrayList<>(qualifiers));
		this.sourceName = sourceName;
	}

	/**
	 * @return this data point's value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return qualifiers (if any) for this data point
	 */
	public List<String> getQualifiers() {
		return qualifiers;
	}

	/**
	 * @return the name of the source that produced this data point
	 */
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String toString() {
		return value + " (Source: " + sourceName + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DataPointDetails)) {
			return false;
		}
		DataPointDetails other = (DataPointDetails) o;
		return Objects.equals(value, other.value) && Objects.equals(qualifiers, other.qualifiers)
				&& Objects.equals(sourceName, other.sourceName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, qualifiers, sourceName);
	}
}
