package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserTest {

    @Test
    public void toJSON() {
        User u = new User("id", "name", "imageUrl", 4);

        JSONObject json = u.toJSON();

        assertEquals(u.getId(), json.getString(User.KEY_ID));
        assertEquals(u.getImageUrl(), json.getString(User.KEY_IMAGEURL));
        assertEquals(u.getName(), json.getString(User.KEY_NAME));
        assertEquals(u.getAnswer(), json.getInt(User.KEY_ANSWER));
        assertEquals(4, json.length());
    }

    @Test
    public void fromJSON() {
        User u = new User("id", "name", "imageUrl", 4);

        String str = u.toJSON().toString();
        User u2 = User.fromJSON(str);

        assertEquals(u, u2);
        assertEquals(u.getAnswer(), u2.getAnswer());
        assertEquals(u.getName(), u2.getName());
        assertEquals(u.getImageUrl(), u2.getImageUrl());
    }
}