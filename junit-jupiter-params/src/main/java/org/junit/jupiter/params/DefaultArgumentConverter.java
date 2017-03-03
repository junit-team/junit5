/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import org.junit.jupiter.api.extension.ParameterContext;

class DefaultArgumentConverter implements ArgumentConverter {

	static final DefaultArgumentConverter INSTANCE = new DefaultArgumentConverter();

	@Override
	public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		Class<?> targetClass = context.getParameter().getType();
		if (targetClass.isInstance(source)) {
			return source;
		}
		if (source instanceof String && targetClass.equals(Integer.TYPE)) {
			return Integer.valueOf((String) source);
		}
		throw new ArgumentConversionException(
			"No implicit conversion to convert object to type " + targetClass.getName() + ": " + source);
	}
}
