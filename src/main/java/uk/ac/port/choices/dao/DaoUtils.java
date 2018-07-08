package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;

import java.util.ArrayList;
import java.util.List;

final class DaoUtils {

    private static Datastore datastore;

    private DaoUtils() {
    }

    static Datastore getDatastore() {
        if (DaoUtils.datastore == null)
            DaoUtils.datastore = DatastoreOptions.getDefaultInstance().getService();  // Authorized Datastore service
        return DaoUtils.datastore;
    }

    //region Functions

    static StringValue getStringValue(Object o) {
        return StringValue.newBuilder(o.toString()).setExcludeFromIndexes(true).build();
    }

    //region Room

    static Room entityToRoom(Entity entity) {
        return new Room(
                entity.getKey().getId(),
                entity.getString(Room.KEY_SIMPLEID),
                DaoUtils.jsonListToQuestionList(entity.getList(Room.KEY_QUESTIONS)),
                (int) entity.getLong(Room.KEY_ROUND),
                entity.getString(Room.KEY_MASTERID),
                Room.parseState(entity.getString(Room.KEY_STATE)),
                DaoUtils.jsonListToUserList(entity.getList(Room.KEY_USERS)),
                entity.getBoolean(Room.KEY_LOCK)
        );
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder roomToEntityBuilder(Room room, BaseEntity.Builder builder) {
        return builder.set(Room.KEY_USERS, DaoUtils.userListToJsonList(room.getUsers()))
                .set(Room.KEY_QUESTIONS, DaoUtils.questionListTojsonList(room.getQuestions()))
                .set(Room.KEY_MASTERID, room.getMasterId())
                .set(Room.KEY_STATE, room.getState().toString())
                .set(Room.KEY_ROUND, room.getRound())
                .set(Room.KEY_SIMPLEID, room.getSimpleId())
                .set(Room.KEY_LOCK, room.isLocked());
    }

    //endregion

    //region QuestionPack

    static QuestionPack entityToQuestionPack(Entity entity) {
        return new QuestionPack(
                entity.getKey().getId(),
                entity.getString(QuestionPack.KEY_NAME),
                DaoUtils.jsonListToQuestionList(entity.getList(QuestionPack.KEY_QUESTIONS))
        );
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder questionPackToEntityBuilder(QuestionPack pack, BaseEntity.Builder builder) {
        return builder.set(QuestionPack.KEY_NAME, pack.getName())
                .set(QuestionPack.KEY_QUESTIONS, DaoUtils.questionListTojsonList(pack.getQuestions()));
    }

    static List<QuestionPack> entityListToQuestionPackList(QueryResults<Entity> resultList) {
        List<QuestionPack> output = new ArrayList<>();
        while (resultList.hasNext()) {
            output.add(DaoUtils.entityToQuestionPack(resultList.next()));
        }
        return output;
    }

    //endregion

    //region User

    static List<User> jsonListToUserList(List<StringValue> input) {
        List<User> users = new ArrayList<>();
        for (StringValue strJson : input) {
            User u = User.fromJSON(strJson.get());
            if (u != null)
                users.add(u);
        }
        return users;
    }

    static List<StringValue> userListToJsonList(List<User> users) {
        List<StringValue> output = new ArrayList<>();
        for (User u : users)
            output.add(DaoUtils.getStringValue(u.toJSON()));
        return output;
    }

    //endregion

    //region Question

    static List<Question> jsonListToQuestionList(List<StringValue> input) {
        List<Question> questions = new ArrayList<>();
        for (StringValue strJson : input) {
            Question q = Question.fromJSON(strJson.get());
            if (q != null)
                questions.add(q);
        }
        return questions;
    }

    static List<StringValue> questionListTojsonList(List<Question> questions) {
        List<StringValue> output = new ArrayList<>();
        for (Question q : questions)
            output.add(DaoUtils.getStringValue(q.toJSON()));
        return output;
    }

    //endregion

    //endregion
}
