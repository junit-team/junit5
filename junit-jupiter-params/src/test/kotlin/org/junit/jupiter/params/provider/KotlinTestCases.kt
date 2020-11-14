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

class WithCompanionTestCase {
    companion object {
        fun stringsProvider(): Array<String> {
            return arrayOf("with-companion")
        }
    }
}

class JvmStaticCompanionTestCase {
    companion object {
        fun stringsProvider(): Array<String> {
            return arrayOf("jvm-static")
        }
    }
}

class WithoutCompanionTestCase

class WithoutProviderMethodTestCase {
    companion object
}

class CompanionMethodWithParametersTestCase {
    companion object {
        fun stringsProvider(value: Int): Array<String> {
            return arrayOf("with-parameters-$value")
        }
    }
}
