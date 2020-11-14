/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.provider

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import org.apiguardian.api.API
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.ReflectionSupport
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.commons.util.ReflectionUtils

@API(
    status = API.Status.INTERNAL,
    since = "5.8.0"
)
internal object MethodSourceKotlinCompanionResolver {
    /**
     * Check if it is possible to resolve provider method from Kotlin's Companion object.
     *
     * Companion object can be resolved if following conditions are met:
     * - Kotlin is on class path
     * - Class containing provider method is Kotlin class
     */
    fun canResolveArguments(context: ExtensionContext, factoryMethodName: String): Boolean =
            kotlinIsOnClasspath() &&
                    maybeKotlinCompanionFactoryMethodResolvable(context, factoryMethodName)

    /**
     * Resolves provider arguments from Companion object.
     *
     * Note that [canResolveArguments] have to be called before calling this method to
     * do few pre checks if we can try to resolve parameters.
     *
     * This method checks if there is `kotlin-reflect.jar` library on class path to support
     * working with Companion objects (and client is indicated by message to do so).
     */
    fun resolveArguments(context: ExtensionContext, factoryMethodName: String): Any? {
        if (!kotlinIsOnClasspath()) {
            throw JUnitException("Kotlin is not on classpath")
        }

        val (companionInstance, factoryFunction) = try {
            resolveCompanionMethod(context, factoryMethodName)
        } catch (e: Throwable) {
            when (e) {
                is KotlinReflectionNotSupportedError -> throw JUnitException("No kotlin-reflect.jar in the classpath", e)
                else -> throw JUnitException("Class [${context.requiredTestClass.name}] or factory method [$factoryMethodName] are not Kotlin compatible", e)
            }
        }
        return factoryFunction.call(companionInstance)
    }

    private fun resolveCompanionMethod(context: ExtensionContext, factoryMethodName: String): Pair<Any, KFunction<*>> {
        return if (factoryMethodName.contains("#")) {
            val fullyQualifiedMethod = FullyQualifiedMethod.fromFactoryMethodName(factoryMethodName)
            val clazz = ReflectionUtils.tryToLoadClass(fullyQualifiedMethod.className).get()!!
            resolveCompanionProvider(clazz, fullyQualifiedMethod.methodName)
        } else {
            val clazz = context.requiredTestClass
            resolveCompanionProvider(clazz, factoryMethodName)
        }.also {
            Preconditions.condition(it.second.parameters.size == 1 &&
                    it.second.parameters[0].kind == KParameter.Kind.INSTANCE) {
                "factory method [$factoryMethodName] must not declare formal parameters"
            }
        }
    }

    private fun resolveCompanionProvider(clazz: Class<*>, factoryMethodName: String): Pair<Any, KFunction<*>> {
        if (!isKotlinClass(clazz)) throw JUnitException("$clazz is not a Kotlin class")
        val companionObject = requiredCompanionInstance(clazz)
        val kFunction = requiredFactoryFunction(companionObject, factoryMethodName)
        return companionObject to kFunction
    }

    private fun requiredFactoryFunction(companionObject: Any, methodName: String): KFunction<*> =
            companionObject::class.functions.firstOrNull { it.name == methodName }
                    ?: throw JUnitException("Could not find method [$methodName] in companion object of class [${companionObject::class.java.name}]")

    private fun requiredCompanionInstance(clazz: Class<*>): Any {
        // KotlinReflectionNotSupportedError is thrown when no kotlin-reflect.jar is not on classpath
        clazz.kotlin.isCompanion
        // now we can try get for companion object instance
        return clazz.kotlin.companionObjectInstance
                ?: throw JUnitException("Companion object not found on ${clazz.kotlin}")
    }

    private fun maybeKotlinCompanionFactoryMethodResolvable(
        context: ExtensionContext,
        factoryMethodName: String
    ): Boolean =
            try {
                resolveCompanionMethod(context, factoryMethodName)
                true
            } catch (e: Throwable) {
                e is KotlinReflectionNotSupportedError
            }

    private fun kotlinIsOnClasspath(): Boolean = kotlinMetadataTry.isSuccess

    private fun isKotlinClass(javaClass: Class<*>): Boolean =
            AnnotationSupport.findAnnotation(javaClass, Metadata::class.java).isPresent

    private val kotlinMetadataTry = ReflectionSupport.tryToLoadClass("kotlin.Metadata")!!

    private data class FullyQualifiedMethod(
        val className: String,
        val methodName: String,
        val methodParameters: String
    ) {
        companion object {
            fun fromFactoryMethodName(factoryMethodName: String): FullyQualifiedMethod {
                val methodParts = ReflectionUtils.parseFullyQualifiedMethodName(factoryMethodName)!!
                return FullyQualifiedMethod(
                    methodParts[0]!!,
                    methodParts[1]!!,
                    methodParts[2]!!
                )
            }
        }
    }
}
