package fr.klemek.choices.model;

import fr.klemek.choices.TestUtils;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuestionPackTest {

    @Test
    public void toJSON() {
        JSONObject json = TestUtils.pack.toJson();
        assertEquals(6, json.length());
        assertEquals((long) TestUtils.pack.getId(), json.getLong(QuestionPack.KEY_ID));
        assertEquals(TestUtils.pack.getName(), json.getString(QuestionPack.KEY_NAME));
        assertEquals(TestUtils.pack.getVideo(), json.getString(QuestionPack.KEY_VIDEO));
        assertEquals(TestUtils.pack.getMessage(), json.getString(QuestionPack.KEY_MESSAGE));
        assertEquals(TestUtils.pack.isEnabled(), json.getBoolean(QuestionPack.KEY_ENABLED));
        assertEquals(1, json.getJSONArray(QuestionPack.KEY_QUESTIONS).length());
        assertEquals(TestUtils.question.toJson().toString(), json.getJSONArray(QuestionPack.KEY_QUESTIONS).get(0).toString());
    }

    @Test
    public void toJSONSimple() {
        JSONObject json = TestUtils.pack.toJson(false);
        assertEquals(3, json.length());
        assertEquals((long) TestUtils.pack.getId(), json.getLong(QuestionPack.KEY_ID));
        assertEquals(TestUtils.pack.getName(), json.getString(QuestionPack.KEY_NAME));
        assertEquals(1, json.getInt(QuestionPack.KEY_QUESTION_COUNT));
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}