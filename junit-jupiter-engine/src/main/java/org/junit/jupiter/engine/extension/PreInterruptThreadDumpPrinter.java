/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.util.Map;

import org.junit.jupiter.api.extension.PreInterruptCallback;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.jupiter.engine.Constants;

/**
 * The default implementation for {@link PreInterruptCallback},
 * which will print the stacks of all {@link Thread}s to {@code System.out}.
 *
 * <p>Note: This is disabled by default, and must be enabled with
 * {@link Constants#EXTENSIONS_TIMEOUT_THREAD_DUMP_ENABLED_PROPERTY_NAME}
 *
 * @since 5.12
 */
final class PreInterruptThreadDumpPrinter implements PreInterruptCallback {
	private static final String NL = "\n";

	@Override
	public void beforeThreadInterrupt(PreInterruptContext preInterruptContext) {
		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		StringBuilder sb = new StringBuilder();
		sb.append("Thread ");
		appendThreadName(sb, preInterruptContext.getThreadToInterrupt());
		sb.append(" will be interrupted.");
		sb.append(NL);
		for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet()) {
			Thread thread = entry.getKey();
			StackTraceElement[] stack = entry.getValue();
			if (stack.length > 0) {
				sb.append(NL);
				appendThreadName(sb, thread);
				for (StackTraceElement stackTraceElement : stack) {
					sb.append(NL);
					//Do the same prefix as java.lang.Throwable.printStackTrace(java.lang.Throwable.PrintStreamOrWriter)
					sb.append("\tat ");
					sb.append(stackTraceElement.toString());

				}
				sb.append(NL);
			}
		}
		System.out.println(sb);
	}

	/**
	 * Appends the {@link Thread} name and ID in a similar fashion as {@code jstack}.
	 * @param sb the buffer
	 * @param th the thread to append
	 */
	private void appendThreadName(StringBuilder sb, Thread th) {
		sb.append("\"");
		sb.append(th.getName());
		sb.append("\"");
		sb.append(" #");
		sb.append(th.getId());
		if (th.isDaemon()) {
			sb.append(" daemon");
		}
	}
}
