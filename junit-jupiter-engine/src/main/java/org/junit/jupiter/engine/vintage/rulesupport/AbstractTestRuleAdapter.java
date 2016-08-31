/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport;

import java.lang.reflect.Method;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.TestRule;

public abstract class AbstractTestRuleAdapter implements GenericBeforeAndAfterAdvice {

	protected void executeMethod(String name, TestRule externalResource) {
		try {
			Method method = externalResource.getClass().getDeclaredMethod(name);
			method.setAccessible(true);
			ReflectionUtils.invokeMethod(method, externalResource);
		}
		catch (NoSuchMethodException | SecurityException e) {
			// TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

}
