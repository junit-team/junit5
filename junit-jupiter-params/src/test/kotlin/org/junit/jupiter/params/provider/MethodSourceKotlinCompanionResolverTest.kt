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

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.JUnitException
import org.mockito.Mockito
import java.util.*

class MethodSourceKotlinCompanionResolverTest {
    @Test
    fun classWithoutCompanionObject() {
        val clazz = WithoutCompanionTestCase::class.java
        assertThatThrownBy {
            val context = extensionContext(clazz)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${clazz.name}] or factory method [doesNotMatter] are not compatible with Kotlin's companion object MethodSource")
                .getRootCause()
                .hasMessage("Unable to find Companion object with method [doesNotMatter] in class hierarchy of [${clazz.name}]")
                .hasSuppressedException(JUnitException("Companion object not found on [${clazz.name}]"))
                .hasSuppressedException(JUnitException("Class [${Object::class.java.name}] is not a Kotlin class"))
    }

    @Test
    fun classWithoutProviderMethod() {
        val clazz = WithoutProviderMethodTestCase::class.java
        assertThatThrownBy {
            val context = extensionContext(clazz)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "thisShouldNotBeFound")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${clazz.name}] or factory method [thisShouldNotBeFound] are not compatible with Kotlin's companion object MethodSource")
                .getRootCause()
                .hasMessage("Unable to find Companion object with method [thisShouldNotBeFound] in class hierarchy of [${clazz.name}]")
                .hasSuppressedException(JUnitException("Could not find method [thisShouldNotBeFound] in companion object of class [${clazz.name}]"))
                .hasSuppressedException(JUnitException("Class [${Object::class.java.name}] is not a Kotlin class"))
    }

    @Test
    fun notAKotlinClass() {
        val clazz = MethodArgumentsProviderTests.TestCase::class.java
        assertThatThrownBy {
            val context = extensionContext(clazz)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${clazz.name}] or factory method [doesNotMatter] are not compatible with Kotlin's companion object MethodSource")
                .getRootCause()
                .hasMessage("Unable to find Companion object with method [doesNotMatter] in class hierarchy of [${clazz.name}]")
                .hasSuppressedException(JUnitException("Class [${clazz.name}] is not a Kotlin class"))
                .hasSuppressedException(JUnitException("Class [${Object::class.java.name}] is not a Kotlin class"))
    }

    @Test
    fun notAKotlinClassFromDifferentCompanion() {
        val clazz = WithCompanionTestCase::class.java
        val clazzSecond = MethodArgumentsProviderTests.TestCase::class.java
        assertThatThrownBy {
            val context = extensionContext(clazz)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "${clazzSecond.name}#doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${clazz.name}] or factory method [${clazzSecond.name}#doesNotMatter] are not compatible with Kotlin's companion object MethodSource")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("Class [${clazzSecond.name}] is not a Kotlin class")
    }

    @Test
    fun companionMethodWithParameters() {
        assertThatThrownBy {
            val context = extensionContext(CompanionMethodWithParametersTestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "stringsProvider")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${CompanionMethodWithParametersTestCase::class.java.name}] or factory method [stringsProvider] are not compatible with Kotlin's companion object MethodSource")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("factory method [stringsProvider] must not declare formal parameters")
    }

    @Test
    fun methodWithParametersFromDifferentCompanion() {
        assertThatThrownBy {
            val context = extensionContext(MethodArgumentsProviderTests.TestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${MethodArgumentsProviderTests.TestCase::class.java.name}] or factory method [${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider] are not compatible with Kotlin's companion object MethodSource")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("factory method [${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider] must not declare formal parameters")
    }

    @Test
    fun invalidClassNameFromDifferentCompanion() {
        val clazz = WithCompanionTestCase::class.java
        val companionMethod = "i.am.aclass.ThatDoesNotExists#doesNotMatter"
        assertThatThrownBy {
            val context = extensionContext(clazz)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, companionMethod)
        }.isInstanceOf(ClassNotFoundException::class.java)
                .hasMessage("i.am.aclass.ThatDoesNotExists")
    }

    companion object {
        private fun extensionContext(testClass: Class<*>): ExtensionContext {
            val extensionContext = Mockito.mock(ExtensionContext::class.java)

            Mockito.`when`(extensionContext.testClass).thenReturn(Optional.ofNullable(testClass))
            Mockito.`when`(extensionContext.testMethod).thenReturn(Optional.empty())

            Mockito.doCallRealMethod().`when`(extensionContext).requiredTestMethod
            Mockito.doCallRealMethod().`when`(extensionContext).requiredTestClass

            Mockito.`when`(extensionContext.testInstance).thenReturn(Optional.empty())
            return extensionContext
        }
    }
}
