/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.vintage.engine.descriptor.RunnerDecorator;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;

/**
 * Decorator for Runners that will be ignored completely.
 *
 * <p>Contrary to {@link org.junit.internal.builders.IgnoredClassRunner}, this
 * runner returns a complete description including all children.
 *
 * @since 5.1
 */
class IgnoringRunnerDecorator extends Runner implements RunnerDecorator {

	private static final Logger logger = LoggerFactory.getLogger(RunnerTestDescriptor.class);

	protected final Runner runner;
	private final Ignore testClassIgnoreAnnotation;

	IgnoringRunnerDecorator(Runner runner, Ignore ignoreAnnotation) {
		this.runner = Preconditions.notNull(runner, "Runner must not be null");
		this.testClassIgnoreAnnotation = Preconditions.notNull(ignoreAnnotation,
			"Test class @Ignore annotation must not be null");
	}

	@Override
	public Description getDescription() {
		Description originalDescription = runner.getDescription();

		if (runner instanceof JUnit38ClassRunner) {
			return junit38ClassRunnerDescriptionWithIgnoreAnnotation(originalDescription);
		}
		else if (originalDescription.getAnnotation(Ignore.class) == null) {
			warnAboutMissingIgnoreAnnotation(originalDescription);
		}

		return originalDescription;
	}

	@Override
	public void run(RunNotifier notifier) {
		notifier.fireTestIgnored(getDescription());
	}

	@Override
	public Runner getDecoratedRunner() {
		return runner;
	}

	/**
	 * {@link JUnit38ClassRunner} does not add class-level annotations to the runner description,
	 * which results in an inconsistent behavior when combined with the vintage engine: the runner description
	 * will be marked as started because the runner told so, but it will alos be reported as skipped by IgnoringRunnerDecorator
	 * which detected the @Ignore annotation on the test Java class.
	 */
	private Description junit38ClassRunnerDescriptionWithIgnoreAnnotation(Description runnerDescription) {
		List<Annotation> effectiveAnnotations = new ArrayList<>(runnerDescription.getAnnotations());
		effectiveAnnotations.add(testClassIgnoreAnnotation);

		Description updatedRunnerDescription = Description.createTestDescription(runnerDescription.getClassName(),
			runnerDescription.getMethodName(), effectiveAnnotations.toArray(new Annotation[0]));

		runnerDescription.getChildren().forEach(updatedRunnerDescription::addChild);
		return updatedRunnerDescription;
	}

	private void warnAboutMissingIgnoreAnnotation(Description originalDescription) {
		logger.warn(() -> "Custom test runner '" + runner.getClass().getName()
				+ "' did not add an @Ignore annotation to the runner description " + originalDescription);
	}
}
