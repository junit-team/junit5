/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.function.Executable;

/**
 * {@code NamedExecutable} is a container that associates a name, a payload and an
 * {@link Executable}
 *
 * @param <T> the type of the payload
 *
 * @since 5.11
 */
@API(status = EXPERIMENTAL, since = "5.11")
public interface NamedExecutable<T extends NamedExecutable<T>> extends Named<T>, Executable {
	@Override
	default String getName() {
		return toString();
	}
}
