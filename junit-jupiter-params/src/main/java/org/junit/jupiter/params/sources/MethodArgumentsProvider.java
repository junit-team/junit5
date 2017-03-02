/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.sources;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.AnnotationInitialized;
import org.junit.jupiter.params.Arguments;
import org.junit.jupiter.params.ArgumentsProvider;
import org.junit.jupiter.params.support.ObjectArrayArguments;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

class MethodArgumentsProvider implements ArgumentsProvider, AnnotationInitialized<MethodSource> {

	private String methodName;
	private Object source;

	@Override
	public void initialize(MethodSource annotation) {
		methodName = annotation.value();
	}

	@Override
	public Iterator<Arguments> arguments(ContainerExtensionContext context) {
		Class<?> testClass = context.getTestClass() //
				.orElseThrow(() -> new JUnitException("Cannot invoke method without test class: " + methodName));
		Method method = ReflectionUtils.findMethod(testClass, methodName) //
				.orElseThrow(() -> new JUnitException("Could not find method: " + methodName));
		source = ReflectionUtils.invokeMethod(method, null);
		if (source instanceof Iterator) {
			return decorate((Iterator<?>) source);
		}
		if (source instanceof Iterable) {
			return decorate(((Iterable<?>) source).iterator());
		}
		if (source instanceof Stream) {
			return decorate(((Stream<?>) source).iterator());
		}
		// TODO #14 better error message
		throw new JUnitException("Illegal return type: " + source.getClass().getName());
	}

	@Override
	public void close() {
		if (source instanceof AutoCloseable) {
			try {
				((AutoCloseable) source).close();
			}
			catch (Exception e) {
				ExceptionUtils.throwAsUncheckedException(e);
			}
		}
	}

	private Iterator<Arguments> decorate(Iterator<?> iterator) {
		return new Iterator<Arguments>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Arguments next() {
				Object nextItem = iterator.next();
				if (nextItem instanceof Arguments) {
					return (Arguments) nextItem;
				}
				// TODO #14 make sure Object[] works, too
				return ObjectArrayArguments.create(nextItem);
			}
		};
	}

}
