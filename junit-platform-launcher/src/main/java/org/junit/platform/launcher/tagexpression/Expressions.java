/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import java.util.Collection;

import org.junit.platform.engine.TestTag;

class Expressions {

	static Expression tag(String tag) {
		TestTag testTag = TestTag.create(tag);
		return new Expression() {
			@Override
			public boolean evaluate(Collection<TestTag> tags) {
				return tags.contains(testTag);
			}

			@Override
			public String toString() {
				return testTag.getName();
			}
		};
	}

	static Expression not(Expression toNegate) {
		return new Expression() {
			@Override
			public boolean evaluate(Collection<TestTag> tags) {
				return !toNegate.evaluate(tags);
			}

			@Override
			public String toString() {
				return "!" + toNegate + "";
			}
		};
	}

	static Expression and(Expression lhs, Expression rhs) {
		return new Expression() {
			@Override
			public boolean evaluate(Collection<TestTag> tags) {
				return lhs.evaluate(tags) && rhs.evaluate(tags);
			}

			@Override
			public String toString() {
				return "(" + lhs + " & " + rhs + ")";
			}
		};
	}

	static Expression or(Expression lhs, Expression rhs) {
		return new Expression() {
			@Override
			public boolean evaluate(Collection<TestTag> tags) {
				return lhs.evaluate(tags) || rhs.evaluate(tags);
			}

			@Override
			public String toString() {
				return "(" + lhs + " | " + rhs + ")";
			}
		};
	}
}
