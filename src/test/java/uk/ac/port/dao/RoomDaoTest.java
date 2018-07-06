package uk.ac.port.dao;

import com.google.cloud.datastore.*;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.model.Question;
import uk.ac.port.model.Room;
import uk.ac.port.model.User;
import uk.ac.port.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RoomDaoTest {

    private static Datastore datastore;
    private static KeyFactory keyFactory;
    private static RoomDao dao;

    @BeforeClass
    public static void setUp() {
        RoomDaoTest.datastore = DatastoreOptions.getDefaultInstance().getService();
        RoomDaoTest.keyFactory = RoomDaoTest.datastore.newKeyFactory().setKind(RoomDao.KIND);
        RoomDaoTest.dao = new RoomDao();
    }

    @Test
    public void entityToRoom() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(1L, "ABCDE", questionList, 5, "abcde", Room.State.ANSWERING, users);

        Entity entity = Entity.newBuilder(RoomDaoTest.keyFactory.newKey(room.getId()))
                .set(RoomDao.USERS, RoomDao.userListToJsonList(room.getUsers()))
                .set(RoomDao.QUESTIONS, RoomDao.questionListTojsonList(room.getQuestions()))
                .set(RoomDao.MASTERID, room.getMasterId())
                .set(RoomDao.STATE, room.getState().toString())
                .set(RoomDao.ROUND, room.getRound())
                .set(RoomDao.SIMPLEID, room.getSimpleId())
                .build();

        Room room2 = RoomDao.entityToRoom(entity);
        assertEquals(room, room2);
        assertEquals(1, room2.getQuestions().size());
        assertEquals(2, room2.getQuestions().get(0).getCorrectAnswer());
        assertEquals(1, room2.getUsers().size());
        assertEquals(4, room2.getUsers().get(0).getAnswer());
    }

    @Test
    public void roomToEntityBuilder() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(1L, "ABCDE", questionList, 5, "abcde", Room.State.ANSWERING, users);

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
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2);
        String str = RoomDao.questionToJson(q).get();
        JSONObject json = new JSONObject(str);

        assertEquals(q.getText(), json.getString(RoomDao.TEXT));
        assertEquals(q.getAnswers().length, json.getJSONArray(RoomDao.ANSWERS).length());
        assertEquals(q.getAnswers()[0], json.getJSONArray(RoomDao.ANSWERS).getString(0));
        assertEquals(q.getCorrectAnswer(), json.getInt(RoomDao.CORRECTANSWER));
    }

    @Test
    public void jsonToQuestion() {
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2);
        StringValue str = RoomDao.questionToJson(q);
        Question q2 = RoomDao.jsonToQuestion(str);

        assertEquals(q, q2);
    }

    @Test
    public void jsonListToQuestionList() {
        Question q = new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2);
        List<StringValue> lst = new ArrayList<>();
        lst.add(RoomDao.questionToJson(q));

        List<Question> questions = RoomDao.jsonListToQuestionList(lst);
        assertEquals(1, questions.size());
        assertEquals(q, questions.get(0));
    }

    @Test
    public void questionListToJsonList() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<StringValue> lst = RoomDao.questionListTojsonList(questionList);
        assertEquals(1, lst.size());
        Question q2 = RoomDao.jsonToQuestion(lst.get(0));
        assertEquals(questionList.get(0), q2);
    }

    @Test
    public void createRoom() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(0L, "ABCDE", questionList, 5, "abcde", Room.State.ANSWERING, users);

        Long id = dao.createRoom(room);

        assertNotNull(id);
        assertEquals(id, room.getId());
        assertNotEquals(0L, (long)room.getId());

        dao.deleteRoom(room);
    }

    @Test
    public void getRoomBySimpleId() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(0L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users);
        assertNotNull(dao.createRoom(room));

        Room room2 = dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        Room room3 = dao.getRoomBySimpleId("qumzfbqmzifgbqpbz");
        assertNull(room3);

        dao.deleteRoom(room);
    }

    @Test
    public void updateRoom() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(0L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users);
        assertNotNull(dao.createRoom(room));

        Room room2 = dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        room2.getUsers().add(new User("id2", "name", "imageUrl", 4));
        dao.updateRoom(room2);

        Room room3 = dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room2, room3);
        assertEquals(2, room3.getUsers().size());

        dao.deleteRoom(room);

    }

    @Test
    public void deleteRoom() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        List<User> users = new ArrayList<>();
        users.add(new User("id", "name", "imageUrl", 4));
        Room room = new Room(0L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users);
        assertNotNull(dao.createRoom(room));

        Room room2 = dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(room, room2);

        dao.deleteRoom(room);

        Room room3 = dao.getRoomBySimpleId(room.getSimpleId());
        assertNull(room3);
    }
}