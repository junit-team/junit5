/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.script;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Used to access named properties without exposing direct access to the
 * underlying source.
 *
 * @since 5.1
 */
@API(status = INTERNAL, since = "5.1")
@Deprecated
public interface ScriptAccessor {

	/**
	 * Get the value of the property with the supplied name.
	 *
	 * @param name the name of the property to look up
	 * @return the value assigned to the specified name; may be {@code null}
	 */
	String get(String name);

	class SystemPropertyAccessor implements ScriptAccessor {

		@Override
		public String get(String name) {
			return System.getProperty(name);
		}
	}

	class EnvironmentVariableAccessor implements ScriptAccessor {

		@Override
		public String get(String name) {
			return System.getenv(name);
		}
	}

	class ConfigurationParameterAccessor implements ScriptAccessor {

		private final ExtensionContext context;

		public ConfigurationParameterAccessor(ExtensionContext context) {
			this.context = context;
		}

		@Override
		public String get(String key) {
			return this.context.getConfigurationParameter(key).orElse(null);
		}
	}

}
