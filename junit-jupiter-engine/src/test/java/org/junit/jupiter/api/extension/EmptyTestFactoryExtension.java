/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;

public class EmptyTestFactoryExtension implements TestFactoryExtension {

	@Override
	public Stream<DynamicTest> createForContainer(ContainerExtensionContext context) {
		return Stream.empty();
	}

	@Override
	public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
		return Stream.empty();
	}
}
