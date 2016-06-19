/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.Collections;
import java.util.List;

import org.junit.platform.commons.util.ExceptionUtils;

/**
 * Internal utilities for working with <em>blacklisted</em> exceptions.
 *
 * <p><em>Blacklisted</em> exceptions are those that should always terminate
 * test plan execution immediately.
 *
 * <h4>Currently Blacklisted Exceptions</h4>
 * <ul>
 * <li>{@link OutOfMemoryError}</li>
 * </ul>
 *
 * @since 5.0
 */
final class BlacklistedExceptions {

	private static final List<Class<? extends Throwable>> blacklist = Collections.singletonList(OutOfMemoryError.class);

	private BlacklistedExceptions() {
		/* no-op */
	}

	/**
	 * Rethrow the supplied {@link Throwable exception} if it is
	 * <em>blacklisted</em>.
	 *
	 * <p>If the supplied {@code exception} is not <em>blacklisted</em>,
	 * this method does nothing.
	 */
	static void rethrowIfBlacklisted(Throwable exception) {
		if (blacklist.stream().anyMatch(exceptionType -> exceptionType.isInstance(exception))) {
			ExceptionUtils.throwAsUncheckedException(exception);
		}
	}

}
