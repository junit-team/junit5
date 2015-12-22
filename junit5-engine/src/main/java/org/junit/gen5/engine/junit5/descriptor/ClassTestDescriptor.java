/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.engine.junit5.descriptor.MethodContextImpl.methodContext;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.extension.AfterAllExtensionPoint;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.Container;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.Node;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;
import org.junit.gen5.engine.junit5.execution.ThrowingConsumer;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
public class ClassTestDescriptor extends JUnit5TestDescriptor implements Container<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	ClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId);

		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
		this.displayName = determineDisplayName(testClass, testClass.getName());

		setSource(new JavaSource(testClass));
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public final Set<TestTag> getTags() {
		return getTags(this.testClass);
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) {
		TestExtensionRegistry newExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testClass,
			context.getTestExtensionRegistry());
		registerBeforeAllMethods(newExtensionRegistry);
		registerAfterAllMethods(newExtensionRegistry);
		registerBeforeEachMethods(newExtensionRegistry);
		registerAfterEachMethods(newExtensionRegistry);

		context = context.extend().withTestExtensionRegistry(newExtensionRegistry).build();

		ContainerExtensionContext containerExtensionContext = new ClassBasedContainerExtensionContext(
			context.getExtensionContext(), this);

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context))
				.withExtensionContext(containerExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Throwable {
		return invokeContainerExecutionConditionExtensionPoints(context.getTestExtensionRegistry(),
			(ContainerExtensionContext) context.getExtensionContext());
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) throws Throwable {

		TestExtensionRegistry extensionRegistry = context.getTestExtensionRegistry();
		ContainerExtensionContext containerExtensionContext = (ContainerExtensionContext) context.getExtensionContext();

		invokeBeforeAllExtensionPoints(extensionRegistry, containerExtensionContext);

		return context;
	}

	@Override
	public JUnit5EngineExecutionContext afterAll(JUnit5EngineExecutionContext context) throws Throwable {
		List<Throwable> throwablesCollector = new LinkedList<>();
		try {
			invokeAfterAllExtensionPoints(context.getTestExtensionRegistry(),
				(ContainerExtensionContext) context.getExtensionContext(), throwablesCollector);
		}
		catch (ReflectionUtils.TargetExceptionWrapper wrapper) {
			throwablesCollector.add(wrapper.getTargetException());
		}
		catch (Throwable throwable) {
			throwablesCollector.add(throwable);
		}

		throwIfAnyThrowablePresent(throwablesCollector);

		return context;
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	//TODO: Remove duplication with MethodTestDescriptor.invokeTestExecutionCondition
	private SkipResult invokeContainerExecutionConditionExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			ContainerExtensionContext containerExtensionContext) throws Throwable {

		//TODO: Should all conditions be executed? Does the first failing win?
		Set<ConditionEvaluationResult> conditionEvaluationResults = new HashSet<>();
		ThrowingConsumer<RegisteredExtensionPoint<ContainerExecutionCondition>> applyContainerExecutionCondition = registeredExtensionPoint -> {
			conditionEvaluationResults.add(
				registeredExtensionPoint.getExtensionPoint().evaluate(containerExtensionContext));
		};
		newTestExtensionRegistry.applyExtensionPoints(ContainerExecutionCondition.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD, applyContainerExecutionCondition);

		ConditionEvaluationResult conditionResult = conditionEvaluationResults.stream().filter(
			result -> result.isDisabled()).findFirst().orElse(ConditionEvaluationResult.enabled(null));

		if (conditionResult.isDisabled()) {
			return SkipResult.skip(conditionResult.getReason().get());
		}
		else {
			return SkipResult.dontSkip();
		}
	}

	private void invokeBeforeAllExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			ContainerExtensionContext containerExtensionContext) throws Throwable {
		ThrowingConsumer<RegisteredExtensionPoint<BeforeAllExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
			registeredExtensionPoint.getExtensionPoint().beforeAll(containerExtensionContext);
		};
		newTestExtensionRegistry.applyExtensionPoints(BeforeAllExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD, applyBeforeEach);
	}

	private void invokeAfterAllExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			ContainerExtensionContext containerExtensionContext, List<Throwable> throwablesCollector) throws Throwable {
		ThrowingConsumer<RegisteredExtensionPoint<AfterAllExtensionPoint>> applyAfterAll = registeredExtensionPoint -> {
			try {
				registeredExtensionPoint.getExtensionPoint().afterAll(containerExtensionContext);
			}
			catch (ReflectionUtils.TargetExceptionWrapper wrapper) {
				throwablesCollector.add(wrapper.getTargetException());
			}
			catch (Throwable t) {
				throwablesCollector.add(t);
			}
		};
		newTestExtensionRegistry.applyExtensionPoints(AfterAllExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.BACKWARD, applyAfterAll);
	}

	// TODO Remove duplication with registerAfterAllMethods
	private void registerBeforeAllMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> beforeAllMethods = findAnnotatedMethods(testClass, BeforeAll.class, MethodSortOrder.HierarchyDown);
		beforeAllMethods.stream().forEach(method -> {
			if (!ReflectionUtils.isStatic(method)) {
				String message = String.format(
					"Cannot register method '%s' as BeforeAll extension since it is not static.", method.getName());
				throw new ExtensionConfigurationException(message);
			}
			BeforeAllExtensionPoint extensionPoint = containerExtensionContext -> {
				try {
					new MethodInvoker(containerExtensionContext, extensionRegistry).invoke(methodContext(null, method));
				}
				catch (ReflectionUtils.TargetExceptionWrapper wrapper) {
					throw wrapper.getTargetException();
				}

			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	// TODO Remove duplication with registerBeforeAllMethods
	private void registerAfterAllMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> beforeAllMethods = findAnnotatedMethods(testClass, AfterAll.class, MethodSortOrder.HierarchyDown);
		beforeAllMethods.stream().forEach(method -> {
			if (!ReflectionUtils.isStatic(method)) {
				String message = String.format(
					"Cannot register method '%s' as AfterAll extension since it is not static.", method.getName());
				throw new ExtensionConfigurationException(message);
			}
			AfterAllExtensionPoint extensionPoint = containerExtensionContext -> {
				new MethodInvoker(containerExtensionContext, extensionRegistry).invoke(methodContext(null, method));
			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	// TODO Remove duplication with registerAfterEachMethods
	private void registerBeforeEachMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> beforeEachMethods = findAnnotatedMethods(testClass, BeforeEach.class,
			MethodSortOrder.HierarchyDown);
		beforeEachMethods.stream().forEach(method -> {
			BeforeEachExtensionPoint extensionPoint = testExtensionContext -> {
				try {
					runMethodInTestExtensionContext(method, testExtensionContext, extensionRegistry);
				}
				catch (ReflectionUtils.TargetExceptionWrapper wrapper) {
					throw wrapper.getTargetException();
				}
			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	// TODO Remove duplication with registerBeforeEachMethods
	private void registerAfterEachMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> afterEachMethods = findAnnotatedMethods(testClass, AfterEach.class, MethodSortOrder.HierarchyDown);
		afterEachMethods.stream().forEach(method -> {
			AfterEachExtensionPoint extensionPoint = testExtensionContext -> {
				runMethodInTestExtensionContext(method, testExtensionContext, extensionRegistry);
			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	private void runMethodInTestExtensionContext(Method method, TestExtensionContext testExtensionContext,
			TestExtensionRegistry extensionRegistry) {
		Optional<Object> optionalInstance = ReflectionUtils.getOuterInstance(testExtensionContext.getTestInstance(),
			method.getDeclaringClass());
		optionalInstance.ifPresent(instance -> new MethodInvoker(testExtensionContext, extensionRegistry).invoke(
			methodContext(instance, method)));
	}

}
