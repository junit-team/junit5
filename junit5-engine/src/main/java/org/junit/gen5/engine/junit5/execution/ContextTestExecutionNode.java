/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.reflect.Method;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ContextTestDescriptor;
import org.opentestalliance.TestSkippedException;

/**
 * @since 5.0
 */
// TODO Implement execution of inner contexts.
@SuppressWarnings("unused")
class ContextTestExecutionNode extends ClassTestExecutionNode {

	ContextTestExecutionNode(ClassTestDescriptor testDescriptor) {
		super(testDescriptor);
	}

	@Override
	public void execute(EngineExecutionContext context) {
		TestSkippedException testSkippedException = new TestSkippedException("Not yet able to execute test contexts.");
		context.getTestExecutionListener().testSkipped(getTestDescriptor(), testSkippedException);
	}

}
