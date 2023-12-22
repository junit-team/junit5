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
 * The {@code AutoClose} annotation is used to automatically close resources
 * used in tests.
 *
 * <p>This annotation should be applied to fields within test classes. It
 * indicates that the annotated resource should be automatically closed after
 * the test execution.
 *
 * <p>By default, the {@code AutoClose} annotation expects the annotated
 * resource to provide a {@code close()} method that will be invoked for closing
 * the resource. However, developers can customize the closing behavior by
 * providing a different method name through the {@link #value} attribute. For
 * example, setting {@code value = "shutdown"} will look for a method named
 * {@code shutdown()} to close the resource. When multiple annotated resources
 * exist the order of closing them is unspecified.
 *
 * @since 5.11
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@API(status = API.Status.EXPERIMENTAL, since = "5.11")
public @interface AutoClose {

	/**
	 * Specifies the name of the method to invoke for closing the resource.
	 *
	 * <p>The default value is {@code close}.
	 *
	 * @return the method name for closing the resource
	 */
	String value() default "close";

}
