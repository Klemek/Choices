package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
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
                .set(Room.KEY_USERS, DaoUtils.userListToJsonList(DaoUtilsTest.room.getUsers()))
                .set(Room.KEY_QUESTIONS, DaoUtils.questionListTojsonList(DaoUtilsTest.room.getQuestions()))
                .set(Room.KEY_MASTERID, DaoUtilsTest.room.getMasterId())
                .set(Room.KEY_STATE, DaoUtilsTest.room.getState().toString())
                .set(Room.KEY_ROUND, DaoUtilsTest.room.getRound())
                .set(Room.KEY_SIMPLEID, DaoUtilsTest.room.getSimpleId())
                .set(Room.KEY_LOCK, DaoUtilsTest.room.isLocked())
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
    public void jsonListToUserList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.getStringValue(DaoUtilsTest.user.toJSON()));

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
        User u2 = User.fromJSON(lst.get(0).get());
        assertEquals(users.get(0), u2);
    }

    @Test
    public void jsonListToQuestionList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.getStringValue(DaoUtilsTest.question.toJSON()));

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
        Question q2 = Question.fromJSON(lst.get(0).get());
        assertEquals(questionList.get(0), q2);
    }

}
