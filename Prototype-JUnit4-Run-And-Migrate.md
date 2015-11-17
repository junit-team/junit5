# Migrating from JUnit 4

Although JUnit 5 will not support many of JUnit 4 features, e.g. Rules and Runners,
we do not expect source code maintainers to migrate all of their existing tests, test extensions
and custom build test infrastructure to migrate to JUnit 5 in one go, or at all.

Instead JUnit 5 comes with a _JUnit 4 test engine_ that allows to run "old"
tests through the JUnit 5 test runners. Moreover, you can have JUnit 4 tests
side by side with JUnit 5 tests because all JUnit 5 classes and annotations are
in other packages below `org.junit.gen5`.

## Running JUnit 4 Tests with JUnit 5

Just make sure that the `junit4-engine` artifact is in your test runtime path.
In that case JUnit 4 tests will automatically be picked up by JUnit 5 test runners.

## Migrating existing tests to JUnit 5

Some of the things you have to watch out for when migrating existing tests:

- All annotations are now in `org.junit.gen5.api`.
- All assertions are now in `org.junit.gen5.api.Assertions`.
- `@Ignore` does no longer exist, it's called `@Disabled` now.
- `@Category` does no longer exist, use `@Tag` instead.
- `@RunWith` does no longer exist.
- `@Rule` does no longer exist.
