package fr.klemek.choices.model;

import fr.klemek.choices.TestUtils;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuestionTest {

    @Test
    public void toJSON() {
        JSONObject json = TestUtils.question.toJson();

        assertEquals(TestUtils.question.getText(), json.getString(Question.KEY_TEXT));
        assertEquals(TestUtils.question.getAnswers().length, json.getJSONArray(Question.KEY_ANSWERS).length());
        assertEquals(TestUtils.question.getAnswers()[0], json.getJSONArray(Question.KEY_ANSWERS).getString(0));
        assertEquals(TestUtils.question.getLinks().length, json.getJSONArray(Question.KEY_LINKS).length());
        assertEquals(TestUtils.question.getLinks()[1], json.getJSONArray(Question.KEY_LINKS).getString(1));
        assertEquals(3, json.length());
    }

    @Test
    public void fromJSON() {
        String json = TestUtils.question.toJson().toString();
        Question q2 = Question.fromJson(json);
        assertEquals(TestUtils.question, q2);
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}