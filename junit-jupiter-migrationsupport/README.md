# Module junit-jupiter-migrationsupport

This module provides support for JUnit 4 rules within JUnit Jupiter.
Currently, this support is limited to subclasses of the ```org.junit.rules.Verifier```
and ```org.junit.rules.ExternalResource``` rules of JUnit 4, respectively.

Please note that a general support for arbitrary ```org.junit.rules.TestRule```
implementations is not possible within the JUnit Jupiter extension model.

The main purpose of this module is to facilitate the migration of large
JUnit 4 codebases containing such JUnit 4 rules by minimizing the effort
needed to run such legacy tests under JUnit 5.
By using one of the two provided class-level extensions on a test class
such rules in legacy code bases can be left unchanged
including the JUnit 4 rule import statements.

However, if you intend to develop a *new* extension for
JUnit 5 please use the new extension model of JUnit Jupiter instead
of the rule-based model of JUnit 4.
