/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package com.example.project;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.TestPlan;

public class SummaryPrintingListener extends SummaryGeneratingListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        super.testPlanExecutionFinished(testPlan);
        try (FileWriter writer = new FileWriter("build/test-summary.txt")) {
            getSummary().printTo(new PrintWriter(writer));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
