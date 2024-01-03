/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * {@code PreInterruptCallback} defines the API for {@link Extension
 * Extensions} that wish to react on {@link Thread#interrupt()} calls issued by Jupiter
 * before the {@link Thread#interrupt()} is executed.
 *
 * <p>This can be used to e.g. dump stacks for diagnostics, when the {@link org.junit.jupiter.api.Timeout}
 * extension is used.</p>
 *
 * <p>There is also a default implementation available, which will dump the stacks of all {@link Thread Threads}
 * to {@code System.out}. This default implementation need to be enabled with the jupiter property:
 * {@code junit.jupiter.extensions.preinterruptcallback.default.enabled}
 *
 *
 * @since 5.11
 * @see org.junit.jupiter.api.Timeout
 */
@API(status = EXPERIMENTAL, since = "5.11")
public interface PreInterruptCallback extends Extension {

	/**
	 * Callback that is invoked <em>before</em> a {@link Thread} is interrupted with {@link Thread#interrupt()}.
	 *
	 * <p>Caution: There is no guarantee on which {@link Thread} this callback will be executed.</p>
	 *
	 * @param threadToInterrupt  the target {@link Thread}, which will get interrupted.
	 * @param context the current extension context; never {@code null}
	 */
	void beforeThreadInterrupt(Thread threadToInterrupt, ExtensionContext context) throws Exception;
}
