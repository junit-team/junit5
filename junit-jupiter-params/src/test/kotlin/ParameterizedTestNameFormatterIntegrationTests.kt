/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ParameterizedTestNameFormatterIntegrationTests {

    @ValueSource(strings = ["foo", "bar"])
    @ParameterizedTest
    fun `implicit'Name`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            assertEquals("[1] foo", info.displayName)
        } else {
            assertEquals("[2] bar", info.displayName)
        }
    }

    @ValueSource(strings = ["foo", "bar"])
    @ParameterizedTest(name = "{0}")
    fun `zero'Only`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            assertEquals("foo", info.displayName)
        } else {
            assertEquals("bar", info.displayName)
        }
    }

    @ValueSource(strings = ["foo", "bar"])
    @ParameterizedTest(name = "{displayName}")
    fun `displayName'Only`(param: String, info: TestInfo) {
        assertEquals("displayName'Only(String, TestInfo)", info.displayName)
    }

    @ValueSource(strings = ["foo", "bar"])
    @ParameterizedTest(name = "{displayName} - {0}")
    fun `displayName'Zero`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            assertEquals("displayName'Zero(String, TestInfo) - foo", info.displayName)
        } else {
            assertEquals("displayName'Zero(String, TestInfo) - bar", info.displayName)
        }
    }

    @ValueSource(strings = ["foo", "bar"])
    @ParameterizedTest(name = "{0} - {displayName}")
    fun `zero'DisplayName`(param: String, info: TestInfo) {
        if (param.equals("foo")) {
            assertEquals("foo - zero'DisplayName(String, TestInfo)", info.displayName)
        } else {
            assertEquals("bar - zero'DisplayName(String, TestInfo)", info.displayName)
        }
    }
}
