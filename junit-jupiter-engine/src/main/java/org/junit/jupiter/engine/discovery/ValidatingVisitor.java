/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import java.util.function.UnaryOperator;

import org.junit.jupiter.engine.descriptor.Validatable;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

class ValidatingVisitor implements TestDescriptor.Visitor {

	private final DiscoveryIssueReporter issueReporter;

	ValidatingVisitor(DiscoveryIssueReporter issueReporter) {
		this.issueReporter = issueReporter;
	}

	@Override
	public void visit(TestDescriptor descriptor) {
		if (descriptor instanceof Validatable) {
			((Validatable) descriptor).validate(new Reporter(descriptor));
		}
	}

	private class Reporter implements Validatable.IssueReporter {

		private final TestDescriptor testDescriptor;

		private Reporter(TestDescriptor testDescriptor) {
			this.testDescriptor = testDescriptor;
		}

		@Override
		public void reportIssue(Severity severity, String message, UnaryOperator<DiscoveryIssue.Builder> issueBuilder) {
			DiscoveryIssue.Builder builder = DiscoveryIssue.builder(severity, message) //
					.source(testDescriptor.getSource());
			issueReporter.reportIssue(issueBuilder.apply(builder));
		}

	}

}
