package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RoomDaoTest {

    private static Datastore datastore;
    private static KeyFactory keyFactory;
    private static RoomDao dao;
    private static Room room;

    @BeforeClass
    public static void setUp() {
        RoomDaoTest.datastore = DatastoreOptions.getDefaultInstance().getService();
        RoomDaoTest.keyFactory = RoomDaoTest.datastore.newKeyFactory().setKind(RoomDao.KIND);
        RoomDaoTest.dao = new RoomDao();

        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        room = new Room(1L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users, true);
    }

    @Test
    public void entityToRoom() {
        Entity entity = Entity.newBuilder(RoomDaoTest.keyFactory.newKey(room.getId()))
                .set(RoomDao.USERS, RoomDao.userListToJsonList(room.getUsers()))
                .set(RoomDao.QUESTIONS, RoomDao.questionListTojsonList(room.getQuestions()))
                .set(RoomDao.MASTERID, room.getMasterId())
                .set(RoomDao.STATE, room.getState().toString())
                .set(RoomDao.ROUND, room.getRound())
                .set(RoomDao.SIMPLEID, room.getSimpleId())
                .set(RoomDao.LOCK, room.isLock())
                .build();

        Room room2 = RoomDao.entityToRoom(entity);
        assertEquals(room, room2);
        assertEquals(1, room2.getQuestions().size());
        assertEquals(1, room2.getUsers().size());
        assertEquals(4, room2.getUsers().get(0).getAnswer());
    }

    @Test
    public void roomToEntityBuilder() {
        Entity entity = (Entity) RoomDao.roomToEntityBuilder(room, Entity.newBuilder(RoomDaoTest.keyFactory.newKey(room.getId()))).build();

        Room room2 = RoomDao.entityToRoom(entity);
        assertEquals(room, room2);
    }

    @Test
    public void userToJson() {
        User u = new User("id", "name", "imageUrl", 4);
        String str = RoomDao.userToJson(u).get();
        JSONObject json = new JSONObject(str);

        assertEquals(u.getId(), json.getString(RoomDao.ID));
        assertEquals(u.getImageUrl(), json.getString(RoomDao.IMAGEURL));
        assertEquals(u.getName(), json.getString(RoomDao.NAME));
        assertEquals(u.getAnswer(), json.getInt(RoomDao.ANSWER));
    }

    @Test
    public void jsonToUser() {
        User u = new User("id", "name", "imageUrl", 4);
        StringValue str = RoomDao.userToJson(u);
        User u2 = RoomDao.jsonToUser(str);

        assertEquals(u, u2);
        assertEquals(u.getAnswer(), u2.getAnswer());
        assertEquals(u.getName(), u2.getName());
        assertEquals(u.getImageUrl(), u2.getImageUrl());
    }

    @Test
    public void jsonListToUserList() {
        User u = new User("id", "name", "imageUrl", 4);
        List<StringValue> lst = new ArrayList<>();
        lst.add(RoomDao.userToJson(u));

        List<User> users = RoomDao.jsonListToUserList(lst);
        assertEquals(1, users.size());
        assertEquals(u, users.get(0));
    }

    @Test
    public void userListToJsonList() {
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        List<StringValue> lst = RoomDao.userListToJsonList(users);
        assertEquals(1, lst.size());
        User u2 = RoomDao.jsonToUser(lst.get(0));
        assertEquals(users.get(0), u2);
    }

    @Test
    public void questionToJson() {
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"});
        String str = RoomDao.questionToJson(q).get();
        JSONObject json = new JSONObject(str);

        assertEquals(q.getText(), json.getString(RoomDao.TEXT));
        assertEquals(q.getAnswers().length, json.getJSONArray(RoomDao.ANSWERS).length());
        assertEquals(q.getAnswers()[0], json.getJSONArray(RoomDao.ANSWERS).getString(0));
    }

    @Test
    public void jsonToQuestion() {
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"});
        StringValue str = RoomDao.questionToJson(q);
        Question q2 = RoomDao.jsonToQuestion(str);

        assertEquals(q, q2);
    }

    @Test
    public void jsonListToQuestionList() {
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"});
        List<StringValue> lst = new ArrayList<>();
        lst.add(RoomDao.questionToJson(q));

        List<Question> questions = RoomDao.jsonListToQuestionList(lst);
        assertEquals(1, questions.size());
        assertEquals(q, questions.get(0));
    }

    @Test
    public void questionListToJsonList() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}));
        List<StringValue> lst = RoomDao.questionListTojsonList(questionList);
        assertEquals(1, lst.size());
        Question q2 = RoomDao.jsonToQuestion(lst.get(0));
        assertEquals(questionList.get(0), q2);
    }

    @Test
    public void createRoom() {
        Long id = RoomDaoTest.dao.createRoom(room);

        assertNotNull(id);
        assertEquals(id, room.getId());
        assertNotEquals(0L, (long)room.getId());

        RoomDaoTest.dao.deleteRoom(room);
    }

    @Test
    public void getRoomBySimpleId() {
        assertNotNull(RoomDaoTest.dao.createRoom(room));

        Room room2 = RoomDaoTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        Room room3 = RoomDaoTest.dao.getRoomBySimpleId("qumzfbqmzifgbqpbz");
        assertNull(room3);

        RoomDaoTest.dao.deleteRoom(room);
    }

    @Test
    public void updateRoom() {
        assertNotNull(RoomDaoTest.dao.createRoom(room));

        Room room2 = RoomDaoTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        room2.getUsers().add(new User("id2", "name", "imageUrl", 4));
        RoomDaoTest.dao.updateRoom(room2);

        Room room3 = RoomDaoTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room2, room3);
        assertEquals(2, room3.getUsers().size());

        RoomDaoTest.dao.deleteRoom(room);

    }

    @Test
    public void deleteRoom() {
        assertNotNull(RoomDaoTest.dao.createRoom(room));

        Room room2 = RoomDaoTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        RoomDaoTest.dao.deleteRoom(room);

        Room room3 = RoomDaoTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertNull(room3);
    }
}