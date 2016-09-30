/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestFactoryExtension;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.platform.commons.JUnitException;

class TestFactoryAnnotationExtension implements TestFactoryExtension {

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	@Override
	public Stream<DynamicTest> createForContainer(ContainerExtensionContext context) {
		throw new IllegalStateException("Not yet implemented.");
	}

	@Override
	public Stream<DynamicTest> createForMethod(TestExtensionContext context) {
		try {
			Object testFactoryMethodResult = invokeTestFactoryMethod(context);
			return toDynamicTestStream(context, testFactoryMethodResult);
		}
		catch (ClassCastException ex) {
			throw invalidReturnTypeException(context);
		}
	}

	private Object invokeTestFactoryMethod(TestExtensionContext context) {
		Method method = context.getTestMethod().get();
		Object instance = context.getTestInstance();
		return executableInvoker.invoke(method, instance, context,
			// TODO we are losing all registered extensions here
			// because the registry containing them can not be referenced
			ExtensionRegistry.createRegistryWithDefaultExtensions());
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

	private JUnitException invalidReturnTypeException(TestExtensionContext testExtensionContext) {
		return new JUnitException(
			String.format("@TestFactory method [%s] must return a Stream, Collection, Iterable, or Iterator of %s.",
				testExtensionContext.getTestMethod().get().toGenericString(), DynamicTest.class.getName()));
	}

}
