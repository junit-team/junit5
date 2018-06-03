/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.Objects;

import org.junit.platform.commons.util.ToStringBuilder;

public class ExclusiveResource {

	private final String key;
	private final LockMode lockMode;
	private int hash;

	public ExclusiveResource(String key, LockMode lockMode) {
		this.key = key;
		this.lockMode = lockMode;
	}

	public String getKey() {
		return key;
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ExclusiveResource that = (ExclusiveResource) o;
		return Objects.equals(key, that.key) && lockMode == that.lockMode;
	}

	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0) {
			h = hash = Objects.hash(key, lockMode);
		}
		return h;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("key", key).append("lockMode", lockMode).toString();
	}

	/**
	 * LockMode translates to the respective {@link java.util.concurrent.locks.ReentrantReadWriteLock} locks.
	 *
	 * <p>Enum order is important, since it can be used to sort locks, so the stronger mode has to be first.
	 */
	public enum LockMode {
		READ_WRITE, READ
	}
}
