/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to use for running tests only when the specified ports are available.
 *
 * If the ports are not available then the test is skipped with an appropriate message.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnabledIfPortsAvailableCondition.class)
public @interface EnabledIfPortsAvailable {
  /**
   * Returns the ports that should be available, in order to run the test.
   */
  int[] value();
}
