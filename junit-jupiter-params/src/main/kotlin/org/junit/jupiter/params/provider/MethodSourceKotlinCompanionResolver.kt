/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.params.provider

import org.apiguardian.api.API
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.function.Try
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.ReflectionSupport
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.commons.util.ReflectionUtils
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

@API(
        status = API.Status.INTERNAL,
        since = "5.9.0"
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
                else -> throw JUnitException("Class [${context.requiredTestClass.name}] or factory method [$factoryMethodName] are not compatible with Kotlin's companion object MethodSource", e)
            }
        }
        return factoryFunction.call(companionInstance)
    }

    private fun resolveCompanionMethod(context: ExtensionContext, factoryMethodName: String): CompanionMethod {
        return if (factoryMethodName.contains("#")) {
            val fullyQualifiedMethod = FullyQualifiedMethod.fromFactoryMethodName(factoryMethodName)
            val clazz = ReflectionUtils.tryToLoadClass(
                    fullyQualifiedMethod.className.removeSuffix(".Companion")
            ).get()!!
            resolveCompanionProvider(clazz, fullyQualifiedMethod.methodName)
        } else {
            val clazz = context.requiredTestClass!!
            val companionMethods = classHierarchy(clazz)
                    .map { item ->
                        Try.call { resolveCompanionProvider(item, factoryMethodName) }
                    }
            val companionMethod = companionMethods.filter { it.isSuccess }
                    .map { it.get() }
                    .firstOrNull()
            when {
                companionMethod != null -> companionMethod
                else -> {
                    val e = JUnitException("Unable to find Companion object with method [$factoryMethodName] in class hierarchy of [${clazz.name}]")
                    companionMethods.filter { it.isFailure }
                            .map { it.cause }
                            .forEach {
                                e.addSuppressed(it)
                            }
                    throw e
                }
            }
        }.also {
            Preconditions.condition(it.method.parameters.size == 1 &&
                    it.method.parameters[0].kind == KParameter.Kind.INSTANCE) {
                "factory method [$factoryMethodName] must not declare formal parameters"
            }
        }
    }

    private fun classHierarchy(clazz: Class<*>): List<Class<*>> {
        val list = mutableListOf<Class<*>>()
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            list += currentClass
            currentClass = currentClass.superclass
        }
        return list
    }

    private fun resolveCompanionProvider(clazz: Class<*>, factoryMethodName: String): CompanionMethod {
        if (!isKotlinClass(clazz)) throw JUnitException("Class [${clazz.name}] is not a Kotlin class")

        val companionObject = requiredCompanionInstance(clazz)
        val kFunction = requiredFactoryFunction(companionObject, factoryMethodName, clazz)
        return CompanionMethod(companionObject, kFunction)
    }

    private fun requiredFactoryFunction(companionObject: Any, methodName: String, clazz: Class<*>): KFunction<*> =
            companionObject::class.functions.firstOrNull { it.name == methodName }
                    ?: throw JUnitException("Could not find method [$methodName] in companion object of class [${clazz.name}]")

    private fun requiredCompanionInstance(clazz: Class<*>): Any {
        // KotlinReflectionNotSupportedError is thrown when no kotlin-reflect.jar is not on classpath
        clazz.kotlin.isCompanion
        // now we can try get for companion object instance
        return clazz.kotlin.companionObjectInstance
                ?: throw JUnitException("Companion object not found on [${clazz.name}]")
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

    private data class CompanionMethod(val instance: Any, val method: KFunction<*>)
}
