/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 1.0
 */
class LauncherConfigurationParameters implements ConfigurationParameters {

	private static final Logger logger = LoggerFactory.getLogger(LauncherConfigurationParameters.class);

	static Builder builder() {
		return new Builder();
	}

	private final List<ParameterProvider> providers;

	private LauncherConfigurationParameters(List<ParameterProvider> providers) {
		this.providers = providers;
	}

	@Override
	public Optional<String> get(String key) {
		return Optional.ofNullable(getProperty(key));
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return get(key).map(Boolean::parseBoolean);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int size() {
		return providers.stream() //
				.mapToInt(ParameterProvider::size) //
				.sum();
	}

	@Override
	public Set<String> keySet() {
		return providers.stream().map(ParameterProvider::keySet).flatMap(Collection::stream).collect(
			Collectors.toSet());
	}

	private String getProperty(String key) {
		Preconditions.notBlank(key, "key must not be null or blank");
		return providers.stream() //
				.map(parameterProvider -> parameterProvider.getValue(key)) //
				.filter(Objects::nonNull) //
				.findFirst() //
				.orElse(null);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("lookups", providers) //
				.toString();
	}

	static final class Builder {

		private final Map<String, String> explicitParameters = new HashMap<>();
		private boolean implicitProvidersEnabled = true;
		private String configFileName = ConfigurationParameters.CONFIG_FILE_NAME;
		private ConfigurationParameters parentConfigurationParameters;

		private Builder() {
		}

		Builder explicitParameters(Map<String, String> parameters) {
			Preconditions.notNull(parameters, "configuration parameters must not be null");
			explicitParameters.putAll(parameters);
			return this;
		}

		Builder enableImplicitProviders(boolean enabled) {
			this.implicitProvidersEnabled = enabled;
			return this;
		}

		Builder configFileName(String configFileName) {
			Preconditions.notBlank(configFileName, "configFileName must not be null or blank");
			this.configFileName = configFileName;
			return this;
		}

		Builder parentConfigurationParameters(ConfigurationParameters parameters) {
			Preconditions.notNull(parameters, "parent configuration parameters must not be null");
			this.parentConfigurationParameters = parameters;
			return this;
		}

		LauncherConfigurationParameters build() {
			List<ParameterProvider> parameterProviders = new ArrayList<>();
			if (!explicitParameters.isEmpty()) {
				parameterProviders.add(ParameterProvider.explicit(explicitParameters));
			}

			if (parentConfigurationParameters != null) {
				parameterProviders.add(ParameterProvider.inherited(parentConfigurationParameters));
			}

			if (implicitProvidersEnabled) {
				parameterProviders.add(ParameterProvider.systemProperties());
				parameterProviders.add(ParameterProvider.propertiesFile(configFileName));
			}
			return new LauncherConfigurationParameters(parameterProviders);
		}
	}

	private interface ParameterProvider {

		String getValue(String key);

		default int size() {
			return 0;
		}

		Set<String> keySet();

		static ParameterProvider explicit(Map<String, String> configParams) {
			return new ParameterProvider() {
				@Override
				public String getValue(String key) {
					return configParams.get(key);
				}

				@Override
				public int size() {
					return configParams.size();
				}

				@Override
				public Set<String> keySet() {
					return configParams.keySet();
				}

				@Override
				public String toString() {
					ToStringBuilder builder = new ToStringBuilder("explicit");
					configParams.forEach(builder::append);
					return builder.toString();
				}
			};
		}

		static ParameterProvider systemProperties() {
			return new ParameterProvider() {
				@Override
				public String getValue(String key) {
					try {
						return System.getProperty(key);
					}
					catch (Exception ignore) {
						return null;
					}
				}

				@Override
				public Set<String> keySet() {
					return System.getProperties().stringPropertyNames();
				}

				@Override
				public String toString() {
					return "systemProperties [...]";
				}
			};
		}

		static ParameterProvider propertiesFile(String configFileName) {
			Preconditions.notBlank(configFileName, "configFileName must not be null or blank");
			Properties properties = loadClasspathResource(configFileName.trim());
			return new ParameterProvider() {
				@Override
				public String getValue(String key) {
					return properties.getProperty(key);
				}

				@Override
				public Set<String> keySet() {
					return properties.stringPropertyNames();
				}

				@Override
				public String toString() {
					ToStringBuilder builder = new ToStringBuilder("propertiesFile");
					properties.stringPropertyNames().forEach(key -> builder.append(key, getValue(key)));
					return builder.toString();
				}
			};
		}

		static ParameterProvider inherited(ConfigurationParameters configParams) {
			return new ParameterProvider() {
				@Override
				public String getValue(String key) {
					return configParams.get(key).orElse(null);
				}

				@Override
				@SuppressWarnings("deprecation")
				public int size() {
					return configParams.size();
				}

				@Override
				public Set<String> keySet() {
					return configParams.keySet();
				}

				@Override
				public String toString() {
					ToStringBuilder builder = new ToStringBuilder("inherited");
					builder.append("parent", configParams);
					return builder.toString();
				}
			};
		}

	}

	private static Properties loadClasspathResource(String configFileName) {
		Properties props = new Properties();

		try {
			ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
			Set<URL> resources = new LinkedHashSet<>(Collections.list(classLoader.getResources(configFileName)));

			if (!resources.isEmpty()) {
				if (resources.size() > 1) {
					logger.warn(() -> String.format(
						"Discovered %d '%s' configuration files in the classpath; only the first will be used.",
						resources.size(), configFileName));
				}

				URL configFileUrl = resources.iterator().next(); // same as List#get(0)
				logger.config(() -> String.format(
					"Loading JUnit Platform configuration parameters from classpath resource [%s].", configFileUrl));
				URLConnection urlConnection = configFileUrl.openConnection();
				urlConnection.setUseCaches(false);
				try (InputStream inputStream = urlConnection.getInputStream()) {
					props.load(inputStream);
				}
			}
		}
		catch (Exception ex) {
			logger.warn(ex,
				() -> String.format(
					"Failed to load JUnit Platform configuration parameters from classpath resource [%s].",
					configFileName));
		}

		return props;
	}

}
