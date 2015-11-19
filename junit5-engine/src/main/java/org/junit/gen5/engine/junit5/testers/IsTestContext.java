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

import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.util.function.Predicate;

import org.junit.gen5.api.Context;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class IsTestContext implements Predicate<Class<?>> {

	@Override
	public boolean test(Class<?> contextClassCandidate) {
		//please do not collapse into single return
		if (isStatic(contextClassCandidate))
			return false;
		if (isPrivate(contextClassCandidate))
			return false;
		if (!contextClassCandidate.isMemberClass())
			return false;
		return isAnnotated(contextClassCandidate, Context.class);
	}

}
