/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.vintage.engine.VintageUniqueIdBuilder;
import org.junit.vintage.engine.samples.junit3.AbstractJUnit3TestCase;
import org.junit.vintage.engine.samples.junit4.AbstractJunit4TestCaseWithConstructorParameter;

/**
 * Tests for {@link VintageDiscoverer}.
 *
 * @since 4.12
 */
@TrackLogRecords
class VintageDiscovererTests {

	@Test
	void classNameFilterExcludesClass() {
		// @formatter:off
		EngineDiscoveryRequest request = request()
				.selectors(selectClass(Foo.class), selectClass(Bar.class))
				.filters(ClassNameFilter.includeClassNamePatterns(".*Foo"))
				.build();
		// @formatter:on

		TestDescriptor testDescriptor = discover(request);

		assertThat(testDescriptor.getChildren()).hasSize(1);
		assertThat(getOnlyElement(testDescriptor.getChildren()).getUniqueId().toString()).contains(Foo.class.getName());
	}

	@Test
	void packageNameFilterExcludesClasses() {
		// @formatter:off
		EngineDiscoveryRequest request = request()
				.selectors(selectClass(Foo.class), selectClass(Bar.class))
				.filters(PackageNameFilter.excludePackageNames("org.junit.vintage.engine.discovery"))
				.build();
		// @formatter:on

		TestDescriptor testDescriptor = discover(request);

		assertThat(testDescriptor.getChildren()).isEmpty();
	}

	@Test
	void doesNotResolveAbstractJUnit3Classes(LogRecordListener listener) {
		doesNotResolve(listener, selectClass(AbstractJUnit3TestCase.class));
	}

	@Test
	void doesNotResolveAbstractJUnit4Classes(LogRecordListener listener) {
		doesNotResolve(listener, selectClass(AbstractJunit4TestCaseWithConstructorParameter.class));
	}

	@Test
	void logsWarningOnUnloadableTestClass(LogRecordListener listener) {
		UniqueId uniqueId = VintageUniqueIdBuilder.uniqueIdForClass("foo.bar.UnknownClass");

		doesNotResolve(listener, selectUniqueId(uniqueId));

		LogRecord logRecord = listener.stream(EngineDiscoveryRequestResolver.class, Level.WARNING).findFirst().get();
		assertThat(logRecord.getMessage()).isEqualTo(
			"UniqueIdSelector [uniqueId = " + uniqueId + "] could not be resolved.");
		assertThat(logRecord.getThrown()).hasCauseInstanceOf(ClassNotFoundException.class);
	}

	@Test
	void ignoresUniqueIdsOfOtherEngines(LogRecordListener listener) {
		doesNotResolve(listener, selectUniqueId(UniqueId.forEngine("someEngine")));
	}

	private void doesNotResolve(LogRecordListener listener, DiscoverySelector selector) {
		LauncherDiscoveryRequest request = request().selectors(selector).build();

		TestDescriptor testDescriptor = discover(request);

		assertThat(testDescriptor.getChildren()).isEmpty();
	}

	private TestDescriptor discover(EngineDiscoveryRequest request) {
		return new VintageDiscoverer().discover(request, engineId());
	}

	public static class Foo {

		@org.junit.Test
		public void test() {
		}

	}

	public static class Bar {

		@org.junit.Test
		public void test() {
		}

	}

}
