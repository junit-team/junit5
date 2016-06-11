/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

import static java.util.Collections.emptyList;
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;

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

	private String classnameFilter;

	private List<String> requiredEngines = emptyList();
	private List<String> excludedEngines = emptyList();

	private List<String> requiredTags = emptyList();
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

	public Optional<String> getClassnameFilter() {
		return Optional.ofNullable(this.classnameFilter);
	}

	public void setClassnameFilter(String classnameFilter) {
		this.classnameFilter = classnameFilter;
	}

	public List<String> getRequiredEngines() {
		return this.requiredEngines;
	}

	public void setRequiredEngines(List<String> requiredEngines) {
		this.requiredEngines = requiredEngines;
	}

	public List<String> getExcludedEngines() {
		return this.excludedEngines;
	}

	public void setExcludedEngines(List<String> excludedEngines) {
		this.excludedEngines = excludedEngines;
	}

	public List<String> getRequiredTags() {
		return this.requiredTags;
	}

	public void setRequiredTags(List<String> requiredTags) {
		this.requiredTags = requiredTags;
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
