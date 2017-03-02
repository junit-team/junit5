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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;
import org.junit.jupiter.params.support.ObjectArrayArguments;

class EnumArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<EnumSource> {

	private Class<? extends Enum<?>> enumClass;

	@Override
	public void initialize(EnumSource annotation) {
		enumClass = annotation.value();
	}

	@Override
	public Iterator<? extends Arguments> arguments() throws IOException {
		return Arrays.stream(enumClass.getEnumConstants()).map(ObjectArrayArguments::create).iterator();
	}

}
