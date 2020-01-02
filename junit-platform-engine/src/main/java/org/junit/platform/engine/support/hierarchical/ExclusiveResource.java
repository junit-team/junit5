/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * An exclusive resource identified by a key with a lock mode that is used to
 * synchronize access to shared resources when executing nodes in parallel.
 *
 * @see Node#getExecutionMode()
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public class ExclusiveResource {

	private final String key;
	private final LockMode lockMode;
	private int hash;

	/**
	 * Create a new {@code ExclusiveResource}.
	 *
	 * @param key the identifier of the resource; never {@code null} or blank
	 * @param lockMode the lock mode to use to synchronize access to the
	 * resource; never {@code null}
	 */
	public ExclusiveResource(String key, LockMode lockMode) {
		this.key = Preconditions.notBlank(key, "key must not be blank");
		this.lockMode = Preconditions.notNull(lockMode, "lockMode must not be null");
	}

	/**
	 * Get the key of this resource.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the lock mode of this resource.
	 */
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
	 * {@code LockMode} translates to the respective {@link ReadWriteLock}
	 * locks.
	 *
	 * @implNote Enum order is important, since it can be used to sort locks, so
	 * the stronger mode has to be first.
	 */
	public enum LockMode {

		/**
		 * Require read and write access to the resource.
		 *
		 * @see ReadWriteLock#writeLock()
		 */
		READ_WRITE,

		/**
		 * Require only read access to the resource.
		 *
		 * @see ReadWriteLock#readLock()
		 */
		READ

	}

}
