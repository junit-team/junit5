/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package example.util

fun <T> contextScope(block: ContextScope.() -> T): T = object : ContextScope {
    override fun <T> apply(function: Function<ContextScope, T>): T = run(function::apply)
}.run(block)
