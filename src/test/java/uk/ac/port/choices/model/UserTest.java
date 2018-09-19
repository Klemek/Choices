package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserTest {

    @Test
    public void toJSON() {
        JSONObject json = TestUtils.user.toJson();

        assertEquals(TestUtils.user.getId(), json.getString(User.KEY_ID));
        assertEquals(TestUtils.user.getImageUrl(), json.getString(User.KEY_IMAGEURL));
        assertEquals(TestUtils.user.getName(), json.getString(User.KEY_NAME));
        assertEquals(TestUtils.user.getAnswer(), json.getInt(User.KEY_ANSWER));
        assertEquals(4, json.length());
    }

    @Test
    public void fromJSON() {
        String str = TestUtils.user.toJson().toString();
        User u2 = User.fromJson(str);

        assertEquals(TestUtils.user, u2);
        assertEquals(TestUtils.user.getAnswer(), u2.getAnswer());
        assertEquals(TestUtils.user.getName(), u2.getName());
        assertEquals(TestUtils.user.getImageUrl(), u2.getImageUrl());
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}