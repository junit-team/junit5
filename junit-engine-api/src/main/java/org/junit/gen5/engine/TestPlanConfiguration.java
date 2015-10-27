
package org.junit.gen5.engine;

import lombok.Value;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
@Value
public final class TestPlanConfiguration {
  private Map<String, String> launchParameters = new HashMap<>();
  private List<Class<?>> classes = new LinkedList<>();
  private List<String> classNames = new LinkedList<>();
  private List<Package> packages = new LinkedList<>();
  private List<String> packageNames = new LinkedList<>();
  private List<Path> paths = new LinkedList<>();
  private List<String> fileNames = new LinkedList<>();
  private List<String> uniqueIds = new LinkedList<>();
  private List<String> includePatterns = new LinkedList<>();
  private List<String> excludePatterns = new LinkedList<>();

  private TestPlanConfiguration() { /* no-op */ }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private TestPlanConfiguration configuration = new TestPlanConfiguration();

    private Builder() {
      /* no-op */
    }

    public Builder parameter(String key, String value) {
      configuration.launchParameters.put(key, value);
      return this;
    }

    public Builder parameters(Map<String, String> launchParameters) {
      configuration.launchParameters.putAll(launchParameters);
      return this;
    }

    public Builder classes(Class<?>... classes) {
      Arrays.stream(classes).forEach(testClass -> configuration.classes.add(testClass));
      return this;
    }

    public Builder classNames(String... classNames) {
      Arrays.stream(classNames).forEach(className -> configuration.classNames.add(className));
      return this;
    }

    public Builder packages(Package... packages) {
      Arrays.stream(packages).forEach(testPackage -> configuration.packages.add(testPackage));
      return this;
    }

    public Builder packageNames(String... packageNames) {
      Arrays.stream(packageNames).forEach(packageName -> configuration.packageNames.add(packageName));
      return this;
    }

    public Builder paths(Path... paths) {
      Arrays.stream(paths).forEach(path -> configuration.paths.add(path));
      return this;
    }

    public Builder fileNames(String... fileNames) {
      Arrays.stream(fileNames).forEach(n -> configuration.fileNames.add(n));
      return this;
    }

    public Builder uniqueIds(String... uniqueIds) {
      Arrays.stream(uniqueIds).forEach(uniqueId -> configuration.uniqueIds.add(uniqueId));
      return this;
    }

    public Builder includePatterns(String... patterns) {
      Arrays.stream(patterns).forEach(pattern -> configuration.includePatterns.add(pattern));
      return this;
    }

    public Builder excludePatterns(String... patterns) {
      Arrays.stream(patterns).forEach(pattern -> configuration.excludePatterns.add(pattern));
      return this;
    }

    public TestPlanConfiguration build() {
      return configuration;
    }
  }
}