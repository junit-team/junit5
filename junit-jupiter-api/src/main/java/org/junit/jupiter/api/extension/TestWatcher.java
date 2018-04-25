/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.util.Optional;

public interface TestWatcher extends Extension {

	default void testSuccessful(String identifier, ExtensionContext context) {
	}

	default void testAborted(String identifier, Throwable cause, ExtensionContext context) {
	}

	default void testFailed(String identifier, Throwable cause, ExtensionContext context) {
	}

	default void testSkipped(String identifier, Optional<String> Reason, ExtensionContext context) {
	}
}
