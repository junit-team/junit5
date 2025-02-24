/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * @since 5.13
 */
@API(status = EXPERIMENTAL, since = "5.13")
public interface ParameterDeclarations {

	List<ParameterDeclaration> getAll();

	Optional<ParameterDeclaration> getFirst();

	Optional<ParameterDeclaration> get(int parameterIndex);

	AnnotatedElement getSourceElement();

	String getSourceElementDescription();

}
