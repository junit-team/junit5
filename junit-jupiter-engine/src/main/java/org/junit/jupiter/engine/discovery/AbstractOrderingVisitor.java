/*
 * Copyright 2015-2024 the original author or authors.
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

/**
 * Abstract base class for {@linkplain TestDescriptor.Visitor visitors} that
 * order children nodes.
 *
 * @param <PARENT> the parent container type to search in for matching children
 * @param <CHILD> the type of children (containers or tests) to order
 * @param <WRAPPER> the wrapper type for the children to order
 * @since 5.8
 */
abstract class AbstractOrderingVisitor<PARENT extends TestDescriptor, CHILD extends TestDescriptor, WRAPPER extends AbstractAnnotatedDescriptorWrapper<?>>
		implements TestDescriptor.Visitor {

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
			Function<CHILD, WRAPPER> descriptorWrapperFactory, DescriptorWrapperOrderer descriptorWrapperOrderer) {

		Set<? extends TestDescriptor> children = parentTestDescriptor.getChildren();

		List<WRAPPER> matchingDescriptorWrappers = children.stream()//
				.filter(matchingChildrenType::isInstance)//
				.map(matchingChildrenType::cast)//
				.map(descriptorWrapperFactory)//
				.collect(toCollection(ArrayList::new));

		// If there are no children to order, abort early.
		if (matchingDescriptorWrappers.isEmpty()) {
			return;
		}

		if (descriptorWrapperOrderer.canOrderWrappers()) {
			List<TestDescriptor> nonMatchingTestDescriptors = children.stream()//
					.filter(childTestDescriptor -> !matchingChildrenType.isInstance(childTestDescriptor))//
					.collect(Collectors.toList());

			// Make a local copy for later validation
			Set<WRAPPER> originalWrappers = new LinkedHashSet<>(matchingDescriptorWrappers);

			descriptorWrapperOrderer.orderWrappers(matchingDescriptorWrappers);

			int difference = matchingDescriptorWrappers.size() - originalWrappers.size();
			if (difference > 0) {
				descriptorWrapperOrderer.logDescriptorsAddedWarning(difference);
			}
			else if (difference < 0) {
				descriptorWrapperOrderer.logDescriptorsRemovedWarning(difference);
			}

			Set<TestDescriptor> orderedTestDescriptors = matchingDescriptorWrappers.stream()//
					.filter(originalWrappers::contains)//
					.map(AbstractAnnotatedDescriptorWrapper::getTestDescriptor)//
					.collect(toCollection(LinkedHashSet::new));

			// There is currently no way to removeAll or addAll children at once, so we
			// first remove them all and then add them all back.
			Stream.concat(orderedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
					.forEach(parentTestDescriptor::removeChild);

			// If we are ordering children of type ClassBasedTestDescriptor, that means we
			// are ordering top-level classes or @Nested test classes. Thus, the
			// nonMatchingTestDescriptors list is either empty (for top-level classes) or
			// contains only local test methods (for @Nested classes) which must be executed
			// before tests in @Nested test classes. So we add the test methods before adding
			// the @Nested test classes.
			if (matchingChildrenType == ClassBasedTestDescriptor.class) {
				Stream.concat(nonMatchingTestDescriptors.stream(), orderedTestDescriptors.stream())//
						.forEach(parentTestDescriptor::addChild);
			}
			// Otherwise, we add the ordered descriptors before the non-matching descriptors,
			// which is the case when we are ordering test methods. In other words, local
			// test methods always get added before @Nested test classes.
			else {
				Stream.concat(orderedTestDescriptors.stream(), nonMatchingTestDescriptors.stream())//
						.forEach(parentTestDescriptor::addChild);
			}
		}

		// Recurse through the children in order to support ordering for @Nested test classes.
		matchingDescriptorWrappers.forEach(descriptorWrapper -> {
			TestDescriptor newParentTestDescriptor = descriptorWrapper.getTestDescriptor();
			DescriptorWrapperOrderer newDescriptorWrapperOrderer = getDescriptorWrapperOrderer(descriptorWrapperOrderer,
				descriptorWrapper);

			orderChildrenTestDescriptors(newParentTestDescriptor, matchingChildrenType, descriptorWrapperFactory,
				newDescriptorWrapperOrderer);
		});
	}

	/**
	 * Get the {@link DescriptorWrapperOrderer} for the supplied {@link AbstractAnnotatedDescriptorWrapper}.
	 *
	 * <p>The default implementation returns the supplied {@code DescriptorWrapperOrderer}.
	 *
	 * @return a new {@code DescriptorWrapperOrderer} or the one supplied as an argument
	 */
	protected DescriptorWrapperOrderer getDescriptorWrapperOrderer(
			DescriptorWrapperOrderer inheritedDescriptorWrapperOrderer,
			AbstractAnnotatedDescriptorWrapper<?> descriptorWrapper) {

		return inheritedDescriptorWrapperOrderer;
	}

	protected class DescriptorWrapperOrderer {

		private final Consumer<List<WRAPPER>> orderingAction;

		private final MessageGenerator descriptorsAddedMessageGenerator;

		private final MessageGenerator descriptorsRemovedMessageGenerator;

		DescriptorWrapperOrderer(Consumer<List<WRAPPER>> orderingAction,
				MessageGenerator descriptorsAddedMessageGenerator,
				MessageGenerator descriptorsRemovedMessageGenerator) {

			this.orderingAction = orderingAction;
			this.descriptorsAddedMessageGenerator = descriptorsAddedMessageGenerator;
			this.descriptorsRemovedMessageGenerator = descriptorsRemovedMessageGenerator;
		}

		private boolean canOrderWrappers() {
			return this.orderingAction != null;
		}

		private void orderWrappers(List<WRAPPER> wrappers) {
			this.orderingAction.accept(wrappers);
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
