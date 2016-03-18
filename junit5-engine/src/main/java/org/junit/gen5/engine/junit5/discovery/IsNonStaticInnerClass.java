/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.util.function.Predicate;

import org.junit.gen5.api.Nested;
import org.junit.gen5.commons.meta.API;

/**
 * Test if a class is a non-static inner class.
 *
 * @since 5.0
 */
@API(Internal)
public class IsNonStaticInnerClass implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (isStatic(candidate))
			return false;
		if (isPrivate(candidate))
			return false;
		return candidate.isMemberClass();
	}

}
