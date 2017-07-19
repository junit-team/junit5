/*
 * Copyright 2015-2017 the original author or authors.
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

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * @since 1.0
 */
@API(Experimental)
public class JUnitPlatformProvider extends AbstractProvider {

	// Parameter names processed to determine which @Tags should be executed.
	static final String EXCLUDE_GROUPS = "excludedGroups";
	static final String EXCLUDE_TAGS = "excludeTags";
	static final String INCLUDE_GROUPS = "groups";
	static final String INCLUDE_TAGS = "includeTags";

	static final String EXCEPTION_MESSAGE_BOTH_NOT_ALLOWED = "The " + INCLUDE_GROUPS + " and " + INCLUDE_TAGS
			+ " parameters (or the " + EXCLUDE_GROUPS + " and " + EXCLUDE_TAGS + " parameters) are synonyms - "
			+ "only one of each is allowed (though neither is required).";

	private final ProviderParameters parameters;
	private final Launcher launcher;
	final Filter<?>[] includeAndExcludeFilters;

	public JUnitPlatformProvider(ProviderParameters parameters) {
		this(parameters, LauncherFactory.create());
	}

	JUnitPlatformProvider(ProviderParameters parameters, Launcher launcher) {
		this.parameters = parameters;
		this.launcher = launcher;
		this.includeAndExcludeFilters = getIncludeAndExcludeFilters();
		Logger.getLogger("org.junit").setLevel(Level.WARNING);
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
		TestsToRun scannedClasses = parameters.getScanResult().applyFilter(
			new TestPlanScannerFilter(launcher, includeAndExcludeFilters), parameters.getTestClassLoader());
		return parameters.getRunOrderCalculator().orderTestClasses(scannedClasses);
	}

	private RunResult invokeAllTests(TestsToRun testsToRun) {
		RunResult runResult;
		ReporterFactory reporterFactory = parameters.getReporterFactory();
		try {
			RunListener runListener = reporterFactory.createReporter();
			launcher.registerTestExecutionListeners(new RunListenerAdapter(runListener));

			for (Class<?> testClass : testsToRun) {
				invokeSingleClass(testClass, runListener);
			}
		}
		finally {
			runResult = reporterFactory.close();
		}
		return runResult;
	}

	private void invokeSingleClass(Class<?> testClass, RunListener runListener) {
		SimpleReportEntry classEntry = new SimpleReportEntry(getClass().getName(), testClass.getName());
		runListener.testSetStarting(classEntry);

		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).filters(
			includeAndExcludeFilters).build();
		launcher.execute(discoveryRequest);

		runListener.testSetCompleted(classEntry);
	}

	private Filter<?>[] getIncludeAndExcludeFilters() {
		List<Filter<?>> filters = new ArrayList<>();

		Optional<List<String>> includes = getGroupsOrTags(getPropertiesList(INCLUDE_GROUPS),
			getPropertiesList(INCLUDE_TAGS));
		includes.map(TagFilter::includeTags).ifPresent(filters::add);

		Optional<List<String>> excludes = getGroupsOrTags(getPropertiesList(EXCLUDE_GROUPS),
			getPropertiesList(EXCLUDE_TAGS));
		excludes.map(TagFilter::excludeTags).ifPresent(filters::add);

		return filters.toArray(new Filter<?>[filters.size()]);
	}

	private Optional<List<String>> getPropertiesList(String key) {
		List<String> compoundProperties = null;
		String property = parameters.getProviderProperties().get(key);
		if (property != null) {
			compoundProperties = Arrays.asList(property.split("[, ]+"));
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
