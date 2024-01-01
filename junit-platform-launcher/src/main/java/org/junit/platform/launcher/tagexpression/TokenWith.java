/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

/**
 * @since 1.1
 */
class TokenWith<T> {

	final Token token;
	final T element;

	TokenWith(Token token, T element) {
		this.token = token;
		this.element = element;
	}

}
