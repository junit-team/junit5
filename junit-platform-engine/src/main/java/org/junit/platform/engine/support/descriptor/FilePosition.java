/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Position inside a file represented by {@linkplain #getLine line} and
 * {@linkplain #getColumn column} numbers.
 *
 * @since 1.0
 */
@API(status = STABLE, since = "1.0")
public class FilePosition implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new {@code FilePosition} using the supplied {@code line} number
	 * and an undefined column number.
	 *
	 * @param line the line number; must be greater than zero
	 */
	public static FilePosition from(int line) {
		return new FilePosition(line);
	}

	/**
	 * Create a new {@code FilePosition} using the supplied {@code line} and
	 * {@code column} numbers.
	 *
	 * @param line the line number; must be greater than zero
	 * @param column the column number; must be greater than zero
	 */
	public static FilePosition from(int line, int column) {
		return new FilePosition(line, column);
	}

	private final int line;
	private final Integer column;

	private FilePosition(int line) {
		Preconditions.condition(line > 0, "line number must be greater than zero");
		this.line = line;
		this.column = null;
	}

	private FilePosition(int line, int column) {
		Preconditions.condition(line > 0, "line number must be greater than zero");
		Preconditions.condition(column > 0, "column number must be greater than zero");
		this.line = line;
		this.column = Integer.valueOf(column);
	}

	/**
	 * Get the line number of this {@code FilePosition}.
	 *
	 * @return the line number
	 */
	public int getLine() {
		return this.line;
	}

	/**
	 * Get the column number of this {@code FilePosition}, if available.
	 *
	 * @return an {@code Optional} containing the column number; never
	 * {@code null} but potentially empty
	 */
	public Optional<Integer> getColumn() {
		return Optional.ofNullable(this.column);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FilePosition that = (FilePosition) o;
		return (this.line == that.line) && Objects.equals(this.column, that.column);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.line, this.column);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("line", this.line)
				.append("column", getColumn().orElse(-1))
				.toString();
		// @formatter:on
	}

}
