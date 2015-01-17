package org.junit.lambda.proposal02;

import java.io.File;

public class Database {
    public static Database instance() {
        return new Database();
    }

    public boolean isEmpty() {
        return true;
    }

    public void startUp() {

    }

    public void shutDown() {

    }

    public void clean() {

    }

    public void dumpTo(File dumpFile) {

    }
}
