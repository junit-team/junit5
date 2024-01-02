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

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;

/**
 * {@code InvocationInterceptor} defines the API for {@link Extension
 * Extensions} that wish to intercept calls to test code.
 *
 * <h2>Invocation Contract</h2>
 *
 * <p>Each method in this class must call {@link Invocation#proceed()} or {@link
 * Invocation#skip()} exactly once on the supplied invocation. Otherwise, the
 * enclosing test or container will be reported as failed.
 *
 * <p>The default implementation calls {@link Invocation#proceed()
 * proceed()} on the supplied {@linkplain Invocation invocation}.
 *
 * <h2>Constructor Requirements</h2>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.5
 * @see Invocation
 * @see ReflectiveInvocationContext
 * @see ExtensionContext
 */
@API(status = STABLE, since = "5.10")
public interface InvocationInterceptor extends Extension {

	/**
	 * Intercept the invocation of a test class constructor.
	 *
	 * <p>Note that the test class may <em>not</em> have been initialized
	 * (static initialization) when this method is invoked.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @param <T> the result type
	 * @return the result of the invocation; never {@code null}
	 * @throws Throwable in case of failure
	 */
	default <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link BeforeAll @BeforeAll} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link BeforeEach @BeforeEach} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link Test @Test} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link TestFactory @TestFactory} method,
	 * such as a {@link org.junit.jupiter.api.RepeatedTest @RepeatedTest} or
	 * {@code @ParameterizedTest} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @param <T> the result type
	 * @return the result of the invocation; potentially {@code null}
	 * @throws Throwable in case of failures
	 */
	default <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		return invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link TestTemplate @TestTemplate} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link DynamicTest}.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 * @deprecated use {@link #interceptDynamicTest(Invocation, DynamicTestInvocationContext, ExtensionContext)} instead
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "5.8")
	default void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of a {@link DynamicTest}.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	@API(status = EXPERIMENTAL, since = "5.8")
	default void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		// by default call the old interceptDynamicTest(Invocation, ExtensionContext) method so that existing extensions still work
		interceptDynamicTest(invocation, extensionContext);
	}

	/**
	 * Intercept the invocation of an {@link AfterEach @AfterEach} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * Intercept the invocation of an {@link AfterAll @AfterAll} method.
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable in case of failures
	 */
	default void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	/**
	 * An invocation that returns a result and may throw a {@link Throwable}.
	 *
	 * <p>This interface is not intended to be implemented by clients.
	 *
	 * @param <T> the result type
	 * @since 5.5
	 */
	@API(status = STABLE, since = "5.10")
	interface Invocation<T> {

		/**
		 * Proceed with this invocation.
		 *
		 * @return the result of this invocation; potentially {@code null}.
		 * @throws Throwable in case the invocation failed
		 */
		T proceed() throws Throwable;

		/**
		 * Explicitly skip this invocation.
		 *
		 * <p>This allows to bypass the check that {@link #proceed()} must be
		 * called at least once. The default implementation does nothing.
		 */
		@API(status = STABLE, since = "5.10")
		default void skip() {
			// do nothing
		}
	}

}
