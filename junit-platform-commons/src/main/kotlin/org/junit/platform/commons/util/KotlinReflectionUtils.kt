/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:JvmName("KotlinReflectionUtilsKt")

package org.junit.platform.commons.util

import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

internal fun getReturnType(method: Method): Class<out Any> =
    with(method.kotlinFunction!!.returnType.jvmErasure) {
        if (this == Unit::class) {
            Void.TYPE
        } else {
            java
        }
    }

internal fun getGenericReturnType(method: Method) = method.kotlinFunction!!.returnType.javaType

internal fun getParameterTypes(method: Method) =
    method.kotlinFunction!!
        .parameters
        .map { it.type.jvmErasure.java }
        .toTypedArray()

internal fun getParameters(method: Method) =
    method.kotlinFunction!!.valueParameters.size.let {
        if (it > 0) {
            method.parameters.copyOf(it)
        } else {
            emptyArray()
        }
    }

internal fun invoke(
    method: Method,
    target: Any?,
    vararg args: Any?
) = runBlocking {
    try {
        method.kotlinFunction!!.callSuspend(target, *args)
    } catch (e: InvocationTargetException) {
        throw e.targetException
    }
}
