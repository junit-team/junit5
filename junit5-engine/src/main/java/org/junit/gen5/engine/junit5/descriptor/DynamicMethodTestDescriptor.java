/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.gen5.engine.support.hierarchical.Leaf;

@API(Internal)
public class DynamicMethodTestDescriptor extends JUnit5TestDescriptor implements Leaf<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	private final Method testMethod;

	public DynamicMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = Preconditions.notNull(testMethod, "Method must not be null");
		this.displayName = determineDisplayName(testMethod, testMethod.getName());

		setSource(new JavaSource(testMethod));
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	@Override
	public String getName() {
		// Intentionally get the class name via getTestClass() instead of
		// testMethod.getDeclaringClass() in order to ensure that inherited
		// test methods in different test subclasses get different names
		// via this method (e.g., for reporting purposes). The caller is,
		// however, still able to determine the declaring class via
		// reflection is necessary.

		// TODO Consider extracting JUnit 5's "method representation" into a common utility.
		return String.format("%s#%s(%s)", getTestClass().getName(), testMethod.getName(),
			StringUtils.nullSafeToString(testMethod.getParameterTypes()));
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) throws Exception {
		return context;
		//		ExtensionRegistry extensionRegistry = populateNewExtensionRegistryFromExtendWith(testMethod,
		//			context.getExtensionRegistry());
		//		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		//		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
		//			context.getExecutionListener(), this, testInstance);
		//
//		// @formatter:off
//		return context.extend()
//				.withExtensionRegistry(extensionRegistry)
//				.withExtensionContext(testExtensionContext)
//				.build();
//		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Exception {
		//		ConditionEvaluationResult evaluationResult = new ConditionEvaluator().evaluateForTest(
		//			context.getExtensionRegistry(), (TestExtensionContext) context.getExtensionContext());
		//		if (evaluationResult.isDisabled()) {
		//			return SkipResult.skip(evaluationResult.getReason().orElse(""));
		//		}
		return SkipResult.dontSkip();
	}

	@Override
	public JUnit5EngineExecutionContext execute(JUnit5EngineExecutionContext context) throws Exception {
		//		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		//		ThrowableCollector throwableCollector = new ThrowableCollector();
		//
		//		invokeInstancePostProcessorExtensionPoints(context.getExtensionRegistry(), testExtensionContext);
		//		invokeBeforeEachExtensionPoints(context.getExtensionRegistry(), testExtensionContext);
		//		invokeTestMethod(context.getExtensionRegistry(), testExtensionContext, throwableCollector);
		//		invokeAfterEachExtensionPoints(context.getExtensionRegistry(), testExtensionContext, throwableCollector);
		//
		//		throwableCollector.assertEmpty();

		return context;
	}

}
