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

import java.util.List;

/**
 * @author Matthias Merdes
 * @author Sam Brannen
 * @since 5.0
 */
public interface MethodArgumentResolverRegistry {

	void addResolvers(MethodArgumentResolver... resolver);

	void addResolvers(List<MethodArgumentResolver> resolvers);

	List<MethodArgumentResolver> getResolvers();

}
