/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import java.util.function.Predicate;
import org.junit.gen5.api.Context;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class IsTestContext extends ReflectionObjectTester implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> contextClassCandidate) {
		if (isPrivate(contextClassCandidate))
			return false;
		if (!contextClassCandidate.isMemberClass())
			return false;
		if (isStatic(contextClassCandidate))
			return false;
		return hasAnnotation(contextClassCandidate, Context.class);
	}
}
