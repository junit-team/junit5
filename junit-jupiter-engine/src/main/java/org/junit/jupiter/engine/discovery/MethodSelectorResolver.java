/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.matches;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.Filterable;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsNestedTestClass;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.jupiter.engine.discovery.predicates.IsTestFactoryMethod;
import org.junit.jupiter.engine.discovery.predicates.IsTestMethod;
import org.junit.jupiter.engine.discovery.predicates.IsTestTemplateMethod;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.NestedMethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * @since 5.5
 */
class MethodSelectorResolver implements SelectorResolver {

	private static final Logger logger = LoggerFactory.getLogger(MethodSelectorResolver.class);
	private static final MethodFinder methodFinder = new MethodFinder();
	private static final Predicate<Class<?>> testClassPredicate = new IsTestClassWithTests().or(
		new IsNestedTestClass());

	protected final JupiterConfiguration configuration;

	MethodSelectorResolver(JupiterConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Resolution resolve(MethodSelector selector, Context context) {
		return resolve(context, emptyList(), selector.getJavaClass(), selector::getJavaMethod, Match::exact);
	}

	@Override
	public Resolution resolve(NestedMethodSelector selector, Context context) {
		return resolve(context, selector.getEnclosingClasses(), selector.getNestedClass(), selector::getMethod,
			Match::exact);
	}

	private Resolution resolve(Context context, List<Class<?>> enclosingClasses, Class<?> testClass,
			Supplier<Method> methodSupplier,
			BiFunction<TestDescriptor, Supplier<Set<? extends DiscoverySelector>>, Match> matchFactory) {
		if (!testClassPredicate.test(testClass)) {
			return unresolved();
		}
		Method method = methodSupplier.get();
		// @formatter:off
		Set<Match> matches = Arrays.stream(MethodType.values())
				.map(methodType -> methodType.resolve(enclosingClasses, testClass, method, context, configuration))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(testDescriptor -> matchFactory.apply(testDescriptor, expansionCallback(testDescriptor)))
				.collect(toSet());
		// @formatter:on
		if (matches.size() > 1) {
			logger.warn(() -> {
				Stream<TestDescriptor> testDescriptors = matches.stream().map(Match::getTestDescriptor);
				return String.format(
					"Possible configuration error: method [%s] resulted in multiple TestDescriptors %s. "
							+ "This is typically the result of annotating a method with multiple competing annotations "
							+ "such as @Test, @RepeatedTest, @ParameterizedTest, @TestFactory, etc.",
					method.toGenericString(), testDescriptors.map(d -> d.getClass().getName()).collect(toList()));
			});
		}
		return matches.isEmpty() ? unresolved() : matches(matches);
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {
		UniqueId uniqueId = selector.getUniqueId();
		// @formatter:off
		return Arrays.stream(MethodType.values())
				.map(methodType -> methodType.resolveUniqueIdIntoTestDescriptor(uniqueId, context, configuration))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(testDescriptor -> {
					boolean exactMatch = uniqueId.equals(testDescriptor.getUniqueId());
					if (testDescriptor instanceof Filterable) {
						Filterable filterable = (Filterable) testDescriptor;
						if (exactMatch) {
							filterable.getDynamicDescendantFilter().allowAll();
						}
						else {
							filterable.getDynamicDescendantFilter().allowUniqueIdPrefix(uniqueId);
						}
					}
					return Resolution.match(exactMatch ? Match.exact(testDescriptor) : Match.partial(testDescriptor, expansionCallback(testDescriptor)));
				})
				.findFirst()
				.orElse(unresolved());
		// @formatter:on
	}

	@Override
	public Resolution resolve(IterationSelector selector, Context context) {
		if (selector.getParentSelector() instanceof MethodSelector) {
			MethodSelector methodSelector = (MethodSelector) selector.getParentSelector();
			return resolve(context, emptyList(), methodSelector.getJavaClass(), methodSelector::getJavaMethod,
				(testDescriptor, childSelectorsSupplier) -> {
					if (testDescriptor instanceof Filterable) {
						Filterable filterable = (Filterable) testDescriptor;
						filterable.getDynamicDescendantFilter().allowIndex(selector.getIterationIndices());
					}
					return Match.partial(testDescriptor, childSelectorsSupplier);
				});
		}
		return unresolved();
	}

	private Supplier<Set<? extends DiscoverySelector>> expansionCallback(TestDescriptor testDescriptor) {
		return () -> {
			if (testDescriptor instanceof Filterable) {
				Filterable filterable = (Filterable) testDescriptor;
				filterable.getDynamicDescendantFilter().allowAll();
			}
			return emptySet();
		};
	}

	private enum MethodType {

		TEST(new IsTestMethod(), TestMethodTestDescriptor.SEGMENT_TYPE) {
			@Override
			protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method,
					JupiterConfiguration configuration) {
				return new TestMethodTestDescriptor(uniqueId, testClass, method, configuration);
			}
		},

		TEST_FACTORY(new IsTestFactoryMethod(), TestFactoryTestDescriptor.SEGMENT_TYPE,
				TestFactoryTestDescriptor.DYNAMIC_CONTAINER_SEGMENT_TYPE,
				TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE) {
			@Override
			protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method,
					JupiterConfiguration configuration) {
				return new TestFactoryTestDescriptor(uniqueId, testClass, method, configuration);
			}
		},

		TEST_TEMPLATE(new IsTestTemplateMethod(), TestTemplateTestDescriptor.SEGMENT_TYPE,
				TestTemplateInvocationTestDescriptor.SEGMENT_TYPE) {
			@Override
			protected TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method,
					JupiterConfiguration configuration) {
				return new TestTemplateTestDescriptor(uniqueId, testClass, method, configuration);
			}
		};

		private final Predicate<Method> methodPredicate;
		private final String segmentType;
		private final Set<String> dynamicDescendantSegmentTypes;

		MethodType(Predicate<Method> methodPredicate, String segmentType, String... dynamicDescendantSegmentTypes) {
			this.methodPredicate = methodPredicate;
			this.segmentType = segmentType;
			this.dynamicDescendantSegmentTypes = new LinkedHashSet<>(Arrays.asList(dynamicDescendantSegmentTypes));
		}

		private Optional<TestDescriptor> resolve(List<Class<?>> enclosingClasses, Class<?> testClass, Method method,
				Context context, JupiterConfiguration configuration) {
			if (!methodPredicate.test(method)) {
				return Optional.empty();
			}
			return context.addToParent(() -> selectClass(enclosingClasses, testClass), //
				parent -> Optional.of(
					createTestDescriptor(createUniqueId(method, parent), testClass, method, configuration)));
		}

		private DiscoverySelector selectClass(List<Class<?>> enclosingClasses, Class<?> testClass) {
			if (enclosingClasses.isEmpty()) {
				return DiscoverySelectors.selectClass(testClass);
			}
			return DiscoverySelectors.selectNestedClass(enclosingClasses, testClass);
		}

		private Optional<TestDescriptor> resolveUniqueIdIntoTestDescriptor(UniqueId uniqueId, Context context,
				JupiterConfiguration configuration) {
			UniqueId.Segment lastSegment = uniqueId.getLastSegment();
			if (segmentType.equals(lastSegment.getType())) {
				return context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
					String methodSpecPart = lastSegment.getValue();
					Class<?> testClass = ((ClassBasedTestDescriptor) parent).getTestClass();
					// @formatter:off
					return methodFinder.findMethod(methodSpecPart, testClass)
							.filter(methodPredicate)
							.map(method -> createTestDescriptor(createUniqueId(method, parent), testClass, method, configuration));
					// @formatter:on
				});
			}
			if (dynamicDescendantSegmentTypes.contains(lastSegment.getType())) {
				return resolveUniqueIdIntoTestDescriptor(uniqueId.removeLastSegment(), context, configuration);
			}
			return Optional.empty();
		}

		private UniqueId createUniqueId(Method method, TestDescriptor parent) {
			String methodId = String.format("%s(%s)", method.getName(),
				ClassUtils.nullSafeToString(method.getParameterTypes()));
			return parent.getUniqueId().append(segmentType, methodId);
		}

		protected abstract TestDescriptor createTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method method,
				JupiterConfiguration configuration);

	}

}
