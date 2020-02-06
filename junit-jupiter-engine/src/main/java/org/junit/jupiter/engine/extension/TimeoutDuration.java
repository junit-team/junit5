/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Timeout;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.5
 */
class TimeoutDuration {

	static TimeoutDuration from(Timeout timeout) {
		return new TimeoutDuration(timeout.value(), timeout.unit());
	}

	private final long value;
	private final TimeUnit unit;

	TimeoutDuration(long value, TimeUnit unit) {
		Preconditions.condition(value > 0, () -> "timeout duration must be a positive number: " + value);
		this.value = value;
		this.unit = Preconditions.notNull(unit, "timeout unit must not be null");
	}

	public long getValue() {
		return value;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TimeoutDuration that = (TimeoutDuration) o;
		return value == that.value && unit == that.unit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, unit);
	}

	@Override
	public String toString() {
		String label = unit.name().toLowerCase();
		if (value == 1 && label.endsWith("s")) {
			label = label.substring(0, label.length() - 1);
		}
		return value + " " + label;
	}

}
