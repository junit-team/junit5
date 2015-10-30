/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class JUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
	private static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification, TestDescriptor root) {
		// TODO lookup SpecificationResolverRegistry within the ApplicationExecutionContext
		TestDescriptorResolverRegistry testDescriptorResolverRegistry = new TestDescriptorResolverRegistry();
		testDescriptorResolverRegistry.addResolver(ClassNameSpecification.class, new ClassNameTestDescriptorResolver());
		testDescriptorResolverRegistry.addResolver(UniqueIdSpecification.class, new UniqueIdTestDescriptorResolver());

		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		for (TestPlanSpecificationElement element : specification) {
			TestDescriptorResolver testDescriptorResolver = testDescriptorResolverRegistry.forType(element.getClass());
			TestDescriptor descriptor = testDescriptorResolver.resolve(root, element);
			testDescriptors.add(descriptor);
			testDescriptors.addAll(testDescriptorResolver.resolveChildren(descriptor, element));
		}
		return new ArrayList<>(testDescriptors);
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		// TODO super class for Java test descriptors?
		return testDescriptor instanceof JavaMethodTestDescriptor || testDescriptor instanceof JavaClassTestDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {

		// TODO Build a tree of TestDescriptors.
		//
		// Simply iterating over a collection is insufficient for our purposes. We need a
		// tree (or some form of hierarchical data structure) in order to be able to
		// execute each test within the correct scope.
		//
		// For example, we need to execute all test methods within a given test class as a
		// group in order to:
		//
		// 1) retain the instance across test method invocations (if desired).
		// 2) invoke class-level before & after methods _around_ the set of methods.

		// TODO hierarchies of tests must be executed top down
		for (TestDescriptor testDescriptor : context.getTestDescriptions()) {

			Preconditions.condition(supports(testDescriptor),
				String.format("%s supports test descriptors of type %s, not of type %s", getClass().getSimpleName(),
					JavaMethodTestDescriptor.class.getName(),
					(testDescriptor != null ? testDescriptor.getClass().getName() : "null")));

			if (testDescriptor instanceof JavaClassTestDescriptor) {
				continue;
			}

			JavaMethodTestDescriptor javaTestDescriptor = (JavaMethodTestDescriptor) testDescriptor;

			try {
				context.getTestExecutionListener().testStarted(javaTestDescriptor);
				new TestExecutor(javaTestDescriptor).execute();
				context.getTestExecutionListener().testSucceeded(javaTestDescriptor);
			}
			catch (InvocationTargetException ex) {
				Throwable targetException = ex.getTargetException();
				if (targetException instanceof TestSkippedException) {
					context.getTestExecutionListener().testSkipped(javaTestDescriptor, targetException);
				}
				else if (targetException instanceof TestAbortedException) {
					context.getTestExecutionListener().testAborted(javaTestDescriptor, targetException);
				}
				else {
					context.getTestExecutionListener().testFailed(javaTestDescriptor, targetException);
				}
			}
			catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
				throw new IllegalStateException(String.format("Test %s is not well-formed and cannot be executed",
					javaTestDescriptor.getUniqueId()), ex);
			}
			catch (Exception ex) {
				context.getTestExecutionListener().testFailed(javaTestDescriptor, ex);
			}
		}
	}
}
