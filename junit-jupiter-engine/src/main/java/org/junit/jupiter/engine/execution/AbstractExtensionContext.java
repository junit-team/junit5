/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * @since 5.0
 */
@API(Internal)
public abstract class AbstractExtensionContext<T extends TestDescriptor> implements ExtensionContext {

	private final ExtensionContext parent;
	private final EngineExecutionListener engineExecutionListener;
	private final T testDescriptor;
	private final ExtensionValuesStore valuesStore;

	private Object testInstance;

	protected AbstractExtensionContext(AbstractExtensionContext<?> parent,
			EngineExecutionListener engineExecutionListener, T testDescriptor) {
		this.parent = parent;
		this.engineExecutionListener = engineExecutionListener;
		this.testDescriptor = testDescriptor;
		this.valuesStore = createStore(parent);
	}

	private ExtensionValuesStore createStore(AbstractExtensionContext<?> parent) {
		ExtensionValuesStore parentStore = null;
		if (parent != null) {
			parentStore = parent.valuesStore;
		}
		return new ExtensionValuesStore(parentStore);
	}

	@Override
	public String getUniqueId() {
		return getTestDescriptor().getUniqueId().toString();
	}

	@Override
	public String getDisplayName() {
		return getTestDescriptor().getDisplayName();
	}

	public void setTestInstance(Object testInstance) {
		this.testInstance = testInstance;
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.ofNullable(this.testInstance);
	}

	@Override
	public void publishReportEntry(Map<String, String> values) {
		engineExecutionListener.reportingEntryPublished(this.testDescriptor, ReportEntry.from(values));
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	protected T getTestDescriptor() {
		return testDescriptor;
	}

	@Override
	public Store getStore(Namespace namespace) {
		Preconditions.notNull(namespace, "Namespace must not be null");
		return new NamespaceAwareStore(valuesStore, namespace);
	}

	@Override
	public Set<String> getTags() {
		return testDescriptor.getTags().stream().map(TestTag::getName).collect(toCollection(LinkedHashSet::new));
	}

}
