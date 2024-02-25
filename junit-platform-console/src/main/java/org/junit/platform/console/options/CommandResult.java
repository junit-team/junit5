/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class CommandResult<T> {

	/**
	 * Exit code indicating successful execution
	 */
	public static final int SUCCESS = 0;

	/**
	 * Exit code indicating any failure(s)
	 */
	protected static final int FAILURE = -1;

	public static <T> CommandResult<T> success() {
		return create(SUCCESS, null);
	}

	public static <T> CommandResult<T> failure() {
		return create(FAILURE, null);
	}

	public static <T> CommandResult<T> create(int exitCode, T value) {
		return new CommandResult<>(exitCode, value);
	}

	private final int exitCode;
	private final T value;

	private CommandResult(int exitCode, T value) {
		this.exitCode = exitCode;
		this.value = value;
	}

	public int getExitCode() {
		return exitCode;
	}

	public Optional<T> getValue() {
		return Optional.ofNullable(value);
	}
}
