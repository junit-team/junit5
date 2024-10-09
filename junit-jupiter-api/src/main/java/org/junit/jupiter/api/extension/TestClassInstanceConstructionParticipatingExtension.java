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

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance;

/**
 * Interface for {@link Extension Extensions} that participate in the
 * construction of test class instances.
 *
 * @since 5.12
 * @see InvocationInterceptor#interceptTestClassConstructor
 * @see ParameterResolver
 * @see TestInstancePreConstructCallback
 * @see TestInstancePostProcessor
 * @see TestInstanceFactory
 */
@API(status = EXPERIMENTAL, since = "5.12")
public interface TestClassInstanceConstructionParticipatingExtension extends Extension {

	/**
	 * Whether this extension should receive a test-scoped
	 * {@link ExtensionContext} during the creation of test instances.
	 *
	 * <p>If an extension returns
	 * {@link ExtensionContextScope#TEST_SCOPED TEST_SCOPED} from this method,
	 * the following extension methods will be called with a test-scoped
	 * {@link ExtensionContext} instead of a class-scoped one, unless the
	 * {@link TestInstance.Lifecycle#PER_CLASS PER_CLASS} lifecycle is used:
	 *
	 * <ul>
	 * <li>{@link InvocationInterceptor#interceptTestClassConstructor}</li>
	 * <li>{@link ParameterResolver} when resolving constructor parameters</li>
	 * <li>{@link TestInstancePreConstructCallback}</li>
	 * <li>{@link TestInstancePostProcessor}</li>
	 * <li>{@link TestInstanceFactory}</li>
	 * </ul>
	 *
	 * <p>In such cases, implementations of these extension callbacks can
	 * observe the following differences:
	 *
	 * <ul>
	 * <li>{@link ExtensionContext#getElement() getElement()} may refer to the
	 * test method and {@link ExtensionContext#getTestClass() getTestClass()}
	 * may refer to a nested test class.
	 * Use {@link TestInstanceFactoryContext#getTestClass()} to get the class
	 * under construction.</li>
	 * <li>{@link ExtensionContext#getTestMethod() getTestMethod()} is no-longer
	 * empty, unless the test class is annotated with
	 * {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}.</li>
	 * <li>If the callback adds a new {@link ExtensionContext.Store.CloseableResource} to the
	 * {@link ExtensionContext.Store}, the resource is closed just after the instance is
	 * destroyed.</li>
	 * <li>The callbacks can now access data previously stored by
	 * {@link TestTemplateInvocationContext}, unless the test class is annotated
	 * with {@link TestInstance @TestInstance(Lifecycle.PER_CLASS)}.</li>
	 * </ul>
	 *
	 * <p><strong>Note</strong>: The behavior which is enabled by returning
	 * {@link ExtensionContextScope#TEST_SCOPED TEST_SCOPED} from this method
	 * will become the default in future versions of JUnit. To ensure future
	 * compatibility, extension implementors are therefore advised to opt in,
	 * even if they don't require the new functionality.
	 *
	 * @implNote There are no guarantees about how often this method is called.
	 *           Therefore, implementations should be idempotent and avoid side
	 *           effects. They may, however, cache the result for performance in
	 *           the {@link ExtensionContext.Store Store} of the supplied
	 *           {@link ExtensionContext}, if necessary.
	 * @param rootContext the root extension context to allow inspection of
	 *                    configuration parameters; never {@code null}
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	default ExtensionContextScope getExtensionContextScopeDuringTestClassInstanceConstruction(
			ExtensionContext rootContext) {
		return ExtensionContextScope.DEFAULT;
	}

	/**
	 * {@code ExtensionContextScope} is used to define the scope of the
	 * {@link ExtensionContext} passed to an extension during the creation of
	 * test instances.
	 *
	 * @since 5.12
	 * @see TestClassInstanceConstructionParticipatingExtension#getExtensionContextScopeDuringTestClassInstanceConstruction
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	enum ExtensionContextScope {

		/**
		 * The extension should receive an {@link ExtensionContext} scoped to
		 * the test class.
		 *
		 * @deprecated This behavior will be removed from future versions of
		 * JUnit and {@link #TEST_SCOPED} will become the default.
		 */
		@API(status = DEPRECATED, since = "5.12") //
		@Deprecated
		DEFAULT,

		/**
		 * The extension should receive an {@link ExtensionContext} scoped to
		 * the test instance.
		 */
		TEST_SCOPED
	}

}
