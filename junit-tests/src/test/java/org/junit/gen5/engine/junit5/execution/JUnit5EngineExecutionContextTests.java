/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.api.Assertions.assertSame;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineExecutionListener;
import org.mockito.Mockito;

class JUnit5EngineExecutionContextTests {

	private JUnit5EngineExecutionContext originalContext;
	private EngineExecutionListener engineExecutionListener;

	@BeforeEach
	void initOriginalContext() {
		engineExecutionListener = Mockito.mock(EngineExecutionListener.class);
		originalContext = new JUnit5EngineExecutionContext(engineExecutionListener);
	}

	@Test
	void executionListenerIsHandedOnWhenContextIsExtended() {
		assertSame(engineExecutionListener, originalContext.getExecutionListener());
		JUnit5EngineExecutionContext newContext = originalContext.extend().build();
		assertSame(engineExecutionListener, newContext.getExecutionListener());
	}

}
