package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuestionTest {

    @Test
    public void toJSON() {
        Question q = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});

        JSONObject json = q.toJSON();

        assertEquals(q.getText(), json.getString(Question.KEY_TEXT));
        assertEquals(q.getHint(), json.getString(Question.KEY_HINT));
        assertEquals(q.getAnswers().length, json.getJSONArray(Question.KEY_ANSWERS).length());
        assertEquals(q.getAnswers()[0], json.getJSONArray(Question.KEY_ANSWERS).getString(0));
        assertEquals(3, json.length());
    }

    @Test
    public void fromJSON() {
        Question q = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});

        String json = q.toJSON().toString();
        Question q2 = Question.fromJSON(json);

        assertEquals(q, q2);
    }
}