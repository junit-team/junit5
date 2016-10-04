/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.TestFactoryExtensionScanner;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.SingleTestExecutor;

/**
 * {@link TestDescriptor} for {@link TestFactoryExtension} methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestFactoryExtensionMethodTestDescriptor extends MethodTestDescriptor {

	public static final String DYNAMIC_TEST_SEGMENT_TYPE = "dynamic-test";

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	public TestFactoryExtensionMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, testClass, testMethod);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public boolean hasTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	protected void invokeTestMethod(JupiterEngineExecutionContext context) {
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		EngineExecutionListener listener = context.getExecutionListener();

		context.getThrowableCollector().execute(() -> {
			AtomicInteger index = new AtomicInteger();
			Method extendedMethod = testExtensionContext.getTestMethod().get();
			// @formatter:off
			TestFactoryExtensionScanner.streamTestFactoryExtensions(extendedMethod)
					.flatMap(extension -> createDynamicTestsForExtension(extension, testExtensionContext))
					.forEach(dynamicTest -> registerAndExecute(dynamicTest, index.incrementAndGet(), listener));
			// @formatter:on
		});
	}

	private Stream<DynamicTest> createDynamicTestsForExtension(Class<TestFactoryExtension> extensionClass,
			TestExtensionContext testExtensionContext) {
		TestFactoryExtension extension = ReflectionUtils.newInstance(extensionClass);
		return extension.createForMethod(testExtensionContext);
	}

	private void registerAndExecute(DynamicTest dynamicTest, int index, EngineExecutionListener listener) {
		UniqueId uniqueId = getUniqueId().append(DYNAMIC_TEST_SEGMENT_TYPE, "#" + index);
		// TODO it is unclear why the source should be present
		TestDescriptor descriptor = new DynamicTestTestDescriptor(uniqueId, dynamicTest, getSource().get());

		addChild(descriptor);
		listener.dynamicTestRegistered(descriptor);

		listener.executionStarted(descriptor);
		TestExecutionResult result = singleTestExecutor.executeSafely(dynamicTest.getExecutable()::execute);
		listener.executionFinished(descriptor, result);
	}

}
