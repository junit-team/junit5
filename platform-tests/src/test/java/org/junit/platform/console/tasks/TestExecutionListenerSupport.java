/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TestExecutionListenerSupport {

	private final Charset charset = StandardCharsets.UTF_8;
	private final ByteArrayOutputStream stream = new ByteArrayOutputStream(1000);
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	PrintWriter out() {
		return new PrintWriter(new OutputStreamWriter(stream, charset));
	}

	TestIdentifier createTest(String displayName) {
		TestIdentifier identifier = TestIdentifier.from(new ContainerOrTest(displayName, false));
		testPlan.add(identifier);
		return identifier;
	}

	private TestIdentifier createContainer(String displayName) {
		TestIdentifier identifier = TestIdentifier.from(new ContainerOrTest(displayName, true));
		testPlan.add(identifier);
		return identifier;
	}

	List<String> execute(TestExecutionListener listener, Consumer<TestExecutionListener> custom) {
		TestIdentifier engine1 = createContainer("engine alpha");
		TestIdentifier engine2 = createContainer("engine omega");
		TestIdentifier containerA = createContainer("container BEGIN");
		TestIdentifier containerB = createContainer("container END");
		TestIdentifier test00 = createTest("test 00");
		TestIdentifier test01 = createTest("test 01");
		TestIdentifier test10 = createTest("test 10");
		TestIdentifier test11 = createTest("test 11");

		listener.testPlanExecutionStarted(testPlan);

		listener.executionStarted(engine1);
		listener.executionStarted(containerA);
		listener.executionStarted(test00);
		listener.executionFinished(test00, TestExecutionResult.successful());
		listener.executionStarted(test01);
		listener.executionFinished(test01, TestExecutionResult.successful());
		listener.executionFinished(containerA, TestExecutionResult.successful());
		listener.executionFinished(engine1, TestExecutionResult.successful());

		if (custom != null) {
			TestIdentifier customEngine = createContainer("custom engine");
			TestIdentifier customContainer = createContainer("custom container");
			listener.executionStarted(customEngine);
			listener.executionStarted(customContainer);
			custom.accept(listener);
			listener.executionFinished(customContainer, TestExecutionResult.successful());
			listener.executionFinished(customEngine, TestExecutionResult.successful());
		}

		listener.executionStarted(engine2);
		listener.executionStarted(containerB);
		listener.executionStarted(test10);
		listener.executionFinished(test10, TestExecutionResult.successful());
		listener.executionStarted(test11);
		listener.executionFinished(test11, TestExecutionResult.successful());
		listener.executionFinished(containerB, TestExecutionResult.successful());
		listener.executionFinished(engine2, TestExecutionResult.successful());

		listener.testPlanExecutionFinished(testPlan);

		try {
			return Arrays.asList(stream.toString(charset.name()).split("\\R"));
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(charset.name() + " is an unsupported encoding?!", e);
		}
	}

	static class ContainerOrTest extends AbstractTestDescriptor {
		private final boolean container;

		ContainerOrTest(String displayName, boolean container) {
			super(UniqueId.root(container ? "container" : "test", displayName), displayName);
			this.container = container;
		}

		@Override
		public boolean isTest() {
			return !container;
		}

		@Override
		public boolean isContainer() {
			return container;
		}
	}
}
