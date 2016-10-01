/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.platform.commons.meta.API;

/**
 * Test whether a method is a JUnit Jupiter test factory container.
 *
 * @since 5.0
 */
@API(Internal)
public class IsTestFactoryExtensionContainer implements Predicate<Class<?>> {

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();
	private static final IsTestFactoryExtensionElement isTestFactoryExtensionElement = new IsTestFactoryExtensionElement();

	@Override
	public boolean test(Class<?> candidate) {
		return isPotentialTestContainer.and(isTestFactoryExtensionElement).test(candidate);
	}

}
