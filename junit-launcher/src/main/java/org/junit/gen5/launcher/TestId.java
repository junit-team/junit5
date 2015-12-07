/*
 * Copyright 2015 the original author or authors.
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

public final class TestId implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String uniqueId;

	public TestId(String uniqueId) {
		Preconditions.notNull(uniqueId, "uniqueId must not be null");
		this.uniqueId = uniqueId;
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
		return uniqueId.hashCode();
	}

	@Override
	public String toString() {
		return uniqueId;
	}

}
