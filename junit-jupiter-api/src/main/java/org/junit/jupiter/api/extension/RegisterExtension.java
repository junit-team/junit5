/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @RegisterExtension} is used to register an {@link Extension} via a
 * field in a test class.
 *
 * <p>In contrast to {@link ExtendWith @ExtendWith} which is used to register
 * extensions <em>declaratively</em>, {@code @RegisterExtension} can be used to
 * register an extension <em>programmatically</em> &mdash; for example, in order
 * to pass arguments to the extension's constructor, {@code static} factory
 * method, or builder API.
 *
 * <p>{@code @RegisterExtension} fields must not be {@code private}. If a
 * {@code @RegisterExtension} field is {@code static}, the extension will be
 * registered after extensions registered at the class level via
 * {@code @ExtendWith}; otherwise, the extension will be registered after
 * extensions registered at the method level via {@code @ExtendWith}.
 *
 * <h3>Example Usage</h3>
 *
 * <p>In the following example, the {@code docs} field in the test class is
 * initialized programmatically by supplying a custom {@code lookUpDocsDir()}
 * method to a {@code static} factory method in the {@code DocumentationExtension}.
 * The configured {@code DocumentationExtension} will be automatically registered
 * as an extension. In addition, test methods can access the instance of the
 * extension via the {@code docs} field if necessary.
 *
 * <pre style="code">
 * class DocumentationTests {
 *
 *     static Path lookUpDocsDir() {
 *         // return path to docs dir
 *     }
 *
 *     {@literal @}RegisterExtension
 *     DocumentationExtension docs =
 *         DocumentationExtension.forPath(lookUpDocsDir());
 *
 *     {@literal @}Test
 *     void generateDocumentation() {
 *         // use docs ...
 *     }
 * }</pre>
 *
 * <h3>Supported Extension APIs</h3>
 * <ul>
 * <li>{@link ExecutionCondition}</li>
 * <li>{@link BeforeAllCallback}</li>
 * <li>{@link AfterAllCallback}</li>
 * <li>{@link BeforeEachCallback}</li>
 * <li>{@link AfterEachCallback}</li>
 * <li>{@link BeforeTestExecutionCallback}</li>
 * <li>{@link AfterTestExecutionCallback}</li>
 * <li>{@link TestInstancePostProcessor}</li>
 * <li>{@link ParameterResolver}</li>
 * <li>{@link TestExecutionExceptionHandler}</li>
 * <li>{@link TestTemplateInvocationContextProvider}</li>
 * </ul>
 *
 * @since 5.1
 * @see ExtendWith
 * @see Extension
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.1")
public @interface RegisterExtension {
}
