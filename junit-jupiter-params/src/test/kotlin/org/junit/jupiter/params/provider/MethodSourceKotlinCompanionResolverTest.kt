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

import java.util.Optional
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.JUnitException
import org.mockito.Mockito

class MethodSourceKotlinCompanionResolverTest {
    @Test
    fun classWithoutCompanionObject() {
        assertThatThrownBy {
            val context = extensionContext(WithoutCompanionTestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${WithoutCompanionTestCase::class.java.name}] or factory method [doesNotMatter] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("Companion object not found on ${WithoutCompanionTestCase::class.java}")
    }

    @Test
    fun classWithoutProviderMethod() {
        assertThatThrownBy {
            val context = extensionContext(WithoutProviderMethodTestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${WithoutProviderMethodTestCase::class.java.name}] or factory method [doesNotMatter] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("Could not find method [doesNotMatter] in companion object of class [${WithoutProviderMethodTestCase.Companion::class.java.name}]")
    }

    @Test
    fun notAKotlinClass() {
        assertThatThrownBy {
            val context = extensionContext(MethodArgumentsProviderTests.TestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${MethodArgumentsProviderTests.TestCase::class.java.name}] or factory method [doesNotMatter] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("class ${MethodArgumentsProviderTests.TestCase::class.java.name} is not a Kotlin class")
    }

    @Test
    fun notAKotlinClassFromDifferentCompanion() {
        assertThatThrownBy {
            val context = extensionContext(WithCompanionTestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "${MethodArgumentsProviderTests.TestCase::class.java.name}#doesNotMatter")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${WithCompanionTestCase::class.java.name}] or factory method [${MethodArgumentsProviderTests.TestCase::class.java.name}#doesNotMatter] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("class ${MethodArgumentsProviderTests.TestCase::class.java.name} is not a Kotlin class")
    }

    @Test
    fun companionMethodWithParameters() {
        assertThatThrownBy {
            val context = extensionContext(CompanionMethodWithParametersTestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "stringsProvider")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${CompanionMethodWithParametersTestCase::class.java.name}] or factory method [stringsProvider] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("factory method [stringsProvider] must not declare formal parameters")
    }

    @Test
    fun methodWithParametersFromDifferentCompanion() {
        assertThatThrownBy {
            val context = extensionContext(MethodArgumentsProviderTests.TestCase::class.java)
            MethodSourceKotlinCompanionResolver.resolveArguments(context, "${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider")
        }.isInstanceOf(JUnitException::class.java)
                .hasMessage("Class [${MethodArgumentsProviderTests.TestCase::class.java.name}] or factory method [${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider] are not Kotlin compatible")
                .hasRootCauseInstanceOf(JUnitException::class.java)
                .hasRootCauseMessage("factory method [${CompanionMethodWithParametersTestCase::class.java.name}#stringsProvider] must not declare formal parameters")
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
