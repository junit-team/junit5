/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.api.Executable;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * Simple component that can be used to collect one or more instances of
 * {@link Throwable}.
 *
 * @since 5.0
 */
class ThrowableCollector {

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
	void execute(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable t) {
			add(t);
		}
	}

	/**
	 * Add the supplied {@link Throwable} to this collector.
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
	 * @return an unmodifiable list of throwables
	 */
	List<Throwable> getThrowables() {
		return Collections.unmodifiableList(this.throwables);
	}

	/**
	 * Assert that this {@code ThrowableCollector} is <em>empty</em>.
	 *
	 * <p>If this collector is not empty, the first collected {@link Throwable}
	 * will be thrown with any additional throwables
	 * {@linkplain Throwable#addSuppressed(Throwable) suppressed} in the
	 * first {@code Throwable}. Note, however, that the {@code Throwable}
	 * will not be wrapped. Rather, it will be thrown as-is using a reflective
	 * hack (based on generics and type erasure) that tricks the Java compiler
	 * into believing that the thrown exception is an unchecked exception.
	 */
	void assertEmpty() {
		if (!this.throwables.isEmpty()) {
			Throwable t = this.throwables.get(0);
			this.throwables.stream().skip(1).forEach(t::addSuppressed);
			ReflectionUtils.throwAsRuntimeException(t);
		}
	}

}
