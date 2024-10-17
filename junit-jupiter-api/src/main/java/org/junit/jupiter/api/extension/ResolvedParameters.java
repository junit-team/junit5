/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import java.util.List;

/**
 * Get the parameters that have been resolved by some {@link ParameterResolver} for the current test.
 * <p>
 * We may add more convenience methods to this interface in the future.
 */
public interface ResolvedParameters {
	/**
	 * Get all the parameters that have been resolved by some {@link ParameterResolver} for the current test.
	 */
	List<?> getAllParameters();

}
