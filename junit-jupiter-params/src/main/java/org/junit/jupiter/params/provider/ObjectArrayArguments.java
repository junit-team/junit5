/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

/**
 * @since 5.0
 */
public class ObjectArrayArguments implements Arguments {

	private final Object[] arguments;

	public static ObjectArrayArguments create(Object... arguments) {
		return new ObjectArrayArguments(arguments);
	}

	private ObjectArrayArguments(Object... arguments) {
		this.arguments = arguments;
	}

	@Override
	public Object[] get() {
		return arguments;
	}
}
