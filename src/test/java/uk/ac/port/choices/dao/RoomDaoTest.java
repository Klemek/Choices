package uk.ac.port.choices.dao;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RoomDaoTest {
    private static Room room;

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.setUpLocalDatastore());

        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"}));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        RoomDaoTest.room = new Room(1L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users, true);
    }


    @Test
    public void createRoom() {
        Long id = RoomDao.createRoom(RoomDaoTest.room);

        assertNotNull(id);
        assertEquals(id, RoomDaoTest.room.getId());
        assertNotEquals(0L, (long) RoomDaoTest.room.getId());

        RoomDao.deleteRoom(RoomDaoTest.room);
    }

    @Test
    public void getRoomBySimpleId() {
        assertNotNull(RoomDao.createRoom(RoomDaoTest.room));

        Room room2 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertEquals(RoomDaoTest.room, room2);

        Room room3 = RoomDao.getRoomBySimpleId("qumzfbqmzifgbqpbz");
        assertNull(room3);

        RoomDao.deleteRoom(RoomDaoTest.room);
    }

    @Test
    public void updateRoom() {
        assertNotNull(RoomDao.createRoom(RoomDaoTest.room));

        Room room2 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertEquals(RoomDaoTest.room, room2);

        room2.getUsers().add(new User("id2", "name", "imageUrl", 4));
        RoomDao.updateRoom(room2);

        Room room3 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertEquals(room2, room3);
        assertEquals(2, room3.getUsers().size());

        RoomDao.deleteRoom(RoomDaoTest.room);

    }

    @Test
    public void deleteRoom() {
        assertNotNull(RoomDao.createRoom(RoomDaoTest.room));

        Room room2 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertEquals(RoomDaoTest.room, room2);

        RoomDao.deleteRoom(RoomDaoTest.room);

        Room room3 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertNull(room3);
    }
}