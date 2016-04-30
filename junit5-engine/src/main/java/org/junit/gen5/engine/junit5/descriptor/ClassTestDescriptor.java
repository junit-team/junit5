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
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.engine.junit5.execution.MethodInvocationContextFactory.methodInvocationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.extension.AfterAllCallback;
import org.junit.gen5.api.extension.AfterEachCallback;
import org.junit.gen5.api.extension.BeforeAllCallback;
import org.junit.gen5.api.extension.BeforeEachCallback;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionConfigurationException;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.execution.ConditionEvaluator;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;
import org.junit.gen5.engine.junit5.execution.ThrowableCollector;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;
import org.junit.gen5.engine.junit5.extension.RegisteredExtensionPoint;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.gen5.engine.support.hierarchical.Container;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * @since 5.0
 */
@API(Internal)
public class ClassTestDescriptor extends JUnit5TestDescriptor implements Container<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	public ClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		super(uniqueId);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.displayName = determineDisplayName(testClass, testClass.getName());

		setSource(new JavaSource(testClass));
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public final String getName() {
		return getTestClass().getName();
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public Set<TestTag> getTags() {
		return getTags(this.testClass);
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) {
		ExtensionRegistry newExtensionRegistry = populateNewExtensionRegistryFromExtendWith(testClass,
			context.getExtensionRegistry());

		registerBeforeAllMethods(newExtensionRegistry);
		registerAfterAllMethods(newExtensionRegistry);
		registerBeforeEachMethods(newExtensionRegistry);
		registerAfterEachMethods(newExtensionRegistry);

		context = context.extend().withExtensionRegistry(newExtensionRegistry).build();

		ContainerExtensionContext containerExtensionContext = new ClassBasedContainerExtensionContext(
			context.getExtensionContext(), context.getExecutionListener(), this);

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context))
				.withExtensionContext(containerExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Exception {
		ConditionEvaluationResult evaluationResult = new ConditionEvaluator().evaluateForContainer(
			context.getExtensionRegistry(), (ContainerExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse(""));
		}
		return SkipResult.dontSkip();
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) throws Exception {
		ExtensionRegistry extensionRegistry = context.getExtensionRegistry();
		ContainerExtensionContext containerExtensionContext = (ContainerExtensionContext) context.getExtensionContext();

		invokeBeforeAllCallbacks(extensionRegistry, containerExtensionContext);

		return context;
	}

	@Override
	public JUnit5EngineExecutionContext afterAll(JUnit5EngineExecutionContext context) throws Exception {
		ThrowableCollector throwableCollector = new ThrowableCollector();

		throwableCollector.execute(() -> invokeAfterAllCallbacks(context.getExtensionRegistry(),
			(ContainerExtensionContext) context.getExtensionContext(), throwableCollector));

		throwableCollector.assertEmpty();

		return context;
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	private void invokeBeforeAllCallbacks(ExtensionRegistry newExtensionRegistry,
			ContainerExtensionContext containerExtensionContext) throws Exception {

		Consumer<RegisteredExtensionPoint<BeforeAllCallback>> applyBeforeEach = registeredExtensionPoint -> executeAndMaskThrowable(
			() -> registeredExtensionPoint.getExtensionPoint().beforeAll(containerExtensionContext));

		newExtensionRegistry.stream(BeforeAllCallback.class, ExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			applyBeforeEach);
	}

	private void invokeAfterAllCallbacks(ExtensionRegistry newExtensionRegistry,
			ContainerExtensionContext containerExtensionContext, ThrowableCollector throwableCollector)
			throws Exception {

		Consumer<RegisteredExtensionPoint<AfterAllCallback>> applyAfterAll = registeredExtensionPoint -> throwableCollector.execute(
			() -> registeredExtensionPoint.getExtensionPoint().afterAll(containerExtensionContext));

		newExtensionRegistry.stream(AfterAllCallback.class, ExtensionRegistry.ApplicationOrder.BACKWARD).forEach(
			applyAfterAll);
	}

	private void registerBeforeAllMethods(ExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, BeforeAll.class, BeforeAllCallback.class,
			this::assertStatic, this::synthesizeBeforeAllCallback);
	}

	private void registerAfterAllMethods(ExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, AfterAll.class, AfterAllCallback.class,
			this::assertStatic, this::synthesizeAfterAllCallback);
	}

	private void registerBeforeEachMethods(ExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, BeforeEach.class, BeforeEachCallback.class,
			this::assertNonStatic, this::synthesizeBeforeEachCallback);
	}

	private void registerAfterEachMethods(ExtensionRegistry extensionRegistry) {
		registerAnnotatedMethodsAsExtensions(extensionRegistry, AfterEach.class, AfterEachCallback.class,
			this::assertNonStatic, this::synthesizeAfterEachCallback);
	}

	private void registerAnnotatedMethodsAsExtensions(ExtensionRegistry extensionRegistry,
			Class<? extends Annotation> annotationType, Class<?> extensionType,
			BiConsumer<Class<?>, Method> methodValidator,
			BiFunction<ExtensionRegistry, Method, ExtensionPoint> extensionPointSynthesizer) {

		// @formatter:off
		findAnnotatedMethods(testClass, annotationType, MethodSortOrder.HierarchyDown).stream()
			.peek(method -> methodValidator.accept(extensionType, method))
			.forEach(method ->
				extensionRegistry.registerExtensionPoint(extensionPointSynthesizer.apply(extensionRegistry, method), method));
		// @formatter:on
	}

	private BeforeAllCallback synthesizeBeforeAllCallback(ExtensionRegistry registry, Method method) {
		return extensionContext -> new MethodInvoker(extensionContext, registry).invoke(
			methodInvocationContext(null, method));
	}

	private AfterAllCallback synthesizeAfterAllCallback(ExtensionRegistry registry, Method method) {
		return extensionContext -> new MethodInvoker(extensionContext, registry).invoke(
			methodInvocationContext(null, method));
	}

	private BeforeEachCallback synthesizeBeforeEachCallback(ExtensionRegistry registry, Method method) {
		return extensionContext -> runMethodInTestExtensionContext(method, extensionContext, registry);
	}

	private AfterEachCallback synthesizeAfterEachCallback(ExtensionRegistry registry, Method method) {
		return extensionContext -> runMethodInTestExtensionContext(method, extensionContext, registry);
	}

	private void runMethodInTestExtensionContext(Method method, TestExtensionContext context,
			ExtensionRegistry registry) {

		// @formatter:off
		ReflectionUtils.getOuterInstance(context.getTestInstance(), method.getDeclaringClass())
			.ifPresent(instance -> new MethodInvoker(context, registry).invoke(methodInvocationContext(instance, method)));
		// @formatter:on
	}

	private void assertStatic(Class<?> extensionType, Method method) {
		if (!ReflectionUtils.isStatic(method)) {
			String message = String.format("Cannot register method '%s' as a(n) %s since it is not static.",
				method.getName(), extensionType.getSimpleName());
			throw new ExtensionConfigurationException(message);
		}
	}

	private void assertNonStatic(Class<?> extensionType, Method method) {
		if (ReflectionUtils.isStatic(method)) {
			String message = String.format("Cannot register method '%s' as a(n) %s since it is static.",
				method.getName(), extensionType.getSimpleName());
			throw new ExtensionConfigurationException(message);
		}
	}

}
