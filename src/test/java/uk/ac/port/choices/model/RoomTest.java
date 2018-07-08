package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.Test;
import uk.ac.port.choices.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RoomTest {

    @Test
    public void toJSON() {
        User user = new User("id", "name", "imageUrl", 4);
        Question question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});

        List<Question> questionList = new ArrayList<>();
        questionList.add(question);
        List<User> users = new ArrayList<>();
        users.add(user);
        Room room = new Room(1L, Utils.getRandomString(6), questionList, 0, "abcde", Room.State.ANSWERING, users, true);

        JSONObject json = room.toJSON();
        assertEquals(7, json.length());
        assertEquals(room.getSimpleId(), json.getString(Room.KEY_ID));
        assertEquals(room.getState().toString(), json.getString(Room.KEY_STATE));
        assertEquals(room.isLocked(), json.getBoolean(Room.KEY_LOCK));
        assertEquals(room.getRound(), json.getInt(Room.KEY_ROUND));
        assertEquals(room.getQuestions().size(), json.getInt(Room.KEY_ROUND_COUNT));
        assertEquals(1, json.getJSONArray(Room.KEY_USERS).length());
        assertEquals(user.toJSON().toString(), json.getJSONArray(Room.KEY_USERS).getJSONObject(0).toString());
        assertEquals(question.toJSON().toString(), json.getJSONObject(Room.KEY_QUESTION).toString());

    }
}