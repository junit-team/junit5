/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEngineExecution;
import org.junit.jupiter.api.BeforeEngineExecution;
import org.junit.jupiter.api.extension.AfterEngineExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEngineExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

class EngineExecutionCallbackResolver implements ElementResolver {

	private final JupiterEngineDescriptor engineDescriptor;

	EngineExecutionCallbackResolver(JupiterEngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		return Optional.empty();
	}

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		// @formatter:off
		findRepeatableAnnotations(element, ExtendWith.class)
				.stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.map(ReflectionUtils::newInstance)
				.filter(x -> x instanceof BeforeEngineExecutionCallback)
				.filter(x -> x instanceof AfterEngineExecutionCallback)
				.forEach(engineDescriptor::registerExtension);
				//.forEach(System.out::println);
		// @formatter:on

		if ((element instanceof Class)) {
			//@formatter:off
			AnnotationUtils.findAnnotatedMethods((Class<?>) element, BeforeEngineExecution.class, TOP_DOWN)
                    .stream()
                    .map(EngineExecutionCallbackResolver::synthesizeBeforeSuiteCallback)
                    .forEach(engineDescriptor::registerExtension);
			AnnotationUtils.findAnnotatedMethods((Class<?>) element, AfterEngineExecution.class, BOTTOM_UP)
                    .stream()
                    .map(EngineExecutionCallbackResolver::synthesizeAfterSuiteCallback)
                    .forEach(engineDescriptor::registerExtension);
            //@formatter:on
		}
		return Collections.emptySet();
	}

	private static BeforeEngineExecutionCallback synthesizeBeforeSuiteCallback(Method method) {
		if (method.getParameterCount() == 0) {
			return context -> ReflectionUtils.invokeMethod(method, null);
		}
		return context -> ReflectionUtils.invokeMethod(method, null, context);
	}

	private static AfterEngineExecutionCallback synthesizeAfterSuiteCallback(Method method) {
		return new DefaultAfterEngineExecutionCallback(method);
	}

	private static class DefaultAfterEngineExecutionCallback implements AfterEngineExecutionCallback {

		private final Method method;
		private Object testInstance;

		DefaultAfterEngineExecutionCallback(Method method) {
			this.method = method;
			this.testInstance = null;
		}

		@Override
		public Optional<Class<?>> getTestInstanceClass() {
			return Optional.of(method.getDeclaringClass());
		}

		@Override
		public void setTestInstance(Object testInstance) {
			this.testInstance = testInstance;
		}

		@Override
		public void afterEngineExecution(ExtensionContext context) {
			if (method.getParameterCount() == 0) {
				ReflectionUtils.invokeMethod(method, testInstance);
				return;
			}
			ReflectionUtils.invokeMethod(method, testInstance, context);
		}
	}

}
