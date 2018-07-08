package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import uk.ac.port.choices.model.Question;
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
        return new Room(entity.getKey().getId(),
                entity.getString(Room.SIMPLEID),
                DaoUtils.jsonListToQuestionList(entity.getList(Room.QUESTIONS)),
                (int) entity.getLong(Room.ROUND),
                entity.getString(Room.MASTERID),
                Room.parseState(entity.getString(Room.STATE)),
                DaoUtils.jsonListToUserList(entity.getList(Room.USERS)),
                entity.getBoolean(Room.LOCK));
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder roomToEntityBuilder(Room room, BaseEntity.Builder builder) {
        return builder.set(Room.USERS, DaoUtils.userListToJsonList(room.getUsers()))
                .set(Room.QUESTIONS, DaoUtils.questionListTojsonList(room.getQuestions()))
                .set(Room.MASTERID, room.getMasterId())
                .set(Room.STATE, room.getState().toString())
                .set(Room.ROUND, room.getRound())
                .set(Room.SIMPLEID, room.getSimpleId())
                .set(Room.LOCK, room.isLocked());
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
