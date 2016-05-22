/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.Objects;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * @since 5.0
 */
@API(Experimental)
public class FilePosition implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int line;
	private final int column;

	public FilePosition(int line, int column) {
		this.line = line;
		this.column = column;
	}

	public int getLine() {
		return this.line;
	}

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
