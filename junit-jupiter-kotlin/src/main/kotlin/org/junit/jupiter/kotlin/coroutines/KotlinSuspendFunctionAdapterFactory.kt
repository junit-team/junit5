/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.kotlin.coroutines

import kotlinx.coroutines.runBlocking
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.INTERNAL
import org.junit.jupiter.engine.support.MethodAdapter
import org.junit.jupiter.engine.support.MethodAdapterFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

@API(status = INTERNAL, since = "6.0")
class KotlinSuspendFunctionAdapterFactory : MethodAdapterFactory {
    override fun adapt(method: Method): MethodAdapter? {
        val kotlinFunction = method.kotlinFunction
        if (kotlinFunction != null && kotlinFunction.isSuspend) {
            val parameters =
                kotlinFunction.valueParameters.size.let {
                    if (it > 0) {
                        method.parameters.copyOf(it)
                    } else {
                        emptyArray()
                    }
                }
            return object : MethodAdapter {
                override fun getMethod() = method

                override fun getName() = kotlinFunction.name

                override fun getReturnType() =
                    with(kotlinFunction.returnType.jvmErasure) {
                        if (this == Unit::class) {
                            Void.TYPE
                        } else {
                            java
                        }
                    }

                override fun getGenericReturnType() = kotlinFunction.returnType.javaType

                override fun getParameterTypes() = kotlinFunction.parameters.map { it.type.jvmErasure.java }.toTypedArray()

                override fun getParameters() = parameters

                override fun invoke(
                    target: Any?,
                    vararg args: Any?
                ) = runBlocking {
                    try {
                        kotlinFunction.callSuspend(target, *args)
                    } catch (e: InvocationTargetException) {
                        throw e.targetException
                    }
                }
            }
        }
        return null
    }
}
