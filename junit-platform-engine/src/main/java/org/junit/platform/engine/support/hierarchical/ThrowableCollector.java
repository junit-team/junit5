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

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.engine.TestExecutionResult.aborted;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestExecutionResult;
import org.opentest4j.TestAbortedException;

/**
 * Simple component that can be used to collect one or more instances of
 * {@link Throwable}.
 *
 * @since 5.2
 */
@API(status = MAINTAINED, since = "5.2")
public class ThrowableCollector {

	private Throwable throwable;

	/**
	 * Execute the supplied {@link Executable} and collect any {@link Throwable}
	 * thrown during the execution.
	 *
	 * <p>If the {@code Executable} throws a <em>blacklisted</em> exception
	 * &mdash; for example, an {@link OutOfMemoryError} &mdash; this method will
	 * rethrow it.
	 *
	 * @param executable the {@code Executable} to execute
	 * @see #assertEmpty()
	 */
	public void execute(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			add(t);
		}
	}

	/**
	 * Add the supplied {@link Throwable} to this {@code ThrowableCollector}.
	 *
	 * @param t the {@code Throwable} to add
	 * @see #execute(Executable)
	 * @see #assertEmpty()
	 */
	private void add(Throwable t) {
		Preconditions.notNull(t, "Throwable must not be null");

		if (this.throwable == null) {
			this.throwable = t;
		}
		else if (this.throwable instanceof TestAbortedException && !(t instanceof TestAbortedException)) {
			t.addSuppressed(this.throwable);
			this.throwable = t;
		}
		else if (throwable != t) {
			this.throwable.addSuppressed(t);
		}
	}

	/**
	 * Get the first {@link Throwable} collected by this
	 * {@code ThrowableCollector}.
	 *
	 * <p>If this collector is not empty, the first collected {@code Throwable}
	 * will be returned with any additional throwables
	 * {@linkplain Throwable#addSuppressed(Throwable) suppressed} in the
	 * first {@code Throwable}.
	 *
	 * <p>If the first collected {@code Throwable} was a
	 * {@link TestAbortedException} and at least one later collected throwable
	 * wasn't, the first non-{@code TestAbortedException} will be returned with
	 * the {@code TestAbortedException} and any additional throwables
	 * {@linkplain Throwable#addSuppressed(Throwable) suppressed} inside.
	 *
	 * @return the first collected {@code Throwable} or {@code null} if this
	 * {@code ThrowableCollector} is empty
	 * @see #isEmpty()
	 * @see #assertEmpty()
	 */
	public Throwable getThrowable() {
		return this.throwable;
	}

	/**
	 * Determine if this {@code ThrowableCollector} is <em>empty</em> (i.e.,
	 * has not collected any {@code Throwables}).
	 */
	public boolean isEmpty() {
		return (this.throwable == null);
	}

	/**
	 * Determine if this {@code ThrowableCollector} is <em>not empty</em> (i.e.,
	 * has collected at least one {@code Throwable}).
	 */
	public boolean isNotEmpty() {
		return (this.throwable != null);
	}

	public void assertNotSame(Throwable otherThrowable) {
		if (this.throwable != otherThrowable) {
			assertEmpty();
		}
	}

	/**
	 * Assert that this {@code ThrowableCollector} is <em>empty</em> (i.e.,
	 * has not collected any {@code Throwables}).
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
		if (!isEmpty()) {
			ExceptionUtils.throwAsUncheckedException(this.throwable);
		}
	}

	/**
	 * Convert the collected throwables into a {@link TestExecutionResult}.
	 *
	 * @return {@linkplain TestExecutionResult#aborted aborted} if the collected
	 * {@code throwable} is a {@link TestAbortedException};
	 * {@linkplain TestExecutionResult#failed failed} if any other
	 * {@link Throwable} was collected; and
	 * {@linkplain TestExecutionResult#successful successful} otherwise
	 */
	public TestExecutionResult toTestExecutionResult() {
		if (isEmpty()) {
			return successful();
		}
		if (throwable instanceof TestAbortedException) {
			return aborted(throwable);
		}
		return failed(throwable);
	}

	/**
	 * Functional interface for an executable block of code that may throw a
	 * {@link Throwable}.
	 */
	@FunctionalInterface
	public interface Executable {

		/**
		 * Execute this executable.
		 *
		 * @throws TestAbortedException to signal abortion
		 * @throws Throwable to signal failure
		 */
		void execute() throws Throwable;

	}

}
