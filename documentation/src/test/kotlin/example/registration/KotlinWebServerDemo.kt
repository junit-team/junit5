/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package example.registration

import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.RegisterExtension

// tag::user_guide[]
class KotlinWebServerDemo {

    companion object {
        @JvmField
        @RegisterExtension
        val server = WebServerExtension.builder()
                .enableSecurity(false)
                .build()
    }

    @Test
    fun getProductList() {
        // Use WebClient to connect to web server using serverUrl and verify response
        val webClient = WebClient()
        val serverUrl = server.serverUrl
        assertEquals(200, webClient.get("$serverUrl/products").responseStatus)
    }
}
// end::user_guide[]

class WebServerExtension : BeforeAllCallback {

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    val serverUrl = "http://example.org:8181"

    override fun beforeAll(context: ExtensionContext) {
        // no-op for demo
    }

    class Builder {
        fun enableSecurity(@Suppress("UNUSED_PARAMETER") securityEnabled: Boolean) = this
        fun build(): WebServerExtension = WebServerExtension()
    }
}

class WebClient {
    fun get(@Suppress("UNUSED_PARAMETER") path: String): WebResponse = WebResponse()
}

class WebResponse {
    val responseStatus = 200
}
