/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Is thrown to signal that the configuration of all extensions valid for a given test or container cannot be fullfilled
 * without violating some contraints.
 * <p>
 * Example: Two extensions declare a {@link org.junit.gen5.api.extension.TestExtension.DefaultOrder} of
 * {@link org.junit.gen5.api.extension.TestExtension.OrderPosition}.OUTERMOST.
 * </p>
 * <p>
 * The exception message must make the problem clear and suggest possible solutions.
 * </p>
 * 
 * @since 5.0.0
 */
public class ExtensionConfigurationException extends RuntimeException {

	public ExtensionConfigurationException(String message) {
		super(message);
	}
}
