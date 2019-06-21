/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.jaeger;

import static java.util.stream.Collectors.joining;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class JaegerExecutionListener implements TestExecutionListener {

	private final ConcurrentMap<String, Span> spans = new ConcurrentHashMap<>();

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType(ConstSampler.TYPE).withParam(1);

		ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);

		Configuration config = new Configuration("test run").withSampler(samplerConfig).withReporter(reporterConfig);

		GlobalTracer.registerIfAbsent(config::getTracer);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		GlobalTracer.get().close();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		Tracer tracer = GlobalTracer.get();
		Tracer.SpanBuilder builder = tracer.buildSpan(testIdentifier.getDisplayName()).withTag("id",
			testIdentifier.getUniqueId());
		testIdentifier.getParentId().ifPresent(parent -> builder.asChildOf(spans.get(parent)));
		if (!testIdentifier.getTags().isEmpty()) {
			builder.withTag("tags", testIdentifier.getTags().stream().map(TestTag::getName).collect(joining(", ")));
		}
		Span span = builder.start();
		spans.put(testIdentifier.getUniqueId(), span);
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		Span span = spans.get(testIdentifier.getUniqueId());
		span.log(toMicros(entry.getTimestamp()), entry.getKeyValuePairs());
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		spans.remove(testIdentifier.getUniqueId()).finish();
	}

	private long toMicros(LocalDateTime timestamp) {
		return ChronoUnit.MICROS.between(Instant.EPOCH, timestamp.toInstant(OffsetDateTime.now().getOffset()));
	}

}
