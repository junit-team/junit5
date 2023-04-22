/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestMethodOrder(MethodOrderer.MethodName::class)
class ParameterizedTestNameFormatterIntegrationTests {

    @MethodSource("methodSource")
    @ParameterizedTest
    fun `implicit'Name`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            Assertions.assertEquals("[1] foo", info.displayName)
        } else {
            Assertions.assertEquals("[2] bar", info.displayName)
        }
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{0}")
    fun `zero'Only`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            Assertions.assertEquals("foo", info.displayName)
        } else {
            Assertions.assertEquals("bar", info.displayName)
        }
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{displayName}")
    fun `displayName'Only`(param: String, info: TestInfo) {
        Assertions.assertEquals("displayName'Only(String, TestInfo)", info.displayName)
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{displayName} - {0}")
    fun `displayName'Zero`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            Assertions.assertEquals("displayName'Zero(String, TestInfo) - foo", info.displayName)
        } else {
            Assertions.assertEquals("displayName'Zero(String, TestInfo) - bar", info.displayName)
        }
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{0} - {displayName}")
    fun `zero'DisplayName`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            Assertions.assertEquals("foo - zero'DisplayName(String, TestInfo)", info.displayName)
        } else {
            Assertions.assertEquals("bar - zero'DisplayName(String, TestInfo)", info.displayName)
        }
    }

    companion object {

        @JvmStatic
        private fun methodSource(): Array<String> =
            arrayOf(
                "foo",
                "bar"
            )
    }
}
