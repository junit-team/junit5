/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestExecutionResolver {

	public static TestExecutor forDescriptor(TestDescriptor testDescriptor) {
		if (testDescriptor instanceof TestExecutor) {
			return (TestExecutor) testDescriptor;
		}
		else {
			throw new UnsupportedOperationException(
				"Engine expects that classes implementing the TestDescriptor interface do also implement the TestExecutor interface!");
		}
	}
}
