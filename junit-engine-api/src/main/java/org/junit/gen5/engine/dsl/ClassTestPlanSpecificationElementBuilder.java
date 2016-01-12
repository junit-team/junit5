/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.dsl;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassSpecification;

public class ClassTestPlanSpecificationElementBuilder {
	public static ClassSpecification forClass(Class<?> testClass) {
		return new ClassSpecification(testClass);
	}

	public static ClassSpecification forClassName(String className) {
		return forClass(ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not resolve class with name: " + className)));
	}
}
