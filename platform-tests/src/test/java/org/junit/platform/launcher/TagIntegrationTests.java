/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.launcher.TagIntegrationTests.TaggedTestCase.doubleTaggedWasExecuted;
import static org.junit.platform.launcher.TagIntegrationTests.TaggedTestCase.tag1WasExecuted;
import static org.junit.platform.launcher.TagIntegrationTests.TaggedTestCase.tag2WasExecuted;
import static org.junit.platform.launcher.TagIntegrationTests.TaggedTestCase.unTaggedWasExecuted;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

class TagIntegrationTests {

	@BeforeEach
	void init() {
		tag1WasExecuted = false;
		tag2WasExecuted = false;
		unTaggedWasExecuted = false;
		doubleTaggedWasExecuted = false;
	}

	@Test
	void includingWrongTagExecutesNothing() {
		LauncherDiscoveryRequest request = this.buildRequest(includeTags("whatever"), TaggedTestCase.class);

		this.execute(request);

		assertFalse(tag1WasExecuted);
		assertFalse(tag2WasExecuted);
		assertFalse(doubleTaggedWasExecuted);
		assertFalse(unTaggedWasExecuted);
	}

	@Test
	void includingSuitableTagExecutesTaggedTestOnly() {
		LauncherDiscoveryRequest request = this.buildRequest(includeTags("tag1"), TaggedTestCase.class);

		this.execute(request);

		assertTrue(tag1WasExecuted);
		assertFalse(tag2WasExecuted);
		assertTrue(doubleTaggedWasExecuted);
		assertFalse(unTaggedWasExecuted);
	}

	@Test
	void includingTheAnyKeywordExecutesAllTaggedTests() {
		LauncherDiscoveryRequest request = this.buildRequest(includeTags("any()"), TaggedTestCase.class);

		this.execute(request);

		assertTrue(tag1WasExecuted);
		assertTrue(tag2WasExecuted);
		assertTrue(doubleTaggedWasExecuted);
		assertFalse(unTaggedWasExecuted);
	}

	@Test
	void includingTheNoneKeywordExecutesAllUntaggedTests() {
		LauncherDiscoveryRequest request = this.buildRequest(includeTags("none()"), TaggedTestCase.class);

		this.execute(request);

		assertFalse(tag1WasExecuted);
		assertFalse(tag2WasExecuted);
		assertFalse(doubleTaggedWasExecuted);
		assertTrue(unTaggedWasExecuted);
	}

	private void execute(LauncherDiscoveryRequest request) {
		Launcher launcher = LauncherFactory.create();
		launcher.execute(request);
	}

	private LauncherDiscoveryRequest buildRequest(PostDiscoveryFilter filter, Class<TaggedTestCase> testClass) {
		LauncherDiscoveryRequestBuilder requestBuilder = request().selectors(selectClass(testClass)).filters(filter);
		return requestBuilder.build();
	}

	static class TaggedTestCase {

		static boolean tag1WasExecuted = false;
		static boolean tag2WasExecuted = false;
		static boolean unTaggedWasExecuted = false;
		static boolean doubleTaggedWasExecuted = false;

		@Test
		@Tag("tag1")
		void tagged1() {
			tag1WasExecuted = true;
		}

		@Test
		@Tag("tag2")
		void tagged2() {
			tag2WasExecuted = true;
		}

		@Test
		@Tag("tag1")
		@Tag("tag2")
		void doubleTagged() {
			doubleTaggedWasExecuted = true;
		}

		@Test
		void unTagged() {
			unTaggedWasExecuted = true;
		}

	}

}
