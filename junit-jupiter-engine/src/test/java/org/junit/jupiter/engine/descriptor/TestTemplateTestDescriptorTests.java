/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

class TestTemplateTestDescriptorTests {

	@Test
	void inheritsTagsFromParent() throws Exception {
		UniqueId rootUniqueId = UniqueId.root("segment", "template");
		UniqueId parentUniqueId = rootUniqueId.append("class", "myClass");
		AbstractTestDescriptor parent = containerTestDescriptorWithTags(parentUniqueId,
			singleton(TestTag.create("foo")));

		TestTemplateTestDescriptor testDescriptor = new TestTemplateTestDescriptor(
			parentUniqueId.append("tmp", "testTemplate()"), MyTestCase.class,
			MyTestCase.class.getDeclaredMethod("testTemplate"));
		parent.addChild(testDescriptor);

		assertThat(testDescriptor.getTags()).containsExactlyInAnyOrder(TestTag.create("foo"), TestTag.create("bar"),
			TestTag.create("baz"));
	}

	private AbstractTestDescriptor containerTestDescriptorWithTags(UniqueId uniqueId, Set<TestTag> tags) {
		return new AbstractTestDescriptor(uniqueId, "testDescriptor with tags") {
			@Override
			public boolean isContainer() {
				return true;
			}

			@Override
			public boolean isTest() {
				return false;
			}

			@Override
			public Set<TestTag> getTags() {
				return tags;
			}
		};
	}

	static class MyTestCase {
		@Tag("bar")
		@Tag("baz")
		@TestTemplate
		void testTemplate() {
		}
	}

}
