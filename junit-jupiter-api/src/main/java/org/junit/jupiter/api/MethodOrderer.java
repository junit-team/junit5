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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

	interface MethodDescriptor {

		/**
		 * Get the method for this descriptor.
		 *
		 * @return the method; never {@code null}
		 */
		Method getMethod();

		/**
		 * Determine if an annotation of {@code annotationType} is either
		 * <em>present</em> or <em>meta-present</em> on the {@link Method} for
		 * this descriptor.
		 *
		 * @param annotationType the annotation type to search for; never {@code null}
		 * @return {@code true} if the annotation is present or meta-present
		 * @see #findAnnotation(Class)
		 * @see #findRepeatableAnnotations(Class)
		 */
		boolean isAnnotated(Class<? extends Annotation> annotationType);

		/**
		 * Find the first annotation of {@code annotationType} that is either
		 * <em>present</em> or <em>meta-present</em> on the {@link Method} for
		 * this descriptor.
		 *
		 * @param <A> the annotation type
		 * @param annotationType the annotation type to search for; never {@code null}
		 * @return an {@code Optional} containing the annotation; never {@code null} but
		 * potentially empty
		 * @see #isAnnotated(Class)
		 * @see #findRepeatableAnnotations(Class)
		 */
		<A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType);

		/**
		 * Find all <em>repeatable</em> {@linkplain Annotation annotations} of
		 * {@code annotationType} that are either <em>present</em> or
		 * <em>meta-present</em> on the {@link Method} for this descriptor.
		 *
		 * @param <A> the annotation type
		 * @param annotationType the repeatable annotation type to search for; never
		 * {@code null}
		 * @return the list of all such annotations found; neither {@code null} nor
		 * mutable, but potentially empty
		 * @since 5.1.1
		 * @see #isAnnotated(Class)
		 * @see #findAnnotation(Class)
		 * @see java.lang.annotation.Repeatable
		 */
		<A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType);

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
