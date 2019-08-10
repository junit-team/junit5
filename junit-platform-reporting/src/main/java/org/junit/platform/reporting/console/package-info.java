/**
 * Support for printing test results to text base streams. Useful
 * for producing decorated console-based output.
 *
 * Three listeners are provided for three different formats:
 * <ol>
 *   <li>{@link FlatPrintingListener}</li>
 *   <li>{@link TreePrintingListener}</li>
 *   <li>{@link VerboseTreePrintingListener}</li>
 * </ol>
 *
 * These listeners were in {@link org.junit.platform.console.tasks}
 * since 1.0 of {@code junit-platform-console}. They were moved here
 * and made public for general use in 1.6.
 *
 * @since 1.6
 */

package org.junit.platform.reporting.console;
