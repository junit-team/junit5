/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.console.tasks.XmlReportAssertions.ensureValidAccordingToJenkinsSchema;

import java.io.StringWriter;
import java.time.Clock;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.launcher.TestPlan;

class XmlReportWriterTests {

	@Test
	void writesFileWithoutTestcaseElementsWithoutAnyTests() throws Exception {
		TestPlan testPlan = TestPlan.from(singleton(new EngineDescriptor("emptyEngine", "Empty Engine")));
		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());

		StringWriter out = new StringWriter();
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);

		String content = ensureValidAccordingToJenkinsSchema(out.toString());
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"Empty Engine\" tests=\"0\"",
				"</testsuite>")
			.doesNotContain("<testcase");
		//@formatter:on
	}

}
