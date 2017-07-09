/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.cucumber.tagexpressions;

public class TagExpressionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TagExpressionException(String message) {
		super(message);
	}
}
