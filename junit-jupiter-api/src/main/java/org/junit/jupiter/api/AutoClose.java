/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * The {@code AutoClose} annotation is used to automatically close resources used in JUnit 5 tests.
 *
 * <p>
 * This annotation should be applied to fields within JUnit 5 test classes. It indicates that the annotated
 * resource should be automatically closed after the test execution. The annotation targets
 * {@link java.lang.annotation.ElementType#FIELD} elements, allowing it to be applied to instance variables.
 * </p>
 *
 * <p>
 * By default, the {@code AutoClose} annotation expects the annotated resource to provide a {@code close()} method
 * that will be invoked for closing the resource. However, developers can customize the closing behavior by providing
 * a different method name through the {@code value} attribute. For example, setting {@code value = "destroy"} will
 * look for a method named {@code destroy()} to close the resource.
 * </p>
 *
 * <p>
 * The {@code AutoClose} annotation is retained at runtime, allowing it to be accessed and processed during test execution.
 * </p>
 *
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 * @since 5.11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@API(status = API.Status.EXPERIMENTAL, since = "5.11")
public @interface AutoClose {

	/**
	 * Specifies the name of the method to invoke for closing the resource.
	 * The default value is "close".
	 *
	 * @return the method name for closing the resource
	 */
	String value() default "close";

}
