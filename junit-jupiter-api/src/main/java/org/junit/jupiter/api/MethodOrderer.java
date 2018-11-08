/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;

import org.apiguardian.api.API;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.util.ClassUtils;

/**
 * @since 5.4
 * @see MethodOrdererContext
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface MethodOrderer {

	void orderMethods(MethodOrdererContext context);

	/**
	 * Get the <em>default</em> {@link ExecutionMode} for the annotated class.
	 *
	 * <p>Defaults to {@link ExecutionMode#SAME_THREAD SAME_THREAD}, since
	 * ordered methods are typically sorted in a fashion that would conflict
	 * with concurrent execution.
	 *
	 * <p>Can be overridden via an explicit
	 * {@link org.junit.jupiter.api.parallel.Execution @Execution} declaration.
	 *
	 * @return the default {@code ExecutionMode}; never {@code null}
	 */
	default ExecutionMode getDefaultExecutionMode() {
		return ExecutionMode.SAME_THREAD;
	}

	/**
	 * {@code MethodOrderer} that sorts methods alphanumerically based on their
	 * names using {@link String#compareTo(String)}.
	 *
	 * <p>If two methods have the same name, a {@code String} representation of
	 * their formal parameter lists will be used as a fallback for comparing the
	 * methods.
	 */
	class Alphanumeric implements MethodOrderer {

		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = (descriptor1, descriptor2) -> {
			Method method1 = descriptor1.getMethod();
			Method method2 = descriptor2.getMethod();

			int result = method1.getName().compareTo(method2.getName());
			if (result != 0) {
				return result;
			}

			// else
			return parameterList(method1).compareTo(parameterList(method2));
		};

		private static String parameterList(Method method) {
			return ClassUtils.nullSafeToString(method.getParameterTypes());
		}
	}

	/**
	 * {@code MethodOrderer} that sorts methods based on the {@link Order @Order}
	 * annotation.
	 *
	 * <p>Any methods not annotated with {@code @Order} will appear at the end of
	 * the sorted list.
	 */
	class OrderAnnotation implements MethodOrderer {

		@Override
		public void orderMethods(MethodOrdererContext context) {
			context.getMethodDescriptors().sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = //
			(descriptor1, descriptor2) -> Integer.compare(getOrder(descriptor1), getOrder(descriptor2));

		private static Integer getOrder(MethodDescriptor descriptor) {
			return descriptor.findAnnotation(Order.class).map(Order::value).orElse(Integer.MAX_VALUE);
		}
	}

	/**
	 * {@code MethodOrderer} that orders methods randomly and allows for concurrent
	 * execution by default.
	 *
	 * @see #getDefaultExecutionMode()
	 */
	class Random implements MethodOrderer {

		@Override
		public void orderMethods(MethodOrdererContext context) {
			long seed = context.getConfigurationParameter("junit.jupiter.execution.order.random.seed")//
					.map(Long::valueOf)//
					.orElse(System.nanoTime());

			Collections.shuffle(context.getMethodDescriptors(), new java.util.Random(seed));
		}

		/**
		 * Returns {@link ExecutionMode#CONCURRENT CONCURRENT} to allow concurrent
		 * execution of randomly ordered methods by default.
		 */
		@Override
		public ExecutionMode getDefaultExecutionMode() {
			return ExecutionMode.CONCURRENT;
		}
	}

}
