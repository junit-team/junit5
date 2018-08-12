/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.junit.platform.surefire.provider;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.ConsoleStream;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.TestsToRun;
import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * @since 1.0
 * @deprecated Please use the provider that is included in Maven Surefire &ge; 2.22.0.
 */
@Deprecated
@API(status = DEPRECATED, since = "1.3")
public class JUnitPlatformProvider extends AbstractProvider {

	// Parameter names processed to determine which @Tags should be executed.
	static final String EXCLUDE_GROUPS = "excludedGroups";
	static final String EXCLUDE_TAGS = "excludeTags";
	static final String INCLUDE_GROUPS = "groups";
	static final String INCLUDE_TAGS = "includeTags";

	static final String CONFIGURATION_PARAMETERS = "configurationParameters";

	static final String EXCEPTION_MESSAGE_BOTH_NOT_ALLOWED = "The " + INCLUDE_GROUPS + " and " + INCLUDE_TAGS
			+ " parameters (or the " + EXCLUDE_GROUPS + " and " + EXCLUDE_TAGS + " parameters) are synonyms - "
			+ "only one of each is allowed (though neither is required).";

	private final ProviderParameters parameters;
	private final Launcher launcher;
	final Filter<?>[] filters;
	final Map<String, String> configurationParameters;

	public JUnitPlatformProvider(ProviderParameters parameters) {
		this(parameters, LauncherFactory.create());
	}

	JUnitPlatformProvider(ProviderParameters parameters, Launcher launcher) {
		this.parameters = parameters;
		this.launcher = launcher;
		this.filters = getFilters();
		this.configurationParameters = getConfigurationParameters();
		Logger.getLogger("org.junit").setLevel(Level.WARNING);
		printDeprecationWarning(parameters);
	}

	private void printDeprecationWarning(ProviderParameters parameters) {
		ConsoleStream consoleLogger = parameters.getConsoleLogger();
		// @formatter:off
		// Despite its name ConsoleStream.println() does not actually append a line separator
		consoleLogger.println(String.join(System.lineSeparator(),
				"",
				" +-------------------------------------------------------------------------------+",
				" | WARNING:                                                                      |",
				" | The junit-platform-surefire-provider has been deprecated and is scheduled to  |",
				" | be removed in JUnit Platform 1.4. Please use the built-in support in Maven    |",
				" | Surefire >= 2.22.0 instead.                                                   |",
				" | » https://junit.org/junit5/docs/current/user-guide/#running-tests-build-maven |",
				" +-------------------------------------------------------------------------------+",
				"",
				"")
		);
		// @formatter:on
	}

	@Override
	public Iterable<Class<?>> getSuites() {
		return scanClasspath();
	}

	@Override
	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		if (forkTestSet instanceof TestsToRun) {
			return invokeAllTests((TestsToRun) forkTestSet);
		}
		else if (forkTestSet instanceof Class) {
			return invokeAllTests(TestsToRun.fromClass((Class<?>) forkTestSet));
		}
		else if (forkTestSet == null) {
			return invokeAllTests(scanClasspath());
		}
		else {
			throw new IllegalArgumentException("Unexpected value of forkTestSet: " + forkTestSet);
		}
	}

	private TestsToRun scanClasspath() {
		TestPlanScannerFilter filter = new TestPlanScannerFilter(launcher, filters);
		ScanResult scanResult = parameters.getScanResult();
		TestsToRun scannedClasses = scanResult.applyFilter(filter, parameters.getTestClassLoader());
		return parameters.getRunOrderCalculator().orderTestClasses(scannedClasses);
	}

	private RunResult invokeAllTests(TestsToRun testsToRun) {
		RunResult runResult;
		ReporterFactory reporterFactory = parameters.getReporterFactory();
		try {
			RunListener runListener = reporterFactory.createReporter();
			ConsoleOutputCapture.startCapture((ConsoleOutputReceiver) runListener);
			LauncherDiscoveryRequest discoveryRequest = buildLauncherDiscoveryRequest(testsToRun);
			launcher.execute(discoveryRequest, new RunListenerAdapter(runListener));
		}
		finally {
			runResult = reporterFactory.close();
		}
		return runResult;
	}

	private LauncherDiscoveryRequest buildLauncherDiscoveryRequest(TestsToRun testsToRun) {
		// @formatter:off
		LauncherDiscoveryRequestBuilder builder = request()
				.filters(filters)
				.configurationParameters(configurationParameters);
		// @formatter:on
		for (Class<?> testClass : testsToRun) {
			builder.selectors(selectClass(testClass));
		}
		return builder.build();
	}

	private Filter<?>[] getFilters() {
		List<Filter<?>> filters = new ArrayList<>();

		Optional<List<String>> includes = getGroupsOrTags(getPropertiesList(INCLUDE_GROUPS),
			getPropertiesList(INCLUDE_TAGS));
		includes.map(TagFilter::includeTags).ifPresent(filters::add);

		Optional<List<String>> excludes = getGroupsOrTags(getPropertiesList(EXCLUDE_GROUPS),
			getPropertiesList(EXCLUDE_TAGS));
		excludes.map(TagFilter::excludeTags).ifPresent(filters::add);

		TestListResolver testListResolver = parameters.getTestRequest().getTestListResolver();
		if (!testListResolver.isEmpty()) {
			filters.add(new TestMethodFilter(testListResolver));
		}

		return filters.toArray(new Filter<?>[filters.size()]);
	}

	private Map<String, String> getConfigurationParameters() {
		String content = parameters.getProviderProperties().get(CONFIGURATION_PARAMETERS);
		if (content == null) {
			return emptyMap();
		}
		try (StringReader reader = new StringReader(content)) {
			Map<String, String> result = new HashMap<>();
			Properties props = new Properties();
			props.load(reader);
			props.stringPropertyNames().forEach(key -> result.put(key, props.getProperty(key)));
			return result;
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Error reading " + CONFIGURATION_PARAMETERS, ex);
		}
	}

	private Optional<List<String>> getPropertiesList(String key) {
		List<String> compoundProperties = null;
		String property = parameters.getProviderProperties().get(key);
		if (StringUtils.isNotBlank(property)) {
			// @formatter:off
			compoundProperties = Arrays.stream(property.split("[,]+"))
					.filter(StringUtils::isNotBlank)
					.map(String::trim)
					.collect(toList());
			// @formatter:on
		}
		return Optional.ofNullable(compoundProperties);
	}

	private Optional<List<String>> getGroupsOrTags(Optional<List<String>> groups, Optional<List<String>> tags) {
		Optional<List<String>> elements = Optional.empty();

		Preconditions.condition(!groups.isPresent() || !tags.isPresent(), EXCEPTION_MESSAGE_BOTH_NOT_ALLOWED);

		if (groups.isPresent()) {
			elements = groups;
		}
		else if (tags.isPresent()) {
			elements = tags;
		}

		return elements;
	}

}
