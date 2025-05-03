/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Provides the JUnit Jupiter {@link org.junit.platform.engine.TestEngine}
 * implementation.
 *
 * @since 5.0
 * @uses org.junit.jupiter.api.extension.Extension
 * @provides org.junit.platform.engine.TestEngine The {@code JupiterTestEngine}
 * runs Jupiter based tests on the platform.
 */
module org.junit.jupiter.kotlin {

	requires static org.apiguardian.api;

	requires org.junit.jupiter.engine;
	requires kotlin.stdlib;
	requires kotlin.reflect;
	requires kotlinx.coroutines.core;

	provides org.junit.jupiter.engine.support.MethodAdapterFactory
			with org.junit.jupiter.kotlin.KotlinSuspendFunctionAdapterFactory;
}
