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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

class EmptyAndNullListsProvider implements ArgumentsProvider, AnnotationConsumer<EmptyAndNullLists> {

	@Override
	public void accept(EmptyAndNullLists t) {
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		List<?> nullList = null;
		List<?> emptyList = new ArrayList<>();

		return Stream.of(emptyList, nullList).map(Arguments::of);
	}

}
