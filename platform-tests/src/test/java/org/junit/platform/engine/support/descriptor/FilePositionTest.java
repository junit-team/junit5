package org.junit.platform.engine.support.descriptor;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Created by derekstoner on 4/29/17.
 */
public class FilePositionTest {
    @org.junit.Test

    @Test
    public void equalsTest() {
        //test for a return of true when the equals method is passed itself
        FilePosition file = new FilePosition(0,0);
        assertTrue("File is same object", file.equals(file));
    }

    @Test
    public void equalsNullTest() {
        //test for a return of true when the equals method is passed itself
        FilePosition file = new FilePosition(0,0);
        assertFalse("File is not null", file.equals(null));
    }

}