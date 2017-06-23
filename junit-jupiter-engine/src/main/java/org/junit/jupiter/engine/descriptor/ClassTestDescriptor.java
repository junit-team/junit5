/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findAfterEachMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeAllMethods;
import static org.junit.jupiter.engine.descriptor.LifecycleMethodUtils.findBeforeEachMethods;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.TestInstanceProvider;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

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
@API(Internal)
public class ClassTestDescriptor extends JupiterTestDescriptor {

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	private final Class<?> testClass;
	private final Lifecycle lifecycle;

	private final List<Method> beforeAllMethods;
	private final List<Method> afterAllMethods;
	private final List<Method> beforeEachMethods;
	private final List<Method> afterEachMethods;

	public ClassTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
		this(uniqueId, ClassTestDescriptor::generateDefaultDisplayName, testClass);
	}

	protected ClassTestDescriptor(UniqueId uniqueId, Function<Class<?>, String> defaultDisplayNameGenerator,
			Class<?> testClass) {

		super(uniqueId, determineDisplayName(Preconditions.notNull(testClass, "Class must not be null"),
			defaultDisplayNameGenerator));

		this.testClass = testClass;
		this.lifecycle = getTestInstanceLifecycle(testClass);

		this.beforeAllMethods = findBeforeAllMethods(testClass, this.lifecycle == Lifecycle.PER_METHOD);
		this.afterAllMethods = findAfterAllMethods(testClass, this.lifecycle == Lifecycle.PER_METHOD);
		this.beforeEachMethods = findBeforeEachMethods(testClass);
		this.afterEachMethods = findAfterEachMethods(testClass);

		setSource(new ClassSource(testClass));
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public Set<TestTag> getTags() {
		return getTags(this.testClass);
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
		return testClass.getName();
	}

	private static String generateDefaultDisplayName(Class<?> testClass) {
		String name = testClass.getName();
		int index = name.lastIndexOf('.');
		return name.substring(index + 1);
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = populateNewExtensionRegistryFromExtendWith(this.testClass,
			context.getExtensionRegistry());

		registerBeforeEachMethodAdapters(registry);
		registerAfterEachMethodAdapters(registry);

		ContainerExtensionContext containerExtensionContext = new ClassBasedContainerExtensionContext(
			context.getExtensionContext(), context.getExecutionListener(), this);

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context, registry, containerExtensionContext))
				.withExtensionRegistry(registry)
				.withExtensionContext(containerExtensionContext)
				.withThrowableCollector(new ThrowableCollector())
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
		return shouldContainerBeSkipped(context);
	}

	@Override
	public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) throws Exception {
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		invokeBeforeAllCallbacks(context);
		if (throwableCollector.isEmpty()) {
			context.beforeAllMethodsExecuted(true);
			invokeBeforeAllMethods(context);
		}

		throwableCollector.assertEmpty();

		return context;
	}

	@Override
	public void after(JupiterEngineExecutionContext context) throws Exception {
		if (context.beforeAllMethodsExecuted()) {
			invokeAfterAllMethods(context);
		}
		invokeAfterAllCallbacks(context);

		context.getThrowableCollector().assertEmpty();
	}

	protected TestInstanceProvider testInstanceProvider(JupiterEngineExecutionContext parentExecutionContext,
			ExtensionRegistry registry, ExtensionContext extensionContext) {

		return new LifecycleAwareTestInstanceProvider(this.testClass, registry, extensionContext);
	}

	protected void invokeTestInstancePostProcessors(Object instance, ExtensionRegistry registry,
			ExtensionContext context) {

		registry.stream(TestInstancePostProcessor.class).forEach(
			extension -> executeAndMaskThrowable(() -> extension.postProcessTestInstance(instance, context)));
	}

	private void invokeBeforeAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ContainerExtensionContext extensionContext = (ContainerExtensionContext) context.getExtensionContext();
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
		ContainerExtensionContext extensionContext = (ContainerExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = getTestInstanceForClassLevelCallbacks(context);

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
		ContainerExtensionContext extensionContext = (ContainerExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();
		Object testInstance = getTestInstanceForClassLevelCallbacks(context);

		this.afterAllMethods.forEach(method -> throwableCollector.execute(
			() -> executableInvoker.invoke(method, testInstance, extensionContext, registry)));
	}

	private Object getTestInstanceForClassLevelCallbacks(JupiterEngineExecutionContext context) {
		return this.lifecycle == Lifecycle.PER_CLASS
				? context.getTestInstanceProvider().getTestInstance(Optional.empty()) : null;
	}

	private void invokeAfterAllCallbacks(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		ContainerExtensionContext extensionContext = (ContainerExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		registry.getReversedExtensions(AfterAllCallback.class)//
				.forEach(extension -> throwableCollector.execute(() -> extension.afterAll(extensionContext)));
	}

	private void registerBeforeEachMethodAdapters(ExtensionRegistry registry) {
		registerMethodsAsExtensions(this.beforeEachMethods, registry, this::synthesizeBeforeEachMethodAdapter);
	}

	private void registerAfterEachMethodAdapters(ExtensionRegistry registry) {

		// Since the bottom-up ordering of afterEachMethods will later be reversed when the
		// synthesized AfterEachMethodAdapters are executed within MethodTestDescriptor, we
		// have to reverse the afterEachMethods list to put them in top-down order before we
		// register them as synthesized extensions.
		List<Method> reversed = new ArrayList<>(this.afterEachMethods);
		Collections.reverse(reversed);

		registerMethodsAsExtensions(reversed, registry, this::synthesizeAfterEachMethodAdapter);
	}

	private void registerMethodsAsExtensions(List<Method> methods, ExtensionRegistry registry,
			Function<Method, Extension> extensionSynthesizer) {

		methods.forEach(method -> registry.registerExtension(extensionSynthesizer.apply(method), method));
	}

	private BeforeEachMethodAdapter synthesizeBeforeEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInTestExtensionContext(method, extensionContext, registry);
	}

	private AfterEachMethodAdapter synthesizeAfterEachMethodAdapter(Method method) {
		return (extensionContext, registry) -> invokeMethodInTestExtensionContext(method, extensionContext, registry);
	}

	private void invokeMethodInTestExtensionContext(Method method, TestExtensionContext context,
			ExtensionRegistry registry) {

		Object instance = ReflectionUtils.getOuterInstance(context.getTestInstance(),
			method.getDeclaringClass()).orElseThrow(
				() -> new JUnitException("Failed to find instance for method: " + method.toGenericString()));

		executableInvoker.invoke(method, instance, context, registry);
	}

	private final class LifecycleAwareTestInstanceProvider implements TestInstanceProvider {

		private final Class<?> testClass;
		private final ExtensionRegistry registry;
		private final ExtensionContext extensionContext;
		private Object testInstance;

		LifecycleAwareTestInstanceProvider(Class<?> testClass, ExtensionRegistry registry,
				ExtensionContext extensionContext) {

			this.testClass = testClass;
			this.registry = registry;
			this.extensionContext = extensionContext;
		}

		@Override
		public Object getTestInstance(Optional<ExtensionRegistry> childExtensionRegistry) {
			if (ClassTestDescriptor.this.lifecycle == Lifecycle.PER_METHOD) {
				return createTestInstance(childExtensionRegistry);
			}

			// else Lifecycle.PER_CLASS
			if (this.testInstance == null) {
				this.testInstance = createTestInstance(childExtensionRegistry);
			}
			return this.testInstance;
		}

		private Object createTestInstance(Optional<ExtensionRegistry> childExtensionRegistry) {
			Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(this.testClass);
			Object instance = executableInvoker.invoke(constructor, this.extensionContext,
				childExtensionRegistry.orElse(this.registry));
			invokeTestInstancePostProcessors(instance, childExtensionRegistry.orElse(this.registry),
				this.extensionContext);
			return instance;
		}

	}

	private static TestInstance.Lifecycle getTestInstanceLifecycle(Class<?> testClass) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, TestInstance.class)
				.map(TestInstance::value)
				.orElse(Lifecycle.PER_METHOD);
		// @formatter:on
	}

}
