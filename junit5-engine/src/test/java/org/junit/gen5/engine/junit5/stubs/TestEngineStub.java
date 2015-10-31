/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.stubs;

import java.util.Collection;

import org.junit.gen5.engine.*;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestEngineStub implements TestEngine {

	public static final String TEST_ENGINE_DUMMY_ID = "TestEngineDummyID";

	@Override
	public String getId() {
		return TEST_ENGINE_DUMMY_ID;
	}

	@Override
	public Collection<TestDescriptor> discoverTests(TestPlanSpecification specification,
			TestDescriptor engineDescriptor) {
		return null;
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return false;
	}

	@Override
	public void execute(EngineExecutionContext context) {
	}
}