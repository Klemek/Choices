package uk.ac.port.choices.dao;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LangDaoTest {

    @Test
    public void createStringAndList() {
        Map<String, String> values = LangDao.listStrings();
        assertTrue(values.isEmpty());

        LangDao.updateString(TestUtils.langEntry.getKey(), TestUtils.langEntry.getValue());

        values = LangDao.listStrings();
        assertEquals(1, values.size());
        assertEquals(TestUtils.langEntry.getValue(), values.get(TestUtils.langEntry.getKey()));

        LangDao.deleteString(TestUtils.langEntry.getKey());

        values = LangDao.listStrings();
        assertTrue(values.isEmpty());
    }

    @Test
    public void updateStringAndDelete() {

        Map<String, String> values = LangDao.listStrings();
        assertTrue(values.isEmpty());

        LangDao.updateString(TestUtils.langEntry.getKey(), TestUtils.langEntry.getValue());

        assertEquals(1, LangDao.listStrings().size());

        LangDao.updateString(TestUtils.langEntry.getKey(), TestUtils.langEntry.getKey());

        values = LangDao.listStrings();
        assertEquals(1, values.size());
        assertEquals(TestUtils.langEntry.getKey(), values.get(TestUtils.langEntry.getKey()));

        LangDao.deleteString(TestUtils.langEntry.getKey());

        values = LangDao.listStrings();
        assertTrue(values.isEmpty());
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
        TestUtilsDao.deleteAllLang();
    }
}