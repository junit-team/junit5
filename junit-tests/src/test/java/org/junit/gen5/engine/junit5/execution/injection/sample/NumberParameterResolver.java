/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection.sample;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ParameterResolver;

/**
 * This is a non-realistic {@link ParameterResolver} that claims to
 * resolve any {@link Number}, when in fact it always resolves an {@link Integer}.
 *
 * <p>This may appear nonsensical; however, there are use cases for which a
 * resolver may think that it can support a particular parameter only to
 * discover later that the actual resolved value is not assignment compatible.
 *
 * <p>For example, consider the case with Spring: the {@code SpringExtension} can
 * theoretically resolve any type of {@code ApplicationContext}, but if the
 * required parameter type is {@code WebApplicationContext} and the user
 * neglects to annotate the test class with {@code @WebAppConfiguration} then
 * the {@code ApplicationContext} loaded by Spring's testing support will in
 * fact be an {@code ApplicationContext} but not a {@code WebApplicationContext}.
 * Since Spring does not determine this in advance, such a scenario would lead to
 * an {@link IllegalArgumentException} with the message "argument type mismatch"
 * when JUnit attempts to invoke the test method.
 *
 * @since 5.0
 */
public class NumberParameterResolver implements ParameterResolver {

	@Override
	public boolean supports(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext) {
		return Number.class.isAssignableFrom(parameter.getType());
	}

	@Override
	public Object resolve(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext) {
		return new Integer(42);
	}

}
