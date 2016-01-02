/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Optional;

public interface Node<C extends EngineExecutionContext> {

	default C prepare(C context) throws Exception {
		return context;
	}

	default SkipResult shouldBeSkipped(C context) throws Exception {
		return SkipResult.dontSkip();
	}

	class SkipResult {
		private final boolean skipped;
		private final String reason;

		public static SkipResult skip(String reason) {
			return new SkipResult(true, reason);
		}

		public static SkipResult dontSkip() {
			return new SkipResult(false, null);
		}

		private SkipResult(boolean skipped, String reason) {
			this.skipped = skipped;
			this.reason = reason;
		}

		public boolean isSkipped() {
			return skipped;
		}

		public Optional<String> getReason() {
			return Optional.ofNullable(reason);
		}
	}
}
