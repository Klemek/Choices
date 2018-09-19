package uk.ac.port.choices.model;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoomTest {

    @Test
    public void toJSON() {
        JSONObject json = TestUtils.room.toJson();
        assertEquals(4, json.length());
        assertEquals(TestUtils.room.getSimpleId(), json.getString(Room.KEY_ID));
        assertEquals(TestUtils.room.isLocked(), json.getBoolean(Room.KEY_LOCK));
        assertEquals(TestUtils.room.areAnswersLocked(), json.getBoolean(Room.KEY_LOCK_ANSWERS));
        assertEquals(1, json.getJSONArray(Room.KEY_USERS).length());
        assertEquals(TestUtils.user.toJson().toString(), json.getJSONArray(Room.KEY_USERS).getJSONObject(0).toString());


    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass());
    }
}