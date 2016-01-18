/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.io.Serializable;
import java.util.Objects;

import org.junit.gen5.commons.util.Preconditions;

/**
 * Immutable value object representing a unique test ID.
 *
 * @since 5.0
 */
public final class TestId implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String uniqueId;

	public TestId(String uniqueId) {
		this.uniqueId = Preconditions.notBlank(uniqueId, "uniqueId must not be null or empty");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestId) {
			TestId that = (TestId) obj;
			return Objects.equals(this.uniqueId, that.uniqueId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.uniqueId.hashCode();
	}

	@Override
	public String toString() {
		return this.uniqueId;
	}
}
