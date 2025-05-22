/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

/**
 * Abstract base class for {@linkplain TestDescriptor.Visitor visitors} that
 * order children nodes.
 *
 * @since 5.8
 */
abstract class AbstractOrderingVisitor implements TestDescriptor.Visitor {

	private final DiscoveryIssueReporter issueReporter;

	AbstractOrderingVisitor(DiscoveryIssueReporter issueReporter) {
		this.issueReporter = issueReporter;
	}

	/**
	 * @param <PARENT> the parent container type to search in for matching children
	 */
	@SuppressWarnings("unchecked")
	protected <PARENT extends TestDescriptor> void doWithMatchingDescriptor(Class<PARENT> parentTestDescriptorType,
			TestDescriptor testDescriptor, Consumer<PARENT> action, Function<PARENT, String> errorMessageBuilder) {

		if (parentTestDescriptorType.isInstance(testDescriptor)) {
			PARENT parentTestDescriptor = (PARENT) testDescriptor;
			try {
				action.accept(parentTestDescriptor);
			}
			catch (Throwable t) {
				UnrecoverableExceptions.rethrowIfUnrecoverable(t);
				String message = errorMessageBuilder.apply(parentTestDescriptor);
				this.issueReporter.reportIssue(DiscoveryIssue.builder(Severity.ERROR, message) //
						.source(parentTestDescriptor.getSource()) //
						.cause(t));
			}
		}
	}

	/**
	 * @param <CHILD> the type of children (containers or tests) to order
	 */
	protected <CHILD extends TestDescriptor, WRAPPER extends AbstractAnnotatedDescriptorWrapper<?>> void orderChildrenTestDescriptors(
			TestDescriptor parentTestDescriptor, Class<CHILD> matchingChildrenType,
			Optional<Consumer<CHILD>> validationAction, Function<CHILD, WRAPPER> descriptorWrapperFactory,
			DescriptorWrapperOrderer<?, WRAPPER> descriptorWrapperOrderer) {

		Stream<CHILD> matchingChildren = parentTestDescriptor.getChildren()//
				.stream()//
				.filter(matchingChildrenType::isInstance)//
				.map(matchingChildrenType::cast);

		if (!descriptorWrapperOrderer.canOrderWrappers()) {
			validationAction.ifPresent(matchingChildren::forEach);
			return;
		}

		if (validationAction.isPresent()) {
			matchingChildren = matchingChildren.peek(validationAction.get());
		}

		List<WRAPPER> matchingDescriptorWrappers = matchingChildren//
				.map(descriptorWrapperFactory)//
				.collect(toCollection(ArrayList::new));

		// If there are no children to order, abort early.
		if (matchingDescriptorWrappers.isEmpty()) {
			return;
		}

		parentTestDescriptor.orderChildren(children -> {
			Stream<TestDescriptor> nonMatchingTestDescriptors = children.stream()//
					.filter(childTestDescriptor -> !matchingChildrenType.isInstance(childTestDescriptor));

			descriptorWrapperOrderer.orderWrappers(matchingDescriptorWrappers,
				message -> reportWarning(parentTestDescriptor, message));

			Stream<TestDescriptor> orderedTestDescriptors = matchingDescriptorWrappers.stream()//
					.map(AbstractAnnotatedDescriptorWrapper::getTestDescriptor);

			if (shouldNonMatchingDescriptorsComeBeforeOrderedOnes()) {
				return Stream.concat(nonMatchingTestDescriptors, orderedTestDescriptors)//
						.collect(toList());
			}
			else {
				return Stream.concat(orderedTestDescriptors, nonMatchingTestDescriptors)//
						.collect(toList());
			}
		});
	}

	private void reportWarning(TestDescriptor parentTestDescriptor, String message) {
		issueReporter.reportIssue(DiscoveryIssue.builder(Severity.WARNING, message) //
				.source(parentTestDescriptor.getSource()));
	}

	protected abstract boolean shouldNonMatchingDescriptorsComeBeforeOrderedOnes();

	/**
	 * @param <WRAPPER> the wrapper type for the children to order
	 */
	protected static class DescriptorWrapperOrderer<ORDERER, WRAPPER> {

		private static final DescriptorWrapperOrderer<?, ?> NOOP = new DescriptorWrapperOrderer<>(null, null, __ -> "",
			___ -> "");

		@SuppressWarnings("unchecked")
		protected static <ORDERER, WRAPPER> DescriptorWrapperOrderer<ORDERER, WRAPPER> noop() {
			return (DescriptorWrapperOrderer<ORDERER, WRAPPER>) NOOP;
		}

		@Nullable
		private final ORDERER orderer;
		@Nullable
		private final Consumer<List<WRAPPER>> orderingAction;

		private final MessageGenerator descriptorsAddedMessageGenerator;
		private final MessageGenerator descriptorsRemovedMessageGenerator;

		DescriptorWrapperOrderer(@Nullable ORDERER orderer, @Nullable Consumer<List<WRAPPER>> orderingAction,
				MessageGenerator descriptorsAddedMessageGenerator,
				MessageGenerator descriptorsRemovedMessageGenerator) {

			this.orderer = orderer;
			this.orderingAction = orderingAction;
			this.descriptorsAddedMessageGenerator = descriptorsAddedMessageGenerator;
			this.descriptorsRemovedMessageGenerator = descriptorsRemovedMessageGenerator;
		}

		@Nullable
		ORDERER getOrderer() {
			return orderer;
		}

		private boolean canOrderWrappers() {
			return this.orderingAction != null;
		}

		private void orderWrappers(List<WRAPPER> wrappers, Consumer<String> errorHandler) {
			List<WRAPPER> orderedWrappers = new ArrayList<>(wrappers);
			requireNonNull(this.orderingAction).accept(orderedWrappers);
			Map<Object, Integer> distinctWrappersToIndex = distinctWrappersToIndex(orderedWrappers);

			int difference = orderedWrappers.size() - wrappers.size();
			int distinctDifference = distinctWrappersToIndex.size() - wrappers.size();
			if (difference > 0) { // difference >= distinctDifference
				reportDescriptorsAddedWarning(difference, errorHandler);
			}
			if (distinctDifference < 0) { // distinctDifference <= difference
				reportDescriptorsRemovedWarning(distinctDifference, errorHandler);
			}

			wrappers.sort(comparing(wrapper -> distinctWrappersToIndex.getOrDefault(wrapper, -1)));
		}

		private Map<Object, Integer> distinctWrappersToIndex(List<?> wrappers) {
			Map<Object, Integer> toIndex = new HashMap<>();
			for (int i = 0; i < wrappers.size(); i++) {
				// Avoid ClassCastException if a misbehaving ordering action added a non-WRAPPER
				Object wrapper = wrappers.get(i);
				if (!toIndex.containsKey(wrapper)) {
					toIndex.put(wrapper, i);
				}
			}
			return toIndex;
		}

		private void reportDescriptorsAddedWarning(int number, Consumer<String> errorHandler) {
			errorHandler.accept(this.descriptorsAddedMessageGenerator.generateMessage(number));
		}

		private void reportDescriptorsRemovedWarning(int number, Consumer<String> errorHandler) {
			errorHandler.accept(this.descriptorsRemovedMessageGenerator.generateMessage(Math.abs(number)));
		}

	}

	@FunctionalInterface
	protected interface MessageGenerator {

		String generateMessage(int number);
	}

}
