/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static java.util.Collections.emptyList;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * @since 5.0
 */
@API(Internal)
public class CommandLineOptions {

	private boolean displayHelp;
	private boolean exitCodeEnabled;
	private boolean ansiColorOutputDisabled;
	private boolean runAllTests;
	private boolean hideDetails;

	private String includeClassNamePattern;

	private List<String> includedEngines = emptyList();
	private List<String> excludedEngines = emptyList();

	private List<String> includedTags = emptyList();
	private List<String> excludedTags = emptyList();

	private List<String> additionalClasspathEntries = emptyList();

	private String xmlReportsDir;

	private List<String> arguments = emptyList();

	public boolean isDisplayHelp() {
		return this.displayHelp;
	}

	public void setDisplayHelp(boolean displayHelp) {
		this.displayHelp = displayHelp;
	}

	public boolean isExitCodeEnabled() {
		return this.exitCodeEnabled;
	}

	public void setExitCodeEnabled(boolean exitCodeEnabled) {
		this.exitCodeEnabled = exitCodeEnabled;
	}

	public boolean isAnsiColorOutputDisabled() {
		return this.ansiColorOutputDisabled;
	}

	public void setAnsiColorOutputDisabled(boolean ansiColorOutputDisabled) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
	}

	public boolean isRunAllTests() {
		return this.runAllTests;
	}

	public void setRunAllTests(boolean runAllTests) {
		this.runAllTests = runAllTests;
	}

	public boolean isHideDetails() {
		return this.hideDetails;
	}

	public void setHideDetails(boolean hideDetails) {
		this.hideDetails = hideDetails;
	}

	public Optional<String> getIncludeClassNamePattern() {
		return Optional.ofNullable(this.includeClassNamePattern);
	}

	public void setIncludeClassNamePattern(String includeClassNamePattern) {
		this.includeClassNamePattern = includeClassNamePattern;
	}

	public List<String> getIncludedEngines() {
		return this.includedEngines;
	}

	public void setIncludedEngines(List<String> includedEngines) {
		this.includedEngines = includedEngines;
	}

	public List<String> getExcludedEngines() {
		return this.excludedEngines;
	}

	public void setExcludedEngines(List<String> excludedEngines) {
		this.excludedEngines = excludedEngines;
	}

	public List<String> getIncludedTags() {
		return this.includedTags;
	}

	public void setIncludedTags(List<String> includedTags) {
		this.includedTags = includedTags;
	}

	public List<String> getExcludedTags() {
		return this.excludedTags;
	}

	public void setExcludedTags(List<String> excludedTags) {
		this.excludedTags = excludedTags;
	}

	public List<String> getAdditionalClasspathEntries() {
		return this.additionalClasspathEntries;
	}

	public void setAdditionalClasspathEntries(List<String> additionalClasspathEntries) {
		this.additionalClasspathEntries = additionalClasspathEntries;
	}

	public Optional<String> getXmlReportsDir() {
		return Optional.ofNullable(this.xmlReportsDir);
	}

	public void setXmlReportsDir(String xmlReportsDir) {
		this.xmlReportsDir = xmlReportsDir;
	}

	public List<String> getArguments() {
		return this.arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
