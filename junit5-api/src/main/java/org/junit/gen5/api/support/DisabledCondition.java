/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.support;

import static org.junit.gen5.commons.util.AnnotationUtils.*;

import org.junit.gen5.api.Condition;
import org.junit.gen5.api.Disabled;

/**
 * @author Sam Brannen
 * @since 5.0
 * @see Disabled
 */
public class DisabledCondition implements Condition {

	/**
	 * Tests are disabled if {@code @Disabled} is present on the test class
	 * or test method.
	 */
	@Override
	public boolean matches(Context context) {
		return (!findAnnotation(context.getTestClass(), Disabled.class).isPresent()
				&& !findAnnotation(context.getTestMethod(), Disabled.class).isPresent());
	}

}
