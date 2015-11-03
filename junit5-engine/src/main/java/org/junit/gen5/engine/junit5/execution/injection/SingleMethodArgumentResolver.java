/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection;

import java.lang.reflect.*;

/**
 * Created by mmerdes on 03.11.15.
 */
public interface SingleMethodArgumentResolver {

	Object resolveArgumentForMethodParameter(Parameter parameter)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
