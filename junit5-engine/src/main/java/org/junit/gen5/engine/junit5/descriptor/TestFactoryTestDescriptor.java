/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.engine.junit5.execution.MethodInvocationContextFactory.methodInvocationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.ThrowableCollector;
import org.junit.gen5.engine.support.hierarchical.SingleTestExecutor;

/**
 * {@link TestDescriptor} for test factory methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestFactoryTestDescriptor extends MethodTestDescriptor {

	public static final String DYNAMIC_TEST_SEGMENT_TYPE = "dynamic-test";

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	public TestFactoryTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, testClass, testMethod);
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	protected void invokeTestMethod(JUnit5EngineExecutionContext context, TestExtensionContext testExtensionContext,
			ThrowableCollector throwableCollector) {

		EngineExecutionListener listener = context.getExecutionListener();

		throwableCollector.execute(() -> {
			MethodInvocationContext methodInvocationContext = methodInvocationContext(
				testExtensionContext.getTestInstance(), testExtensionContext.getTestMethod().get());

			MethodInvoker methodInvoker = new MethodInvoker(testExtensionContext, context.getExtensionRegistry());
			Object testFactoryMethodResult = methodInvoker.invoke(methodInvocationContext);
			Stream<? extends DynamicTest> dynamicTestStream = toDynamicTestStream(testExtensionContext,
				testFactoryMethodResult);

			AtomicInteger index = new AtomicInteger();
			try {
				dynamicTestStream.forEach(
					dynamicTest -> registerAndExecute(dynamicTest, index.incrementAndGet(), listener));
			}
			catch (ClassCastException cce) {
				throw invalidReturnTypeException(testExtensionContext);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Stream<? extends DynamicTest> toDynamicTestStream(TestExtensionContext testExtensionContext,
			Object testFactoryMethodResult) {

		if (testFactoryMethodResult instanceof Stream) {
			return (Stream<? extends DynamicTest>) testFactoryMethodResult;
		}
		// use Collection's stream() implementation even though it implements Iterable
		if (testFactoryMethodResult instanceof Collection) {
			Collection<? extends DynamicTest> dynamicTestCollection = (Collection<? extends DynamicTest>) testFactoryMethodResult;
			return dynamicTestCollection.stream();
		}
		if (testFactoryMethodResult instanceof Iterable) {
			Iterable<? extends DynamicTest> dynamicTestIterable = (Iterable<? extends DynamicTest>) testFactoryMethodResult;
			return StreamSupport.stream(dynamicTestIterable.spliterator(), false);
		}
		if (testFactoryMethodResult instanceof Iterator) {
			Iterator<? extends DynamicTest> dynamicTestIterator = (Iterator<? extends DynamicTest>) testFactoryMethodResult;
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(dynamicTestIterator, Spliterator.ORDERED),
				false);
		}

		throw invalidReturnTypeException(testExtensionContext);
	}

	private void registerAndExecute(DynamicTest dynamicTest, int index, EngineExecutionListener listener) {
		UniqueId uniqueId = getUniqueId().append(DYNAMIC_TEST_SEGMENT_TYPE, "%" + index);
		DynamicTestTestDescriptor dynamicTestTestDescriptor = new DynamicTestTestDescriptor(uniqueId, dynamicTest,
			getSource().get());
		addChild(dynamicTestTestDescriptor);

		listener.dynamicTestRegistered(dynamicTestTestDescriptor);
		listener.executionStarted(dynamicTestTestDescriptor);
		TestExecutionResult result = singleTestExecutor.executeSafely(dynamicTest.getExecutable()::execute);
		listener.executionFinished(dynamicTestTestDescriptor, result);
	}

	private JUnitException invalidReturnTypeException(TestExtensionContext testExtensionContext) {
		return new JUnitException(
			String.format("@TestFactory method [%s] must return a Stream, Collection, Iterable, or Iterator of %s.",
				testExtensionContext.getTestMethod().get().toGenericString(), DynamicTest.class.getName()));
	}

}
