/*
 * Copyright 2015-2020 the original author or authors.
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

@API(status = EXPERIMENTAL, since = "5.8")
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
