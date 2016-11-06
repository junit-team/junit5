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
import static java.util.Collections.singletonList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * @since 1.0
 */
@API(Internal)
public class CommandLineOptions {

	private boolean displayHelp;
	private boolean ansiColorOutputDisabled;
	private boolean hideDetails;

	private boolean scanClasspath;
	private List<String> arguments = emptyList();
	private List<URI> selectedUris = emptyList();
	private List<String> selectedFiles = emptyList();
	private List<String> selectedDirectories = emptyList();
	private List<String> selectedPackages = emptyList();
	private List<String> selectedClasses = emptyList();
	private List<String> selectedMethods = emptyList();
	private List<String> selectedClasspathResources = emptyList();

	private List<String> includedClassNamePatterns = singletonList(STANDARD_INCLUDE_PATTERN);
	private List<String> includedEngines = emptyList();
	private List<String> excludedEngines = emptyList();
	private List<String> includedTags = emptyList();
	private List<String> excludedTags = emptyList();

	private List<Path> additionalClasspathEntries = emptyList();

	private Path reportsDir;

	public boolean isDisplayHelp() {
		return this.displayHelp;
	}

	public void setDisplayHelp(boolean displayHelp) {
		this.displayHelp = displayHelp;
	}

	public boolean isAnsiColorOutputDisabled() {
		return this.ansiColorOutputDisabled;
	}

	public void setAnsiColorOutputDisabled(boolean ansiColorOutputDisabled) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
	}

	public boolean isScanClasspath() {
		return this.scanClasspath;
	}

	public void setScanClasspath(boolean scanClasspath) {
		this.scanClasspath = scanClasspath;
	}

	public boolean isHideDetails() {
		return this.hideDetails;
	}

	public void setHideDetails(boolean hideDetails) {
		this.hideDetails = hideDetails;
	}

	public List<URI> getSelectedUris() {
		return selectedUris;
	}

	public void setSelectedUris(List<URI> selectedUris) {
		this.selectedUris = selectedUris;
	}

	public List<String> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<String> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public List<String> getSelectedDirectories() {
		return selectedDirectories;
	}

	public void setSelectedDirectories(List<String> selectedDirectories) {
		this.selectedDirectories = selectedDirectories;
	}

	public List<String> getSelectedPackages() {
		return selectedPackages;
	}

	public void setSelectedPackages(List<String> selectedPackages) {
		this.selectedPackages = selectedPackages;
	}

	public List<String> getSelectedClasses() {
		return selectedClasses;
	}

	public void setSelectedClasses(List<String> selectedClasses) {
		this.selectedClasses = selectedClasses;
	}

	public List<String> getSelectedMethods() {
		return selectedMethods;
	}

	public void setSelectedMethods(List<String> selectedMethods) {
		this.selectedMethods = selectedMethods;
	}

	public List<String> getSelectedClasspathResources() {
		return selectedClasspathResources;
	}

	public void setSelectedClasspathResources(List<String> selectedClasspathResources) {
		this.selectedClasspathResources = selectedClasspathResources;
	}

	public List<String> getIncludedClassNamePatterns() {
		return includedClassNamePatterns;
	}

	public void setIncludedClassNamePatterns(List<String> includedClassNamePatterns) {
		this.includedClassNamePatterns = includedClassNamePatterns;
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

	public List<Path> getAdditionalClasspathEntries() {
		return this.additionalClasspathEntries;
	}

	public void setAdditionalClasspathEntries(List<Path> additionalClasspathEntries) {
		// Create a modifiable copy
		this.additionalClasspathEntries = new ArrayList<>(additionalClasspathEntries);
		this.additionalClasspathEntries.removeIf(path -> !Files.exists(path));
	}

	public Optional<Path> getReportsDir() {
		return Optional.ofNullable(this.reportsDir);
	}

	public void setReportsDir(Path reportsDir) {
		this.reportsDir = reportsDir;
	}

	public List<String> getArguments() {
		return this.arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
