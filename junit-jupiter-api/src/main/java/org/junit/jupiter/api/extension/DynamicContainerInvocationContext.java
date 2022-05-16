/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import org.apiguardian.api.API;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.Stream;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * {@code DynamicContainerInvocationContext} represents the <em>context</em> of a
 * single invocation of a {@linkplain org.junit.jupiter.api.DynamicContainer
 * dynamic container}.
 *
 * @since 5.8
 * @see org.junit.jupiter.api.DynamicContainer
 */
@API(status = EXPERIMENTAL, since = "5.8")
public interface DynamicContainerInvocationContext {

	/**
	 * Get the {@code Executable} of this dynamic container invocation context.
	 *
	 * @return the executable of the dynamic container invocation, never {@code null}
	 */
	Stream<Executable> getExecutable();

}
