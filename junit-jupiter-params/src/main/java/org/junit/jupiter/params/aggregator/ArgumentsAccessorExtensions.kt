/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.aggregator

/**
 * Extension Function for {@link ArgumentsAccessor}.
 *
 * @since 5.3
 * @receiver[ArgumentsAccessor]
 * @see ArgumentsAccessor.get(Int, Class<T!>!)
 */
inline fun <reified T: Any> ArgumentsAccessor.get(index: Int) : T =
        this.get(index, T::class.java)
