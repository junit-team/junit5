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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestDescriptor;

abstract class AbstractOrderingVisitor<PARENT extends TestDescriptor, CHILD extends TestDescriptor, ELEMENT extends AbstractAnnotatedElementDescriptor<?>> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOrderingVisitor.class);

	@SuppressWarnings("unchecked")
	protected void doWithMatchingDescriptor(Class<PARENT> parentTestDescriptorType, TestDescriptor testDescriptor,
			Consumer<PARENT> action, Function<PARENT, String> errorMessageBuilder) {

		if (parentTestDescriptorType.isInstance(testDescriptor)) {
			PARENT parentTestDescriptor = (PARENT) testDescriptor;
			try {
				action.accept(parentTestDescriptor);
			}
			catch (Throwable t) {
				UnrecoverableExceptions.rethrowIfUnrecoverable(t);
				logger.error(t, () -> errorMessageBuilder.apply(parentTestDescriptor));
			}
		}
	}

	protected void orderChildrenTestDescriptors(TestDescriptor parentTestDescriptor, Class<CHILD> matchingChildrenType,
			Function<CHILD, ELEMENT> elementDescriptorFactory, ElementDescriptorOrderer elementDescriptorOrderer) {

		Set<? extends TestDescriptor> children = parentTestDescriptor.getChildren();

		List<ELEMENT> matchingElementDescriptors = children.stream()//
				.filter(matchingChildrenType::isInstance)//
				.map(matchingChildrenType::cast)//
				.map(elementDescriptorFactory)//
				.collect(toCollection(ArrayList::new));

		// If there are no children to order, abort early.
		if (matchingElementDescriptors.isEmpty()) {
			return;
		}

		if (elementDescriptorOrderer.canOrderDescriptors()) {
			List<TestDescriptor> nonMatchingTestDescriptors = children.stream()//
					.filter(childTestDescriptor -> !matchingChildrenType.isInstance(childTestDescriptor))//
					.collect(Collectors.toList());

			// Make a local copy for later validation
			Set<ELEMENT> originalDescriptors = new LinkedHashSet<>(matchingElementDescriptors);

			elementDescriptorOrderer.orderDescriptors(matchingElementDescriptors);

			int difference = matchingElementDescriptors.size() - originalDescriptors.size();
			if (difference > 0) {
				elementDescriptorOrderer.logDescriptorsAddedWarning(difference);
			}
			else if (difference < 0) {
				elementDescriptorOrderer.logDescriptorsRemovedWarning(difference);
			}

			Set<TestDescriptor> sortedTestDescriptors = matchingElementDescriptors.stream()//
					.filter(originalDescriptors::contains)//
					.map(AbstractAnnotatedElementDescriptor::getTestDescriptor)//
					.collect(toCollection(LinkedHashSet::new));

			// There is currently no way to removeAll or addAll children at once, so we
			// first remove them all and then add them all back.
			Stream.concat(sortedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
					.forEach(parentTestDescriptor::removeChild);

			// If we are sorting children of type ClassBasedTestDescriptor, that means we
			// are sorting @Nested test classes. In that case, the nonMatchingTestDescriptors
			// are local test methods which must be executed before tests in @Nested test
			// classes. So we add the test methods before adding the @Nested test classes.
			if (matchingChildrenType == ClassBasedTestDescriptor.class) {
				Stream.concat(nonMatchingTestDescriptors.stream(), sortedTestDescriptors.stream())//
						.forEach(parentTestDescriptor::addChild);
			}
			// Otherwise, we add the sorted descriptors before the non-matching descriptors,
			// which is the case when we are sorting test methods. In other words, local
			// test methods always get added before @Nested test classes.
			else {
				Stream.concat(sortedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
						.forEach(parentTestDescriptor::addChild);
			}
		}

		// Recurse through the children in order to support ordering for @Nested test classes.
		matchingElementDescriptors.forEach(annotatedElementDescriptor -> {
			TestDescriptor newParentTestDescriptor = annotatedElementDescriptor.getTestDescriptor();
			ElementDescriptorOrderer newElementDescriptorOrderer = getElementDescriptorOrderer(elementDescriptorOrderer,
				annotatedElementDescriptor);

			orderChildrenTestDescriptors(newParentTestDescriptor, matchingChildrenType, elementDescriptorFactory,
				newElementDescriptorOrderer);
		});
	}

	/**
	 * Get the {@link ElementDescriptorOrderer} for the supplied {@link AbstractAnnotatedElementDescriptor}.
	 *
	 * <p>The default implementation returns the supplied {@code ElementDescriptorOrderer}.
	 *
	 * @return a new {@code ElementDescriptorOrderer} or the one supplied as an argument
	 */
	protected ElementDescriptorOrderer getElementDescriptorOrderer(
			ElementDescriptorOrderer inheritedElementDescriptorOrderer,
			AbstractAnnotatedElementDescriptor<?> annotatedElementDescriptor) {

		return inheritedElementDescriptorOrderer;
	}

	protected class ElementDescriptorOrderer {

		private final Consumer<List<ELEMENT>> orderingAction;

		private final MessageGenerator descriptorsAddedMessageGenerator;

		private final MessageGenerator descriptorsRemovedMessageGenerator;

		ElementDescriptorOrderer(Consumer<List<ELEMENT>> orderingAction,
				MessageGenerator descriptorsAddedMessageGenerator,
				MessageGenerator descriptorsRemovedMessageGenerator) {

			this.orderingAction = orderingAction;
			this.descriptorsAddedMessageGenerator = descriptorsAddedMessageGenerator;
			this.descriptorsRemovedMessageGenerator = descriptorsRemovedMessageGenerator;
		}

		private boolean canOrderDescriptors() {
			return this.orderingAction != null;
		}

		private void orderDescriptors(List<ELEMENT> elements) {
			this.orderingAction.accept(elements);
		}

		private void logDescriptorsAddedWarning(int number) {
			logger.warn(() -> this.descriptorsAddedMessageGenerator.generateMessage(number));
		}

		private void logDescriptorsRemovedWarning(int number) {
			logger.warn(() -> this.descriptorsRemovedMessageGenerator.generateMessage(Math.abs(number)));
		}

	}

	@FunctionalInterface
	protected interface MessageGenerator {

		String generateMessage(int number);
	}

}
