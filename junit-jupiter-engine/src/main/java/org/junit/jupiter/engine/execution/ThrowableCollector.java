/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Executable;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * Simple component that can be used to collect one or more instances of
 * {@link Throwable}.
 *
 * @since 5.0
 */
@API(Internal)
public class ThrowableCollector {

	private final List<Throwable> throwables = new ArrayList<>();

	/**
	 * Execute the supplied {@link Executable} and {@link #add collect} any
	 * {@link Throwable} thrown during the execution.
	 *
	 * @param executable the {@code Executable} to execute
	 * @see #add(Throwable)
	 * @see #getThrowables()
	 * @see #assertEmpty()
	 */
	public void execute(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable t) {
			add(t);
		}
	}

	/**
	 * Add the supplied {@link Throwable} to this {@code ThrowableCollector}.
	 *
	 * @param t the {@code Throwable} to add
	 * @see #execute(Executable)
	 * @see #getThrowables()
	 * @see #assertEmpty()
	 */
	void add(Throwable t) {
		Preconditions.notNull(t, "Throwable must not be null");
		this.throwables.add(t);
	}

	/**
	 * Get the list of {@link Throwable Throwables} collected by this
	 * {@code ThrowableCollector}.
	 *
	 * @return an unmodifiable list of the throwables collected by this
	 * {@code ThrowableCollector}
	 */
	List<Throwable> getThrowables() {
		return Collections.unmodifiableList(this.throwables);
	}

	/**
	 * Determine if this {@code ThrowableCollector} is <em>empty</em> (i.e.,
	 * has not collected any {@link #getThrowables() Throwables}).
	 */
	public boolean isEmpty() {
		return this.throwables.isEmpty();
	}

	/**
	 * Assert that this {@code ThrowableCollector} is <em>empty</em> (i.e.,
	 * has not collected any {@link #getThrowables() Throwables}).
	 *
	 * <p>If this collector is not empty, the first collected {@code Throwable}
	 * will be thrown with any additional throwables
	 * {@linkplain Throwable#addSuppressed(Throwable) suppressed} in the
	 * first {@code Throwable}. Note, however, that the {@code Throwable}
	 * will not be wrapped. Rather, it will be
	 * {@linkplain ExceptionUtils#throwAsUncheckedException masked}
	 * as an unchecked exception.
	 *
	 * @see ExceptionUtils#throwAsUncheckedException(Throwable)
	 */
	public void assertEmpty() {
		if (!this.throwables.isEmpty()) {
			Throwable t = this.throwables.get(0);
			this.throwables.stream().skip(1).forEach(t::addSuppressed);
			ExceptionUtils.throwAsUncheckedException(t);
		}
	}

}
