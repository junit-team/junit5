package org.junit.lambda.proposal02.examples;

import junit.framework.TestResult;
import org.junit.lambda.proposal02.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Decorators replace JUnit4 rules.
 * We can decorate test classes (aka test contexts) - and even individual tests.
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

class DatabaseDecorator implements TestContextDecorator {

    Database database;

    @Override
    public TestResult run(TestComponent testComponent) throws Exception {
        //Leaving out try-catch for clarity of example
        database = Database.instance();
        database.startUp();
        TestResult result = TestContextDecorator.super.run(testComponent);
        database.shutDown();
        return result;
    }

    @Override
    public TestResult runChild(TestComponent testComponent) throws Exception {
        database.clean();
        return TestContextDecorator.super.runChild(testComponent);
    }

}

class TemporaryFile implements TestComponentDecorator {

    List<File>files = new ArrayList<File>();

    @Override
    public TestResult run(TestComponent testComponent) throws Exception {
        //Leaving out try-catch for clarity of example
        TestResult result = TestComponentDecorator.super.run(testComponent);
        for(File file : files)
            file.delete();
        return result;
    }

    public File createFile(String pre, String post) throws IOException {
        File newFile = File.createTempFile(pre, post);
        files.add(newFile);
        return newFile;
    }
}