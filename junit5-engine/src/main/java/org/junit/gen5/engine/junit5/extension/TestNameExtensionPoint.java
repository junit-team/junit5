/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.TestName;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExtension;

/**
 * {@link TestExtension} that registeres a {@link MethodParameterResolver} to inject the name of the currently
 * executing test for {@code String} method parameters annotated with
 * {@link TestName @TestName}.
 *
 * @since 5.0
 */
public class TestNameExtensionPoint implements TestExtension {

	@Override
	public void registerExtensionPoints(ExtensionPointRegistry registry) {
		registry.register(new TestNameParameterResolver(), MethodParameterResolver.class);
	}

	private class TestNameParameterResolver implements MethodParameterResolver {
		@Override
		public boolean supports(Parameter parameter, ExtensionContext testExecutionContext) {
			return (parameter.getType() == String.class && isAnnotated(parameter, TestName.class));
		}

		@Override
		public Object resolve(Parameter parameter, ExtensionContext extensionContext) {
			return extensionContext.getDisplayName();
		}

	}

}
