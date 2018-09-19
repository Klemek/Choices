package fr.klemek.choices.dao;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;

import fr.klemek.choices.model.Question;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.model.Room;
import fr.klemek.choices.model.User;
import fr.klemek.choices.utils.Lang;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class DaoUtils {

    private static Datastore datastore;

    private DaoUtils() {
    }

    static Datastore getDatastore() {
        if (DaoUtils.datastore == null)
            DaoUtils.datastore = DatastoreOptions.getDefaultInstance().getService();
        return DaoUtils.datastore;
    }

    //region Functions

    static StringValue getStringValue(Object o) {
        return StringValue.newBuilder(o == null ? null : o.toString()).setExcludeFromIndexes(true).build();
    }

    //region Room

    static Room entityToRoom(Entity entity) {
        return new Room(
                entity.getKey().getId(),
                entity.getString(Room.KEY_SIMPLEID),
                entity.getString(Room.KEY_MASTERID),
                DaoUtils.jsonListToUserList(entity.getList(Room.KEY_USERS)),
                entity.getBoolean(Room.KEY_LOCK),
                entity.getBoolean(Room.KEY_LOCK_ANSWERS)
        );
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder roomToEntityBuilder(Room room, BaseEntity.Builder builder) {
        return builder.set(Room.KEY_USERS, DaoUtils.userListToJsonList(room.getUsers()))
                .set(Room.KEY_MASTERID, room.getMasterId())
                .set(Room.KEY_SIMPLEID, room.getSimpleId())
                .set(Room.KEY_LOCK, room.isLocked())
                .set(Room.KEY_LOCK_ANSWERS, room.areAnswersLocked());
    }

    //endregion

    //region Lang
    static Map.Entry<String, String> entityToLang(Entity entity) {
        return new AbstractMap.SimpleEntry<>(
                entity.getString(Lang.KEY_KEY),
                entity.getString(Lang.KEY_VALUE)
        );
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder langToEntityBuilder(String key, String value, BaseEntity.Builder builder) {
        return builder.set(Lang.KEY_KEY, key)
                .set(Lang.KEY_VALUE, DaoUtils.getStringValue(value));
    }

    static Map<String, String> entityListToLang(QueryResults<Entity> resultList) {
        Map<String, String> output = new HashMap<>();
        while (resultList.hasNext()) {
            Map.Entry<String, String> entry = DaoUtils.entityToLang(resultList.next());
            output.put(entry.getKey(), entry.getValue());
        }
        return output;
    }

    //endregion

    //region QuestionPack

    static QuestionPack entityToQuestionPack(Entity entity) {
        return new QuestionPack(
                entity.getKey().getId(),
                entity.getString(QuestionPack.KEY_NAME),
                entity.getString(QuestionPack.KEY_VIDEO),
                entity.getString(QuestionPack.KEY_MESSAGE),
                entity.contains(QuestionPack.KEY_ENABLED) ? entity.getBoolean(QuestionPack.KEY_ENABLED) : false,
                DaoUtils.jsonListToQuestionList(entity.getList(QuestionPack.KEY_QUESTIONS))
        );
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder questionPackToEntityBuilder(QuestionPack pack,
                                                          BaseEntity.Builder builder) {
        return builder
                .set(QuestionPack.KEY_NAME, pack.getName())
                .set(QuestionPack.KEY_VIDEO, DaoUtils.getStringValue(pack.getVideo()))
                .set(QuestionPack.KEY_MESSAGE, DaoUtils.getStringValue(pack.getMessage()))
                .set(QuestionPack.KEY_ENABLED, pack.isEnabled())
                .set(QuestionPack.KEY_QUESTIONS,
                        DaoUtils.questionListTojsonList(pack.getQuestions()));
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
            User u = User.fromJson(strJson.get());
            if (u != null)
                users.add(u);
        }
        return users;
    }

    static List<StringValue> userListToJsonList(List<User> users) {
        List<StringValue> output = new ArrayList<>();
        for (User u : users)
            output.add(DaoUtils.getStringValue(u.toJson()));
        return output;
    }

    //endregion

    //region Question

    static List<Question> jsonListToQuestionList(List<StringValue> input) {
        List<Question> questions = new ArrayList<>();
        for (StringValue strJson : input) {
            Question q = Question.fromJson(strJson.get());
            if (q != null)
                questions.add(q);
        }
        return questions;
    }

    static List<StringValue> questionListTojsonList(List<Question> questions) {
        List<StringValue> output = new ArrayList<>();
        for (Question q : questions)
            output.add(DaoUtils.getStringValue(q.toJson()));
        return output;
    }

    //endregion

    //endregion
}
