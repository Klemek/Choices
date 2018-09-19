package uk.ac.port.choices.dao;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.utils.Lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DaoUtilsTest {

    private static KeyFactory roomKeyFactory;
    private static KeyFactory questionPackKeyFactory;
    private static KeyFactory langKeyFactory;

    @Test
    public void entityToRoom() {
        Entity entity = Entity.newBuilder(DaoUtilsTest.roomKeyFactory.newKey(TestUtils.room.getId()))
                .set(Room.KEY_USERS, DaoUtils.userListToJsonList(TestUtils.room.getUsers()))
                .set(Room.KEY_MASTERID, TestUtils.room.getMasterId())
                .set(Room.KEY_SIMPLEID, TestUtils.room.getSimpleId())
                .set(Room.KEY_LOCK, TestUtils.room.isLocked())
                .set(Room.KEY_LOCK_ANSWERS, TestUtils.room.areAnswersLocked())
                .build();

        Room room2 = DaoUtils.entityToRoom(entity);
        assertEquals(TestUtils.room, room2);
        assertEquals(1, room2.getUsers().size());
        assertEquals(4, room2.getUsers().get(0).getAnswer());
        assertEquals(TestUtils.room.isLocked(), room2.isLocked());
        assertEquals(TestUtils.room.areAnswersLocked(), room2.areAnswersLocked());
    }

    @Test
    public void roomToEntityBuilder() {
        Entity entity = (Entity) DaoUtils.roomToEntityBuilder(TestUtils.room, Entity.newBuilder(DaoUtilsTest.roomKeyFactory.newKey(TestUtils.room.getId()))).build();

        Room room2 = DaoUtils.entityToRoom(entity);
        assertEquals(TestUtils.room, room2);
    }

    @Test
    public void entityToQuestionPack() {
        Entity entity = Entity.newBuilder(DaoUtilsTest.questionPackKeyFactory.newKey(TestUtils.pack.getId()))
                .set(QuestionPack.KEY_NAME, TestUtils.pack.getName())
                .set(QuestionPack.KEY_VIDEO, TestUtils.pack.getVideo())
                .set(QuestionPack.KEY_MESSAGE, TestUtils.pack.getMessage())
                .set(QuestionPack.KEY_ENABLED, TestUtils.pack.isEnabled())
                .set(QuestionPack.KEY_QUESTIONS, DaoUtils.questionListTojsonList(TestUtils.pack.getQuestions()))
                .build();

        QuestionPack pack2 = DaoUtils.entityToQuestionPack(entity);
        assertEquals(TestUtils.pack, pack2);
    }

    @Test
    public void entityToQuestionPackMissingEnabled() {
        Entity entity = Entity.newBuilder(DaoUtilsTest.questionPackKeyFactory.newKey(TestUtils.pack.getId()))
                .set(QuestionPack.KEY_NAME, TestUtils.pack.getName())
                .set(QuestionPack.KEY_VIDEO, TestUtils.pack.getVideo())
                .set(QuestionPack.KEY_MESSAGE, TestUtils.pack.getMessage())
                .set(QuestionPack.KEY_QUESTIONS, DaoUtils.questionListTojsonList(TestUtils.pack.getQuestions()))
                .build();

        QuestionPack pack2 = DaoUtils.entityToQuestionPack(entity);
        assertFalse(pack2.isEnabled());
    }

    @Test
    public void questionPackToEntityBuilder() {
        Entity entity = (Entity) DaoUtils.questionPackToEntityBuilder(TestUtils.pack, Entity.newBuilder(DaoUtilsTest.questionPackKeyFactory.newKey(TestUtils.pack.getId()))).build();

        QuestionPack pack2 = DaoUtils.entityToQuestionPack(entity);
        assertEquals(TestUtils.pack, pack2);
    }

    @Test
    public void jsonListToUserList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.getStringValue(TestUtils.user.toJson()));

        List<User> users = DaoUtils.jsonListToUserList(lst);
        assertEquals(1, users.size());
        assertEquals(TestUtils.user, users.get(0));
    }

    @Test
    public void userListToJsonList() {
        List<User> users = new ArrayList<>();
        users.add(TestUtils.user);
        List<StringValue> lst = DaoUtils.userListToJsonList(users);
        assertEquals(1, lst.size());
        User u2 = User.fromJson(lst.get(0).get());
        assertEquals(users.get(0), u2);
    }

    @Test
    public void jsonListToQuestionList() {
        List<StringValue> lst = new ArrayList<>();
        lst.add(DaoUtils.getStringValue(TestUtils.question.toJson()));

        List<Question> questions = DaoUtils.jsonListToQuestionList(lst);
        assertEquals(1, questions.size());
        assertEquals(TestUtils.question, questions.get(0));
    }

    @Test
    public void questionListToJsonList() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(TestUtils.question);
        List<StringValue> lst = DaoUtils.questionListTojsonList(questionList);
        assertEquals(1, lst.size());
        Question q2 = Question.fromJson(lst.get(0).get());
        assertEquals(questionList.get(0), q2);
    }

    @Test
    public void entityToLang() {
        Entity entity = Entity.newBuilder(DaoUtilsTest.langKeyFactory.newKey(1L))
                .set(Lang.KEY_KEY, TestUtils.langEntry.getKey())
                .set(Lang.KEY_VALUE, TestUtils.langEntry.getValue())
                .build();

        Map.Entry<String, String> entry = DaoUtils.entityToLang(entity);
        assertEquals(TestUtils.langEntry, entry);
    }

    @Test
    public void langToEntityBuilder() {
        Entity entity = (Entity) DaoUtils.langToEntityBuilder(TestUtils.langEntry.getKey(),
                TestUtils.langEntry.getValue(),
                Entity.newBuilder(DaoUtilsTest.langKeyFactory.newKey(1L))).build();

        Map.Entry<String, String> entry = DaoUtils.entityToLang(entity);
        assertEquals(TestUtils.langEntry, entry);
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));

        Datastore datastore = DaoUtils.getDatastore();
        DaoUtilsTest.roomKeyFactory = datastore.newKeyFactory().setKind(Room.MODEL_KIND);
        DaoUtilsTest.questionPackKeyFactory = datastore.newKeyFactory().setKind(QuestionPack.MODEL_KIND);
        DaoUtilsTest.langKeyFactory = datastore.newKeyFactory().setKind(Lang.MODEL_KIND);

        List<Question> questionList = new ArrayList<>();
        questionList.add(TestUtils.question);

        List<User> users = new ArrayList<>();
        users.add(TestUtils.user);
    }
}
