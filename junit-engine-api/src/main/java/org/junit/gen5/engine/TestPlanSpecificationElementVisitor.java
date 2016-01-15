/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @since 5.0
 */
public interface TestPlanSpecificationElementVisitor {
	default void visitUniqueId(String uniqueId) {
	}

	default void visitPackage(String packageName) {
	}

	default void visitClass(Class<?> testClass) {
	}

	default void visitMethod(Class<?> testClass, Method testMethod) {
	}

	default void visitAllTests(File rootDirectory) {
	}
}
