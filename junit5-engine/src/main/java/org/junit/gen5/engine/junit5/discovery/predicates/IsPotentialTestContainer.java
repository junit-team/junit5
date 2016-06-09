/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery.predicates;

import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.ReflectionUtils.isAbstract;
import static org.junit.gen5.commons.util.ReflectionUtils.isStatic;

import java.util.function.Predicate;

import org.junit.gen5.commons.meta.API;

/**
 * Test if a class is a potential top-level JUnit 5 test container, even if
 * it does not contain tests.
 *
 * @since 5.0
 */
@API(Internal)
public class IsPotentialTestContainer implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (isAbstract(candidate))
			return false;
		if (candidate.isLocalClass())
			return false;
		return (isStatic(candidate) || !candidate.isMemberClass());
	}

}
