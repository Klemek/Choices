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

import static org.junit.Assert.assertEquals;

public class DaoUtilsTest {

    private static KeyFactory roomKeyFactory;
    private static User user;
    private static Question question;
    private static Room room;

    @BeforeClass
    public static void setUp() {
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
        DaoUtilsTest.roomKeyFactory = datastore.newKeyFactory().setKind(RoomDao.KIND);

        DaoUtilsTest.user = new User("id", "name", "imageUrl", 4);
        DaoUtilsTest.question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});

        List<Question> questionList = new ArrayList<>();
        questionList.add(DaoUtilsTest.question);
        List<User> users = new ArrayList<>();
        users.add(DaoUtilsTest.user);
        DaoUtilsTest.room = new Room(1L, Utils.getRandomString(6), questionList, 5, "abcde", Room.State.ANSWERING, users, true);
    }


    @Test
    public void entityToRoom() {
        Entity entity = Entity.newBuilder(DaoUtilsTest.roomKeyFactory.newKey(DaoUtilsTest.room.getId()))
                .set(DaoUtils.USERS, DaoUtils.userListToJsonList(DaoUtilsTest.room.getUsers()))
                .set(DaoUtils.QUESTIONS, DaoUtils.questionListTojsonList(DaoUtilsTest.room.getQuestions()))
                .set(DaoUtils.MASTERID, DaoUtilsTest.room.getMasterId())
                .set(DaoUtils.STATE, DaoUtilsTest.room.getState().toString())
                .set(DaoUtils.ROUND, DaoUtilsTest.room.getRound())
                .set(DaoUtils.SIMPLEID, DaoUtilsTest.room.getSimpleId())
                .set(DaoUtils.LOCK, DaoUtilsTest.room.isLocked())
                .build();

        Room room2 = DaoUtils.entityToRoom(entity);
        assertEquals(DaoUtilsTest.room, room2);
        assertEquals(1, room2.getQuestions().size());
        assertEquals(1, room2.getUsers().size());
        assertEquals(4, room2.getUsers().get(0).getAnswer());
    }

    @Test
    public void roomToEntityBuilder() {
        Entity entity = (Entity) DaoUtils.roomToEntityBuilder(DaoUtilsTest.room, Entity.newBuilder(DaoUtilsTest.roomKeyFactory.newKey(DaoUtilsTest.room.getId()))).build();

        Room room2 = DaoUtils.entityToRoom(entity);
        assertEquals(DaoUtilsTest.room, room2);
    }

    @Test
    public void userToJson() {
        String str = DaoUtils.userToJson(DaoUtilsTest.user).get();
        JSONObject json = new JSONObject(str);

        assertEquals(DaoUtilsTest.user.getId(), json.getString(DaoUtils.ID));
        assertEquals(DaoUtilsTest.user.getImageUrl(), json.getString(DaoUtils.IMAGEURL));
        assertEquals(DaoUtilsTest.user.getName(), json.getString(DaoUtils.NAME));
        assertEquals(DaoUtilsTest.user.getAnswer(), json.getInt(DaoUtils.ANSWER));
    }

    @Test
    public void jsonToUser() {
        StringValue str = DaoUtils.userToJson(DaoUtilsTest.user);
        User u2 = DaoUtils.jsonToUser(str);

        assertEquals(DaoUtilsTest.user, u2);
        assertEquals(DaoUtilsTest.user.getAnswer(), u2.getAnswer());
        assertEquals(DaoUtilsTest.user.getName(), u2.getName());
        assertEquals(DaoUtilsTest.user.getImageUrl(), u2.getImageUrl());
    }

    @Test
    public void jsonListToUserList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.userToJson(DaoUtilsTest.user));

        List<User> users = DaoUtils.jsonListToUserList(lst);
        assertEquals(1, users.size());
        assertEquals(DaoUtilsTest.user, users.get(0));
    }

    @Test
    public void userListToJsonList() {
        List<User> users = new ArrayList<>();
        users.add(DaoUtilsTest.user);
        List<StringValue> lst = DaoUtils.userListToJsonList(users);
        assertEquals(1, lst.size());
        User u2 = DaoUtils.jsonToUser(lst.get(0));
        assertEquals(users.get(0), u2);
    }

    @Test
    public void questionToJson() {
        String str = DaoUtils.questionToJson(DaoUtilsTest.question).get();
        JSONObject json = new JSONObject(str);

        assertEquals(DaoUtilsTest.question.getText(), json.getString(DaoUtils.TEXT));
        assertEquals(DaoUtilsTest.question.getHint(), json.getString(DaoUtils.HINT));
        assertEquals(DaoUtilsTest.question.getAnswers().length, json.getJSONArray(DaoUtils.ANSWERS).length());
        assertEquals(DaoUtilsTest.question.getAnswers()[0], json.getJSONArray(DaoUtils.ANSWERS).getString(0));
    }

    @Test
    public void jsonToQuestion() {
        StringValue str = DaoUtils.questionToJson(DaoUtilsTest.question);
        Question q2 = DaoUtils.jsonToQuestion(str);

        assertEquals(DaoUtilsTest.question, q2);
    }

    @Test
    public void jsonListToQuestionList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.questionToJson(DaoUtilsTest.question));

        List<Question> questions = DaoUtils.jsonListToQuestionList(lst);
        assertEquals(1, questions.size());
        assertEquals(DaoUtilsTest.question, questions.get(0));
    }

    @Test
    public void questionListToJsonList() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(DaoUtilsTest.question);
        List<StringValue> lst = DaoUtils.questionListTojsonList(questionList);
        assertEquals(1, lst.size());
        Question q2 = DaoUtils.jsonToQuestion(lst.get(0));
        assertEquals(questionList.get(0), q2);
    }

}
