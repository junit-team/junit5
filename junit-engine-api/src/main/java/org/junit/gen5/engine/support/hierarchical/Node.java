/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.gen5.commons.meta.API;

/**
 * A <em>node</em> within the execution hierarchy.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the {@code HierarchicalTestEngine}
 * @since 5.0
 * @see HierarchicalTestEngine
 * @see Container
 * @see Leaf
 */
@API(Experimental)
public interface Node<C extends EngineExecutionContext> {

	/**
	 * Prepare the supplied {@code context} prior to execution.
	 *
	 * <p>The default implementation returns the supplied {@code context} unmodified.
	 */
	default C prepare(C context) throws Exception {
		return context;
	}

	/**
	 * Determine if the execution of the supplied {@code context} should be
	 * <em>skipped</em>.
	 *
	 * <p>The default implementation returns {@link SkipResult#doNotSkip()}.
	 */
	default SkipResult shouldBeSkipped(C context) throws Exception {
		return SkipResult.doNotSkip();
	}

	/**
	 * The result of determining whether the execution of a given {@code context}
	 * should be <em>skipped</em>.
	 *
	 * @see Node#shouldBeSkipped(EngineExecutionContext)
	 */
	class SkipResult {

		private static final SkipResult alwaysExecuteSkipResult = new SkipResult(false, null);

		private final boolean skipped;
		private final String reason;

		/**
		 * Factory for creating <em>skipped</em> results.
		 *
		 * <p>A context that is skipped will be not be executed.
		 *
		 * @param reason the reason why the context should be skipped
		 * @return a skipped {@code SkipResult} with the given reason
		 */
		public static SkipResult skip(String reason) {
			return new SkipResult(true, reason);
		}

		/**
		 * Factory for creating <em>do not skip</em> results.
		 *
		 * <p>A context that is not skipped will be executed as normal.
		 *
		 * @return a <em>do not skip</em> {@code SkipResult}
		 */
		public static SkipResult doNotSkip() {
			return alwaysExecuteSkipResult;
		}

		private SkipResult(boolean skipped, String reason) {
			this.skipped = skipped;
			this.reason = reason;
		}

		/**
		 * Whether execution of the context should be skipped.
		 *
		 * @return {@code true} if the execution should be skipped
		 */
		public boolean isSkipped() {
			return this.skipped;
		}

		/**
		 * Get the reason why execution of the context should be skipped,
		 * if available.
		 */
		public Optional<String> getReason() {
			return Optional.ofNullable(this.reason);
		}
	}

}
