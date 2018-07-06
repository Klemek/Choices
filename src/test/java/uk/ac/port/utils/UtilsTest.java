package uk.ac.port.utils;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.TestUtils;
import uk.ac.port.utils.Logger;

import java.io.File;

import static org.junit.Assert.*;

public class UtilsTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }

    @Test
    public void testStringToIntegerSuccess() {
        String test = "123456";
        assertEquals(Integer.valueOf(123456), Utils.stringToInteger(test));
    }

    @Test
    public void testStringToIntegerFail() {
        String test = "test";
        assertNull(Utils.stringToInteger(test));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(Utils.containsIgnoreCase("abcdef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcDef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcdef", "dEf"));
        assertTrue(Utils.containsIgnoreCase("abcdeF", "Def"));
        assertFalse(Utils.containsIgnoreCase("abcdef", "aef"));
    }

    @Test
    public void testCoalesceNull() {
        assertNull(Utils.coalesce());
        assertNull(Utils.coalesce((String) null));
        assertNull(Utils.coalesce((String) null, null));
    }

    @Test
    public void testCoalesceNotNull() {
        assertEquals("a", Utils.coalesce(null, "a"));
        assertEquals("a", Utils.coalesce(null, "a", "b"));
    }

    @Test
    public void testIsAlphaNumeric() {
        assertTrue(Utils.isAlphaNumeric("aBc"));
        assertTrue(Utils.isAlphaNumeric("123"));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z45bc"));
        assertFalse(Utils.isAlphaNumeric(" -;!:,%"));
        assertFalse(Utils.isAlphaNumeric("1B2a3Z4 5bc"));
        assertTrue(Utils.isAlphaNumeric(""));
        assertTrue(Utils.isAlphaNumeric(null));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z4 5bc", ' '));
        assertTrue(Utils.isAlphaNumeric(" -;!:,%", ' ', '-', ';', '!', ':', ',', '%'));
    }
}
