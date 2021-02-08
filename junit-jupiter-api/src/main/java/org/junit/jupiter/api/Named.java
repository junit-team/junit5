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

	String getName();

	T getPayload();

}
