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

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.SingleTestExecutor;

/**
 * {@link TestDescriptor} for {@link org.junit.jupiter.api.TestFactory @TestFactory}
 * methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestFactoryTestDescriptor extends MethodTestDescriptor {

	public static final String DYNAMIC_TEST_SEGMENT_TYPE = "dynamic-test";

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();
	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	public TestFactoryTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
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
			Method method = testExtensionContext.getTestMethod().get();
			Object instance = testExtensionContext.getTestInstance();
			Object testFactoryMethodResult = executableInvoker.invoke(method, instance, testExtensionContext,
				context.getExtensionRegistry());

			try {
				AtomicInteger index = new AtomicInteger();
				toDynamicTestStream(testExtensionContext, testFactoryMethodResult).forEach(
					dynamicTest -> registerAndExecute(dynamicTest, index.incrementAndGet(), listener));
			}
			catch (ClassCastException ex) {
				throw invalidReturnTypeException(testExtensionContext);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Stream<DynamicTest> toDynamicTestStream(TestExtensionContext testExtensionContext,
			Object testFactoryMethodResult) {

		if (testFactoryMethodResult instanceof Stream) {
			return (Stream<DynamicTest>) testFactoryMethodResult;
		}
		if (testFactoryMethodResult instanceof Collection) {
			// Use Collection's stream() implementation even though Collection implements Iterable
			Collection<DynamicTest> collection = (Collection<DynamicTest>) testFactoryMethodResult;
			return collection.stream();
		}
		if (testFactoryMethodResult instanceof Iterable) {
			Iterable<DynamicTest> iterable = (Iterable<DynamicTest>) testFactoryMethodResult;
			return stream(iterable.spliterator(), false);
		}
		if (testFactoryMethodResult instanceof Iterator) {
			Iterator<DynamicTest> iterator = (Iterator<DynamicTest>) testFactoryMethodResult;
			return stream(spliteratorUnknownSize(iterator, ORDERED), false);
		}

		throw invalidReturnTypeException(testExtensionContext);
	}

	private void registerAndExecute(DynamicTest dynamicTest, int index, EngineExecutionListener listener) {
		UniqueId uniqueId = getUniqueId().append(DYNAMIC_TEST_SEGMENT_TYPE, "#" + index);
		TestDescriptor descriptor = new DynamicTestTestDescriptor(uniqueId, dynamicTest, getSource().get());

		addChild(descriptor);
		listener.dynamicTestRegistered(descriptor);

		listener.executionStarted(descriptor);
		TestExecutionResult result = singleTestExecutor.executeSafely(dynamicTest.getExecutable()::execute);
		listener.executionFinished(descriptor, result);
	}

	private JUnitException invalidReturnTypeException(TestExtensionContext testExtensionContext) {
		return new JUnitException(
			String.format("@TestFactory method [%s] must return a Stream, Collection, Iterable, or Iterator of %s.",
				testExtensionContext.getTestMethod().get().toGenericString(), DynamicTest.class.getName()));
	}

}
