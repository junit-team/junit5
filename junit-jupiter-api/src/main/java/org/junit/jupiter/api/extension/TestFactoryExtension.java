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

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.platform.commons.meta.API;

/**
 * TODO
 *
 * @since 5.0
 */
@API(Experimental)
public interface TestFactoryExtension extends Extension {

	/**
	 * Creates dynamic tests for the container represented by the specified
	 * {@link ContainerExtensionContext}.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return the generated tests; never {@code null}
	 */
	Stream<DynamicTest> createForContainer(ContainerExtensionContext context);

	/**
	 * Creates dynamic tests for the method represented by the specified
	 * {@link TestExtensionContext}.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return the generated tests; never {@code null}
	 */
	Stream<DynamicTest> createForMethod(TestExtensionContext context);

}
