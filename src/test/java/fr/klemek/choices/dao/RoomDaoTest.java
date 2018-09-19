package fr.klemek.choices.dao;

import fr.klemek.choices.TestUtils;
import fr.klemek.choices.model.Room;
import fr.klemek.choices.model.User;
import fr.klemek.choices.utils.Utils;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RoomDaoTest {
    private static Room room;

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

    @Test
    public void deleteAllRoom() {
        assertNotNull(RoomDao.createRoom(RoomDaoTest.room));

        Room room2 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertEquals(RoomDaoTest.room, room2);

        RoomDao.deleteAllRooms();

        Room room3 = RoomDao.getRoomBySimpleId(RoomDaoTest.room.getSimpleId());
        assertNull(room3);
    }

    @Test
    public void listRoomSimpleIds() {
        assertNotNull(RoomDao.createRoom(RoomDaoTest.room));

        List<String> list = RoomDao.listRoomSimpleIds();
        assertEquals(1, list.size());
        assertEquals(RoomDaoTest.room.getSimpleId(), list.get(0));

        RoomDao.deleteRoom(RoomDaoTest.room);
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));

        RoomDaoTest.room = new Room(1L, Utils.getRandomString(6), "abcde", Arrays.asList(TestUtils.user), true, false);
    }
}