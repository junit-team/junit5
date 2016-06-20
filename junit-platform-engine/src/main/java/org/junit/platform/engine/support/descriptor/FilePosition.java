/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.Objects;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Position inside a file represented by {@linkplain #getLine line} and
 * {@linkplain #getColumn column}.
 *
 * @since 5.0
 */
@API(Experimental)
public class FilePosition implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int line;
	private final int column;

	/**
	 * Create a new {@code FilePosition} using the supplied {@code line} and
	 * {@code column}.
	 *
	 * @param line the line (1-based)
	 * @param column the column (1-based)
	 */
	public FilePosition(int line, int column) {
		this.line = line;
		this.column = column;
	}

	/**
	 * Get the line (1-based) of this position.
	 */
	public int getLine() {
		return this.line;
	}

	/**
	 * Get the column (1-based) of this position.
	 */
	public int getColumn() {
		return this.column;
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
		return this.line == that.line && this.column == that.column;
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
				.append("column", this.column)
				.toString();
		// @formatter:on
	}

}
