/*
 * Copyright 2015-2017 the original author or authors.
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
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

/**
 * {@link org.junit.platform.engine.TestDescriptor TestDescriptor} for {@link org.junit.jupiter.api.TestFactory @TestFactory}
 * methods.
 *
 * @since 5.0
 */
@API(Internal)
public class TestFactoryTestDescriptor extends MethodTestDescriptor {

	public static final String DYNAMIC_CONTAINER_SEGMENT_TYPE = "dynamic-container";
	public static final String DYNAMIC_TEST_SEGMENT_TYPE = "dynamic-test";

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	public TestFactoryTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, testClass, testMethod);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	protected void invokeTestMethod(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();

		context.getThrowableCollector().execute(() -> {
			Object instance = testExtensionContext.getTestInstance();
			Object testFactoryMethodResult = executableInvoker.invoke(getTestMethod(), instance, testExtensionContext,
				context.getExtensionRegistry());
			TestSource source = getSource().orElseThrow(() -> new JUnitException("Test source must be present"));
			try (Stream<DynamicNode> dynamicNodeStream = toDynamicNodeStream(testFactoryMethodResult)) {
				int index = 1;
				Iterator<DynamicNode> iterator = dynamicNodeStream.iterator();
				while (iterator.hasNext()) {
					DynamicNode dynamicNode = iterator.next();
					JupiterTestDescriptor descriptor = createDynamicDescriptor(this, dynamicNode, index++, source);
					dynamicTestExecutor.execute(descriptor);
				}
			}
			catch (ClassCastException ex) {
				throw invalidReturnTypeException(ex);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private Stream<DynamicNode> toDynamicNodeStream(Object testFactoryMethodResult) {
		try {
			return (Stream<DynamicNode>) CollectionUtils.toStream(testFactoryMethodResult);
		}
		catch (PreconditionViolationException ex) {
			throw invalidReturnTypeException(ex);
		}
	}

	static JupiterTestDescriptor createDynamicDescriptor(JupiterTestDescriptor parent, DynamicNode node, int index,
			TestSource source) {
		JupiterTestDescriptor descriptor;
		if (node instanceof DynamicTest) {
			DynamicTest test = (DynamicTest) node;
			UniqueId uniqueId = parent.getUniqueId().append(DYNAMIC_TEST_SEGMENT_TYPE, "#" + index);
			descriptor = new DynamicTestTestDescriptor(uniqueId, test, source);
		}
		else {
			DynamicContainer container = (DynamicContainer) node;
			UniqueId uniqueId = parent.getUniqueId().append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#" + index);
			descriptor = new DynamicContainerTestDescriptor(uniqueId, container, source);
		}
		parent.addChild(descriptor);
		return descriptor;
	}

	private JUnitException invalidReturnTypeException(Throwable cause) {
		String message = String.format(
			"@TestFactory method [%s] must return a Stream, Collection, Iterable, or Iterator of %s.",
			getTestMethod().toGenericString(), DynamicNode.class.getName());
		return new JUnitException(message, cause);
	}

}
