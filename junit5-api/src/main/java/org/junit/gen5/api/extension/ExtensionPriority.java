/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ExtendWith} is an annotation that is used to prioritize {@linkplain TestExtension test extensions} in case a
 * {@linkplain ExtensionConfigurationException} is thrown. The order of the extensions listed in {@code value()}
 * determines prioritization. For example, when two extensions register an {@linkplain ExtensionPoint} with
 * {@link ExtensionPoint.Postion}.FIRST than the first extension's extension point will really be first, the other one
 * second..
 *
 * @since 5.0
 * @see TestExtension
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExtensionPriority {

	Class<? extends TestExtension>[]value();

}
