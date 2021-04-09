/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code Named} is used to wrap an object and give it a name.
 *
 * @param <T> the type of the payload
 */
@API(status = STABLE, since = "5.8")
public interface Named<T> {

	/**
	 * Factory method for creating an instance of {@code Named} based on a {@code name} and a {@code payload}
	 *
	 * @param name the name to be used for the wrapped object
	 * @param payload the object to be wrapped
	 * @param <T> the type of the payload
	 * @return an instance of {@code Named}; never {@code null}
	 * @see #named(String, java.lang.Object)
	 */
	static <T> Named<T> of(String name, T payload) {
		return new Named<T>() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public T getPayload() {
				return payload;
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	/**
	 * Factory method for creating an instance of {@code Named} based on a {@code name} and a {@code payload}
	 *
	 * <p>This method is an <em>alias</em> for {@link Named#of} and is
	 * intended to be used when statically imported &mdash; for example, via:
	 * {@code import static org.junit.jupiter.api.Named.named;}
	 *
	 * @param name the name to be used for the wrapped object
	 * @param payload the object to be wrapped
	 * @param <T> the type of the payload
	 * @return an instance of {@code Named}; never {@code null}
	 */
	static <T> Named<T> named(String name, T payload) {
		return of(name, payload);
	}

	String getName();

	T getPayload();

}
