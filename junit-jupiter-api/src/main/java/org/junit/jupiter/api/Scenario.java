/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @Scenario} is used to signal that the annotated method is a
 * <em>test factory</em> method with fail-fast execution mode.
 *
 * @since 5.0
 * @see TestFactory
 * @see DynamicTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@TestFactory
public @interface Scenario {
}
