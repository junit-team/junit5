/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.annotation.Testable;

/**
 * Opt this test method into combining {@link TestTemplateInvocationContextProvider} instances together into a single
 * one. This means that the product of the tests will be run. This allows combining tests such as the
 * {@link RepeatedTest} and {@code ParameterizedTest} annotations.
 * <p>
 *     <b>Note:</b> Not all {@link TestTemplateInvocationContextProvider} instances may be compatible with this.
 *     Specifically: any implementation of {@link TestTemplateInvocationContextProvider} which does complex initilization
 *     logic in the {@link TestTemplateInvocationContextProvider#provideTestTemplateInvocationContexts(ExtensionContext)}
 *     method may not work. Extenstions that wish to support this option should instead to their work in an extension
 *     that is created, new, each time {@link TestTemplateInvocationContext#getAdditionalExtensions()} is called.
 * </p>
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.8")
@Testable
public @interface CombineTestTemplates {
}
