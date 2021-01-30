/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestDescriptor;

class AbstractOrderingVisitor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOrderingVisitor.class);

	@SuppressWarnings({ "unchecked" })
	protected <T extends TestDescriptor> void doWithMatchingDescriptor(Class<T> descriptorSubType,
			TestDescriptor testDescriptor, Consumer<T> action, Function<T, String> errorMessageBuilder) {

		if (descriptorSubType.isAssignableFrom(testDescriptor.getClass())) {
			T specificTestDescriptor = (T) testDescriptor;
			try {
				action.accept(specificTestDescriptor);
			}
			catch (Throwable t) {
				UnrecoverableExceptions.rethrowIfUnrecoverable(t);
				logger.error(t, () -> errorMessageBuilder.apply(specificTestDescriptor));
			}
		}
	}

	protected <T extends TestDescriptor, D extends AbstractAnnotatedElementDescriptor<?>> void orderChildrenTestDescriptors(
			TestDescriptor parentTestDescriptor, Class<T> matchingChildrenType, Function<T, D> descriptorWrapperBuilder,
			Consumer<List<D>> orderingAction, IntFunction<String> descriptorsAddedLogger,
			IntFunction<String> descriptorsRemovedLogger) {

		Set<? extends TestDescriptor> children = parentTestDescriptor.getChildren();

		List<TestDescriptor> nonMatchingTestDescriptors = children.stream()//
				.filter(childTestDescriptor -> !(matchingChildrenType.isAssignableFrom(childTestDescriptor.getClass())))//
				.collect(Collectors.toList());

		List<D> matchingDescriptorWrappers = children.stream()//
				.filter(matchingChildrenType::isInstance)//
				.map(matchingChildrenType::cast)//
				.map(descriptorWrapperBuilder)//
				.collect(toCollection(ArrayList::new));

		// Make a local copy for later validation
		Set<D> originalDescriptors = new LinkedHashSet<>(matchingDescriptorWrappers);

		orderingAction.accept(matchingDescriptorWrappers);

		int difference = matchingDescriptorWrappers.size() - originalDescriptors.size();

		if (difference > 0) {
			logger.warn(() -> descriptorsAddedLogger.apply(difference));
		}
		else if (difference < 0) {
			logger.warn(() -> descriptorsRemovedLogger.apply(difference));
		}

		Set<TestDescriptor> sortedTestDescriptors = matchingDescriptorWrappers.stream()//
				.filter(originalDescriptors::contains)//
				.map(AbstractAnnotatedElementDescriptor::getTestDescriptor)//
				.collect(toCollection(LinkedHashSet::new));

		// Currently no way to removeAll or addAll children at once.
		Stream.concat(sortedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
				.forEach(parentTestDescriptor::removeChild);
		Stream.concat(sortedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
				.forEach(parentTestDescriptor::addChild);
	}

}
