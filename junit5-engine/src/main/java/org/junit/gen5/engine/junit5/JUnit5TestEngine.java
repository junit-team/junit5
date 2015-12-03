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

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.*;

public class JUnit5TestEngine implements TestEngine {
	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return "junit5";
	}

	@Override
	public TestDescriptor discoverTests(TestPlanSpecification specification) {
		Preconditions.notNull(specification, "specification must not be null");
		return null;
	}

	@Override
	public void execute(ExecutionRequest request) {
	}
}