/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.sources;

import static java.util.Collections.emptyIterator;

import java.util.Iterator;

import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;

class MethodArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<MethodSource> {
	@Override
	public void initialize(MethodSource annotation) {
		// TODO
	}

	@Override
	public Iterator<Arguments> arguments() {
		// TODO
		return emptyIterator();
	}

}
