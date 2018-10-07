/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Stream.of;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

class EmptyAndNullStringsProvider implements ArgumentsProvider, AnnotationConsumer<EmptyAndNullStrings> {

	private static final String EMPTY = "";

	private String[] blankValues;

	@Override
	public void accept(EmptyAndNullStrings annotation) {
		this.blankValues = annotation.blankValues();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return Stream.concat(of(null, EMPTY), of(blankValues)).map(Arguments::of);
	}
}
