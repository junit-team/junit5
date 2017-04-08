/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isPublic;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;

import java.util.function.Predicate;

/**
 * @since 4.12
 */
class IsPotentialJUnit4TestClass implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> candidate) {
		// Do not collapse into single return.
		if (isAbstract(candidate))
			return false;
		if (!isPublic(candidate))
			return false;
		if (isNonStaticMemberClass(candidate))
			return false;

		return true;
	}

	private boolean isNonStaticMemberClass(Class<?> candidate) {
		return candidate.isMemberClass() && !isStatic(candidate);
	}

}
