/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:API(status = EXPERIMENTAL, since = "5.7")

package org.junit.jupiter.api

import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL

/**
 * Value that can be used with [@SinceKotlin][SinceKotlin] to hide symbols from
 * Kotlin users. There is a dedicated annotation ([@JvmSynthetic][JvmSynthetic])
 * to hide Kotlin symbols from Java, however, there is none for the opposite.
 * [KT-36439](https://youtrack.jetbrains.com/issue/KT-36439) was created to ask
 * for exactly that feature, until then this workaround is what leads to the
 * same desired result.
 *
 * Note that files using this workaround must add [@file:Suppress][Suppress]
 * containing [JAVA_ONLY_HACK] to disable the warning about symbols that require
 * a newer Kotlin version than the one the module is compiled against.
 */
internal const val JAVA_ONLY = "999999.999999"

/** @see JAVA_ONLY */
internal const val JAVA_ONLY_HACK = "NEWER_VERSION_IN_SINCE_KOTLIN"
