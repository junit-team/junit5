
/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;

@DisplayNameGeneration(IntrospectiveDisplayNameGenerator.class)
public class FetchTestSuite implements DisplayNameGenerator {

	@Override
	public String generateDisplayNameForClass(Class<?> testClass) {
		return "Suite: " + testClass.getSimpleName();
	}

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		return "Test: " + testMethod.getName().replace("_", " ");
	}
}
