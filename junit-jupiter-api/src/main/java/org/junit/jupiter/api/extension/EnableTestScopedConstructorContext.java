/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

/**
 * {@code @EnableTestScopedConstructorContext} allows
 * {@link Extension Extensions} to use a test-scoped {@link ExtensionContext}
 * during creation of test instances.
 *
 * <p>The annotation should be used on extension classes.
 * JUnit will call the following extension callbacks of annotated extensions
 * with a test-scoped {@link ExtensionContext}, unless the test class is
 * annotated with {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}.
 *
 * <ul>
 * <li>{@link InvocationInterceptor#interceptTestClassConstructor(InvocationInterceptor.Invocation, ReflectiveInvocationContext, ExtensionContext) InvocationInterceptor.interceptTestClassConstructor(...)}</li>
 * <li>{@link ParameterResolver} when resolving constructor parameters</li>
 * <li>{@link TestInstancePreConstructCallback}</li>
 * <li>{@link TestInstancePostProcessor}</li>
 * <li>{@link TestInstanceFactory}</li>
 * </ul>
 *
 * <p>Implementations of these extension callbacks can observe the following
 * differences if they are using {@code @EnableTestScopedConstructorContext}.
 *
 * <ul>
 * <li>{@link ExtensionContext#getElement() getElement()} may refer to the test
 * method and {@link ExtensionContext#getTestClass() getTestClass()} may refer
 * to a nested test class. Use {@link TestInstanceFactoryContext#getTestClass()}
 * to get the class under construction.</li>
 * <li>{@link ExtensionContext#getTestMethod() getTestMethod()} is no-longer
 * empty, unless the test class is annotated with
 * {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}.</li>
 * <li>If the callback adds a new {@link CloseableResource CloseableResource} to
 * the {@link Store Store}, the resource is closed just after the instance is
 * destroyed.</li>
 * <li>The callbacks can now access data previously stored by
 * {@link TestTemplateInvocationContext}, unless the test class is annotated
 * with {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}.</li>
 * </ul>
 *
 * <p><strong>Note</strong>: The behavior which is enabled by this annotation is
 * expected to become the default in future versions of JUnit Jupiter. To ensure
 * future compatibility, extension vendors are therefore advised to annotate
 * their extensions, even if they don't need the new functionality.
 *
 * @since 5.12
 * @see InvocationInterceptor
 * @see ParameterResolver
 * @see TestInstancePreConstructCallback
 * @see TestInstancePostProcessor
 * @see TestInstanceFactory
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@API(status = MAINTAINED, since = "5.12")
public @interface EnableTestScopedConstructorContext {
}
