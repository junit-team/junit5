/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.registerExtensionsFromFields;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterEachMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeEachMethods;
import static org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils.getTestInstanceLifecycle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.TestInstanceProvider;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <h3>Default Display Names</h3>
 *
 * <p>The default display name for a top-level or nested static test class is
 * the fully qualified name of the class with the package name and leading dot
 * (".") removed.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class ClassTestDescriptor extends JupiterTestDescriptor {

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	private final Class<?> testClass;
	private final Set<TestTag> tags;

	private List<Method> beforeAllMethods;
	private List<Method> afterAllMethods;

	public ClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		this(uniqueId, ClassTestDescriptor::generateDefaultDisplayName, testClass);
	}

	protected ClassTestDescriptor(UniqueId uniqueId, Function<Class<?>, String> defaultDisplayNameGenerator,
			Class<?> testClass) {

		super(uniqueId, determineDisplayName(Preconditions.notNull(testClass, "Class must not be null"),
			defaultDisplayNameGenerator), ClassSource.from(testClass));

		this.testClass = testClass;
		this.tags = getTags(testClass);
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		// return modifiable copy
		return new LinkedHashSet<TestTag>(this.tags);
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public String getLegacyReportingName() {
		return this.testClass.getName();
	}

	private static String generateDefaultDisplayName(Class<?> testClass) {
		String name = testClass.getName();
		int index = name.lastIndexOf('.');
		return name.substring(index + 1);
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public ExecutionMode getExecutionMode() {
		return getExecutionMode(getTestClass());
	}

	@Override
	public Set<ExclusiveResource> getExclusiveResources() {
		return getExclusiveResources(getTestClass());
	}

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = populateNewExtensionRegistryFromExtendWithAnnotation(
			context.getExtensionRegistry(), this.testClass);

		// Register extensions from static fields here, at the class level but
		// after extensions registered via @ExtendWith.
		registerExtensionsFromFields(registry, this.testClass, null);
		registerBeforeEachMethodAdapters(registry);
		registerAfterEachMethodAdapters(registry);

		Lifecycle lifecycle = getTestInstanceLifecycle(this.testClass, context.getConfigurationParameters());
		ThrowableCollector throwableCollector = new ThrowableCollector();
		ClassExtensionContext extensionContext = new ClassExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, lifecycle, context.getConfigurationParameters(), throwableCollector);

		this.beforeAllMethods = findBeforeAllMethods(this.testClass, lifecycle == Lifecycle.PER_METHOD);
		this.afterAllMethods = findAfterAllMethods(this.testClass, lifecycle == Lifecycle.PER_METHOD);

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context, registry, extensionContext))
				.withExtensionRegistry(registry)
				.withExtensionContext(extensionContext)
				.withThrowableCollector(throwableCollector)
				.build();
		// @formatter:on
	}

	@Override
	public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		Lifecycle lifecycle = context.getExtensionContext().getTestInstanceLifecycle().orElse(Lifecycle.PER_METHOD);
		if (lifecycle == Lifecycle.PER_CLASS) {
			// Eagerly load test instance for BeforeAllCallbacks, if necessary,
			// and store the instance in the ExtensionContext.
			ClassExtensionContext extensionContext = (ClassExtensionContext) context.getExtensionContext();
			throwableCollector.execute(() -> extensionContext.setTestInstance(
				context.getTestInstanceProvider().getTestInstance(Optional.empty())));
		}

		if (throwableCollector.isEmpty()) {
			context.beforeAllCallbacksExecuted(true);
			invokeBeforeAllCallbacks(context);

			if (throwableCollector.isEmpty()) {
				context.beforeAllMethodsExecuted(true);
				invokeBeforeAllMethods(context);
			}
		}

		throwableCollector.assertEmpty();

		return context;
	}

	@Override
	public void after(JupiterEngineExecutionContext context) {

		boolean throwableWasNotAlreadyThrown = context.getThrowableCollector().isEmpty();

		if (context.beforeAllMethodsExecuted()) {
			invokeAfterAllMethods(context);
		}

		if (context.beforeAllCallbacksExecuted()) {
			invokeAfterAllCallbacks(context);
		}

		// If the ThrowableCollector was not empty when this method was called,
		// that means an exception was already thrown either before or during
		// the execution of this Node. If an exception was already thrown, any
		// later exceptions were added as suppressed exceptions to that original
		// exception, and we don't need to rethrow the original exception here
		// since it will be thrown by HierarchicalTestExecutor.NodeExecutor.
		//
		// However, if no exception was previously thrown, we need to ensure
		// that any exceptions thrown by @AfterAll methods or AfterAllCallbacks
		// are thrown here.
		if (throwableWasNotAlreadyThrown) {
			context.getThrowableCollector().assertEmpty();
		}
	}

	private TestInstanceProvider testInstanceProvider(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ClassExtensionContext extensionContext) {

		TestInstanceProvider testInstanceProvider = childRegistry -> instantiateAndPostProcessTestInstance(
			parentExecutionContext, extensionContext, childRegistry.orElse(registry));

		return childRegistry -> extensionContext.getTestInstance().orElseGet(
			() -> testInstanceProvider.getTestInstance(childRegistry));
	}

	private Object instantiateAndPostProcessTestInstance(JupiterEngineExecutionContext context,
			ExtensionContext extensionContext, ExtensionRegistry registry) {

		Object instance = instantiateTestClass(context, registry, extensionContext);
		invokeTestInstancePostProcessors(instance, registry, extensionContext);
		// In addition, we register extensions from instance fields here since the
		// best time to do that is immediately following test class instantiation
		// and post processing.
		registerExtensionsFromFields(registry, this.testClass, instance);
		return instance;
	}

	protected Object instantiateTestClass(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ExtensionContext extensionContext) {

		Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(this.testClass);
		return executableInvoker.invoke(constructor, extensionContext, registry);
	}

	private void invokeTestInstancePostProcessors(Object instance, ExtensionRegistry registry,
			ExtensionContext context) {

		registry.stream(TestInstancePostProcessor.class).forEach(
			extension -> executeAndMaskThrowable(() -> extension.postProcessTestInstance(instance, context)));
	}

	private void invokeBeforeAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		for (BeforeAllCallback callback : registry.getExtensions(BeforeAllCallback.class)) {
			throwableCollector.execute(() -> callback.beforeAll(extensionContext));
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	private void invokeBeforeAllMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = extensionContext.getTestInstance().orElse(null);

		for (Method method : this.beforeAllMethods) {
			throwableCollector.execute(
				() -> executableInvoker.invoke(method, testInstance, extensionContext, registry));
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	private void invokeAfterAllMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = extensionContext.getTestInstance().orElse(null);

		this.afterAllMethods.forEach(method -> throwableCollector.execute(
			() -> executableInvoker.invoke(method, testInstance, extensionContext, registry)));
	}

	private void invokeAfterAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		registry.getReversedExtensions(AfterAllCallback.class)//
				.forEach(extension -> throwableCollector.execute(() -> extension.afterAll(extensionContext)));
	}

	private void registerBeforeEachMethodAdapters(ExtensionRegistry registry) {
		List<Method> beforeEachMethods = findBeforeEachMethods(this.testClass);
		registerMethodsAsExtensions(beforeEachMethods, registry, this::synthesizeBeforeEachMethodAdapter);
	}

	private void registerAfterEachMethodAdapters(ExtensionRegistry registry) {
		// Make a local copy since findAfterEachMethods() returns an immutable list.
		List<Method> afterEachMethods = new ArrayList<>(findAfterEachMethods(this.testClass));

		// Since the bottom-up ordering of afterEachMethods will later be reversed when the
		// synthesized AfterEachMethodAdapters are executed within TestMethodTestDescriptor,
		// we have to reverse the afterEachMethods list to put them in top-down order before
		// we register them as synthesized extensions.
		Collections.reverse(afterEachMethods);

		registerMethodsAsExtensions(afterEachMethods, registry, this::synthesizeAfterEachMethodAdapter);
	}

	private void registerMethodsAsExtensions(List<Method> methods, ExtensionRegistry registry,
			Function<Method, Extension> extensionSynthesizer) {

		methods.forEach(method -> registry.registerExtension(extensionSynthesizer.apply(method), method));
	}

	private BeforeEachMethodAdapter synthesizeBeforeEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInExtensionContext(method, extensionContext, registry);
	}

	private AfterEachMethodAdapter synthesizeAfterEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInExtensionContext(method, extensionContext, registry);
	}

	private void invokeMethodInExtensionContext(Method method, ExtensionContext context, ExtensionRegistry registry) {
		Object testInstance = context.getRequiredTestInstance();
		testInstance = ReflectionUtils.getOutermostInstance(testInstance, method.getDeclaringClass()).orElseThrow(
			() -> new JUnitException("Failed to find instance for method: " + method.toGenericString()));

		executableInvoker.invoke(method, testInstance, context, registry);
	}

}
