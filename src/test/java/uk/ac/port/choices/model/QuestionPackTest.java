package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuestionPackTest {

    @Test
    public void toJSON() {
        Question question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});
        List<Question> questionList = new ArrayList<>();
        questionList.add(question);

        QuestionPack pack = new QuestionPack(1L, "name", questionList);

        JSONObject json = pack.toJSON();
        assertEquals(3, json.length());
        assertEquals((long) pack.getId(), json.getLong(QuestionPack.KEY_ID));
        assertEquals(pack.getName(), json.getString(QuestionPack.KEY_NAME));
        assertEquals(1, json.getJSONArray(Room.KEY_QUESTIONS).length());
        assertEquals(question.toJSON().toString(), json.getJSONArray(Room.KEY_QUESTIONS).get(0).toString());

    }
}