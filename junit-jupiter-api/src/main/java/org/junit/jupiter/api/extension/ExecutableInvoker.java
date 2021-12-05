/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL, since = "5.9")
public interface ExecutableInvoker {

	Object invoke(Executable executable, Object target);

	<T> T invoke(Constructor<T> constructor, Object outerInstance);

}
