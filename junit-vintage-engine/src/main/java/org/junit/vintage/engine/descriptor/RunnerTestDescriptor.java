/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static java.util.Collections.singletonList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.HashSet;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

/**
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public class RunnerTestDescriptor extends VintageTestDescriptor {

	private static final Logger logger = LoggerFactory.getLogger(RunnerTestDescriptor.class);

	private final Runner runner;
	private final Set<Description> rejectedExclusions = new HashSet<>();
	private boolean wasFiltered;

	public RunnerTestDescriptor(UniqueId uniqueId, Class<?> testClass, Runner runner) {
		super(uniqueId, runner.getDescription(), testClass.getSimpleName(), ClassSource.from(testClass));
		this.runner = runner;
	}

	public Request toRequest() {
		return new RunnerRequest(this.runner);
	}

	@Override
	protected boolean tryToExcludeFromRunner(Description description) {
		boolean excluded = tryToFilterRunner(description);
		if (excluded) {
			wasFiltered = true;
		}
		else {
			rejectedExclusions.add(description);
		}
		return excluded;
	}

	private boolean tryToFilterRunner(Description description) {
		if (runner instanceof Filterable) {
			ExcludeDescriptionFilter filter = new ExcludeDescriptionFilter(description);
			try {
				((Filterable) runner).filter(filter);
			}
			catch (NoTestsRemainException ignore) {
				// it's safe to ignore this exception because childless TestDescriptors will get pruned
			}
			return filter.wasSuccessful();
		}
		return false;
	}

	@Override
	protected boolean canBeRemovedFromHierarchy() {
		return true;
	}

	@Override
	public void prune() {
		if (wasFiltered) {
			// filtering the runner may render intermediate Descriptions obsolete
			// (e.g. test classes without any remaining children in a suite)
			pruneDescriptorsForObsoleteDescriptions(singletonList(runner.getDescription()));
		}
		if (rejectedExclusions.isEmpty()) {
			super.prune();
		}
		else if (rejectedExclusions.containsAll(getDescription().getChildren())) {
			// since the Runner was asked to remove all of its direct children,
			// it's safe to remove it entirely
			removeFromHierarchy();
		}
		else {
			logIncompleteFiltering();
		}
	}

	private void logIncompleteFiltering() {
		if (runner instanceof Filterable) {
			logger.warn(() -> "Runner " + runner.getClass().getName() //
					+ " (used on " + getDescription().getTestClass()
					+ ") was not able to satisfy all filter requests.");
		}
		else {
			logger.warn(() -> "Runner " + runner.getClass().getName() //
					+ " (used on " + getDescription().getTestClass() + ") does not support filtering" //
					+ " and will therefore be run completely.");
		}
	}

	private static class ExcludeDescriptionFilter extends Filter {

		private final Description description;
		private boolean successful;

		ExcludeDescriptionFilter(Description description) {
			this.description = description;
		}

		@Override
		public boolean shouldRun(Description description) {
			if (this.description.equals(description)) {
				successful = true;
				return false;
			}
			return true;
		}

		@Override
		public String describe() {
			return "exclude " + description;
		}

		boolean wasSuccessful() {
			return successful;
		}
	}
}
