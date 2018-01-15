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

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.apache.maven.surefire.util.ScannerFilter;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
final class TestPlanScannerFilter implements ScannerFilter {

	private final Launcher launcher;
	private final Filter<?>[] includeAndExcludeFilters;

	TestPlanScannerFilter(Launcher launcher, Filter<?>[] includeAndExcludeFilters) {
		this.launcher = launcher;
		this.includeAndExcludeFilters = includeAndExcludeFilters;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean accept(Class testClass) {
		// @formatter:off
		LauncherDiscoveryRequest discoveryRequest = request()
				.selectors(selectClass(testClass))
				.filters(includeAndExcludeFilters)
				.build();
		// @formatter:on
		TestPlan testPlan = launcher.discover(discoveryRequest);
		return testPlan.containsTests();
	}

}
