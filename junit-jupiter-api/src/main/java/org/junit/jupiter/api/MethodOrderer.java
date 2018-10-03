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
import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * @since 5.4
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface MethodOrderer {

	void orderMethods(List<? extends MethodDescriptor> methodDescriptors);

	interface MethodDescriptor {

		Class<?> getTestClass();

		Method getTestMethod();

	}

	/**
	 * {@code MethodOrderer} that sorts methods alphanumerically based on their
	 * names using {@link String#compareTo(String)}.
	 *
	 * <p>If two methods have the same name, {@link Method#toString()} will be
	 * used as a fallback for comparing them.
	 */
	class Alphanumeric implements MethodOrderer {

		@Override
		public void orderMethods(List<? extends MethodDescriptor> methodDescriptors) {
			methodDescriptors.sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = (descriptor1, descriptor2) -> {
			Method method1 = descriptor1.getTestMethod();
			Method method2 = descriptor2.getTestMethod();

			int result = method1.getName().compareTo(method2.getName());
			// TODO Fallback to (name + formal argument list) instead of toString().
			return (result != 0) ? result : method1.toString().compareTo(method2.toString());
		};
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
		public void orderMethods(List<? extends MethodDescriptor> methodDescriptors) {
			methodDescriptors.sort(comparator);
		}

		private static final Comparator<MethodDescriptor> comparator = (descriptor1, descriptor2) -> {
			return Integer.compare(getOrder(descriptor1), getOrder(descriptor2));
		};

		private static Integer getOrder(MethodDescriptor descriptor) {
			return AnnotationUtils.findAnnotation(descriptor.getTestMethod(), Order.class)//
					.map(Order::value)//
					.orElse(Integer.MAX_VALUE);
		}
	}

	/**
	 * {@code MethodOrderer} that orders methods randomly.
	 */
	class Random implements MethodOrderer {

		@Override
		public void orderMethods(List<? extends MethodDescriptor> methodDescriptors) {
			Collections.shuffle(methodDescriptors);
		}
	}

}
