package org.junit.lambda.proposal02.examples;

import org.junit.lambda.proposal02.ContextDecorator;
import org.junit.lambda.proposal02.Database;
import org.junit.lambda.proposal02.Decorate;
import org.junit.lambda.proposal02.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Decorators replace JUnit4 rules.
 * We can decorate test classes (aka test contexts) - and maybe even individual tests
 */
@Decorate(DatabaseDecorator.class)
public class DecoratedTests {

    @Test
    public void initialDatabaseIsEmpty() {
        assertTrue(Database.instance().isEmpty());
    }

    @Test
    public void testWithInjectedDecorator(DatabaseDecorator decorator) {
        assertTrue(decorator.database.isEmpty());
    }

    @Test
    @Decorate(TemporaryFile.class)
    public void dumpDatabaseToFile(TemporaryFile file, DatabaseDecorator databaseDecorator) throws IOException {
        File dumpFile = file.createFile("db", "dump");
        databaseDecorator.database.dumpTo(dumpFile);
        assertFileContents("blabla", dumpFile);
    }

    private void assertFileContents(String expectedContents, File dumpFile) {
    }
}

class DatabaseDecorator implements ContextDecorator {

    Database database;

    @Override
    public void beforeAll() {
        database = Database.instance();
        database.startUp();
    }

    @Override
    public void before() {
        database.clean();
    }

    @Override
    public void afterAll() {
        database.shutDown();
    }
}

class TemporaryFile implements ContextDecorator {

    List<File>files = new ArrayList<File>();

    @Override
    public void after() throws IOException {
        for(File file : files)
            file.delete();
    }

    public File createFile(String pre, String post) throws IOException {
        File newFile = File.createTempFile(pre, post);
        files.add(newFile);
        return newFile;
    }
}