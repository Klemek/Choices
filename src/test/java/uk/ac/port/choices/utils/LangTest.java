package uk.ac.port.choices.utils;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.dao.TestUtilsDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LangTest {

    @Test
    public void toJson() {
        JSONObject json = Lang.toJson();
        assertEquals(1, json.length());
        assertEquals(TestUtils.langEntry.getValue(), json.getString(TestUtils.langEntry.getKey()));
    }

    @Test
    public void getHashCode() {
        JSONObject json = new JSONObject();
        json.put(TestUtils.langEntry.getKey(), TestUtils.langEntry.getValue());
        assertEquals(TestUtils.langEntry.getValue().hashCode(), Lang.getHashCode());
    }

    @Test
    public void update() {
        JSONObject json = new JSONObject();
        json.put(TestUtils.langEntry.getKey(), TestUtils.langEntry.getKey());
        Lang.update(json.toString());

        assertEquals(TestUtils.langEntry.getKey().hashCode(), Lang.getHashCode());

        json = Lang.toJson();
        assertEquals(1, json.length());
        assertEquals(TestUtils.langEntry.getKey(), json.getString(TestUtils.langEntry.getKey()));

        json.put(TestUtils.langEntry.getKey(), TestUtils.langEntry.getValue());
        Lang.update(json.toString());

        assertEquals(TestUtils.langEntry.getValue().hashCode(), Lang.getHashCode());
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
        TestUtilsDao.deleteAllLang();
        Lang.init(true);
    }
}