/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;

/**
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class TestDiscoveryOptions {

	private boolean scanClasspath;
	private List<Path> additionalClasspathEntries = emptyList();
	private List<Path> selectedClasspathEntries = emptyList();

	private boolean scanModulepath;

	private List<ModuleSelector> selectedModules = emptyList();
	private List<UriSelector> selectedUris = emptyList();
	private List<FileSelector> selectedFiles = emptyList();
	private List<DirectorySelector> selectedDirectories = emptyList();
	private List<PackageSelector> selectedPackages = emptyList();
	private List<ClassSelector> selectedClasses = emptyList();
	private List<MethodSelector> selectedMethods = emptyList();
	private List<ClasspathResourceSelector> selectedClasspathResources = emptyList();
	private List<IterationSelector> selectedIterations = emptyList();

	private List<String> includedClassNamePatterns = singletonList(STANDARD_INCLUDE_PATTERN);
	private List<String> excludedClassNamePatterns = emptyList();
	private List<String> includedPackages = emptyList();
	private List<String> excludedPackages = emptyList();
	private List<String> includedEngines = emptyList();
	private List<String> excludedEngines = emptyList();
	private List<String> includedTagExpressions = emptyList();
	private List<String> excludedTagExpressions = emptyList();

	private Map<String, String> configurationParameters = emptyMap();

	public boolean isScanModulepath() {
		return this.scanModulepath;
	}

	public void setScanModulepath(boolean scanModulepath) {
		this.scanModulepath = scanModulepath;
	}

	public boolean isScanClasspath() {
		return this.scanClasspath;
	}

	public void setScanClasspath(boolean scanClasspath) {
		this.scanClasspath = scanClasspath;
	}

	public List<Path> getExistingAdditionalClasspathEntries() {
		return this.additionalClasspathEntries.stream().filter(Files::exists).collect(toList());
	}

	public List<Path> getAdditionalClasspathEntries() {
		return this.additionalClasspathEntries;
	}

	public void setAdditionalClasspathEntries(List<Path> additionalClasspathEntries) {
		this.additionalClasspathEntries = additionalClasspathEntries;
	}

	public List<Path> getSelectedClasspathEntries() {
		return this.selectedClasspathEntries;
	}

	public void setSelectedClasspathEntries(List<Path> selectedClasspathEntries) {
		this.selectedClasspathEntries = selectedClasspathEntries;
	}

	public List<UriSelector> getSelectedUris() {
		return selectedUris;
	}

	public void setSelectedUris(List<UriSelector> selectedUris) {
		this.selectedUris = selectedUris;
	}

	public List<FileSelector> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<FileSelector> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public List<DirectorySelector> getSelectedDirectories() {
		return selectedDirectories;
	}

	public void setSelectedDirectories(List<DirectorySelector> selectedDirectories) {
		this.selectedDirectories = selectedDirectories;
	}

	public List<ModuleSelector> getSelectedModules() {
		return selectedModules;
	}

	public void setSelectedModules(List<ModuleSelector> selectedModules) {
		this.selectedModules = selectedModules;
	}

	public List<PackageSelector> getSelectedPackages() {
		return selectedPackages;
	}

	public void setSelectedPackages(List<PackageSelector> selectedPackages) {
		this.selectedPackages = selectedPackages;
	}

	public List<ClassSelector> getSelectedClasses() {
		return selectedClasses;
	}

	public void setSelectedClasses(List<ClassSelector> selectedClasses) {
		this.selectedClasses = selectedClasses;
	}

	public List<MethodSelector> getSelectedMethods() {
		return selectedMethods;
	}

	public void setSelectedMethods(List<MethodSelector> selectedMethods) {
		this.selectedMethods = selectedMethods;
	}

	public List<ClasspathResourceSelector> getSelectedClasspathResources() {
		return selectedClasspathResources;
	}

	public void setSelectedClasspathResources(List<ClasspathResourceSelector> selectedClasspathResources) {
		this.selectedClasspathResources = selectedClasspathResources;
	}

	public List<IterationSelector> getSelectedIterations() {
		return selectedIterations;
	}

	public void setSelectedIterations(List<IterationSelector> selectedIterations) {
		this.selectedIterations = selectedIterations;
	}

	public List<DiscoverySelector> getExplicitSelectors() {
		List<DiscoverySelector> selectors = new ArrayList<>();
		selectors.addAll(getSelectedUris());
		selectors.addAll(getSelectedFiles());
		selectors.addAll(getSelectedDirectories());
		selectors.addAll(getSelectedModules());
		selectors.addAll(getSelectedPackages());
		selectors.addAll(getSelectedClasses());
		selectors.addAll(getSelectedMethods());
		selectors.addAll(getSelectedClasspathResources());
		selectors.addAll(getSelectedIterations());
		return selectors;
	}

	public List<String> getIncludedClassNamePatterns() {
		return this.includedClassNamePatterns;
	}

	public void setIncludedClassNamePatterns(List<String> includedClassNamePatterns) {
		this.includedClassNamePatterns = includedClassNamePatterns;
	}

	public List<String> getExcludedClassNamePatterns() {
		return this.excludedClassNamePatterns;
	}

	public void setExcludedClassNamePatterns(List<String> excludedClassNamePatterns) {
		this.excludedClassNamePatterns = excludedClassNamePatterns;
	}

	public List<String> getIncludedPackages() {
		return this.includedPackages;
	}

	public void setIncludedPackages(List<String> includedPackages) {
		this.includedPackages = includedPackages;
	}

	public List<String> getExcludedPackages() {
		return this.excludedPackages;
	}

	public void setExcludedPackages(List<String> excludedPackages) {
		this.excludedPackages = excludedPackages;
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

	public List<String> getIncludedTagExpressions() {
		return this.includedTagExpressions;
	}

	public void setIncludedTagExpressions(List<String> includedTags) {
		this.includedTagExpressions = includedTags;
	}

	public List<String> getExcludedTagExpressions() {
		return this.excludedTagExpressions;
	}

	public void setExcludedTagExpressions(List<String> excludedTags) {
		this.excludedTagExpressions = excludedTags;
	}

	public Map<String, String> getConfigurationParameters() {
		return this.configurationParameters;
	}

	public void setConfigurationParameters(Map<String, String> configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

}
