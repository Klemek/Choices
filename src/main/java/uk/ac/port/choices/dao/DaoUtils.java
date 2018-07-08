package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.utils.Logger;

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

    //region Constants

    //ROOM ENTITY

    static final String QUESTIONS = "questions";
    static final String ROUND = "round";
    static final String MASTERID = "masterid";
    static final String STATE = "state";
    static final String USERS = "users";
    static final String SIMPLEID = "simpleId";
    static final String LOCK = "lock";

    //USER JSON

    static final String ID = "id";
    static final String NAME = "name";
    static final String IMAGEURL = "imageUrl";
    static final String ANSWER = "answer";

    //QUESTION JSON

    static final String TEXT = "text";
    static final String HINT = "hint";
    static final String ANSWERS = "answers";

    //endregion

    //region Functions

    //region Room

    static Room entityToRoom(Entity entity) {
        return new Room(entity.getKey().getId(),
                entity.getString(DaoUtils.SIMPLEID),
                DaoUtils.jsonListToQuestionList(entity.getList(DaoUtils.QUESTIONS)),
                (int) entity.getLong(DaoUtils.ROUND),
                entity.getString(DaoUtils.MASTERID),
                Room.parseState(entity.getString(DaoUtils.STATE)),
                DaoUtils.jsonListToUserList(entity.getList(DaoUtils.USERS)),
                entity.getBoolean(DaoUtils.LOCK));
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder roomToEntityBuilder(Room room, BaseEntity.Builder builder) {
        return builder.set(DaoUtils.USERS, DaoUtils.userListToJsonList(room.getUsers()))
                .set(DaoUtils.QUESTIONS, DaoUtils.questionListTojsonList(room.getQuestions()))
                .set(DaoUtils.MASTERID, room.getMasterId())
                .set(DaoUtils.STATE, room.getState().toString())
                .set(DaoUtils.ROUND, room.getRound())
                .set(DaoUtils.SIMPLEID, room.getSimpleId())
                .set(DaoUtils.LOCK, room.isLock());
    }

    //endregion

    //region User

    static StringValue userToJson(User user) {
        JSONObject json = new JSONObject();
        json.put(DaoUtils.ID, user.getId());
        json.put(DaoUtils.NAME, user.getName());
        json.put(DaoUtils.IMAGEURL, user.getImageUrl());
        json.put(DaoUtils.ANSWER, user.getAnswer());
        return StringValue.newBuilder(json.toString()).setExcludeFromIndexes(true).build();
    }

    static User jsonToUser(StringValue strJson) {
        try {
            JSONObject json = new JSONObject(strJson.get());
            String id = json.getString(DaoUtils.ID);
            String name = json.getString(DaoUtils.NAME);
            String imageurl = json.getString(DaoUtils.IMAGEURL);
            int answer = json.getInt(DaoUtils.ANSWER);
            return new User(id, name, imageurl, answer);
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }

    static List<User> jsonListToUserList(List<StringValue> input) {
        List<User> users = new ArrayList<>();
        for (StringValue strJson : input) {
            User u = DaoUtils.jsonToUser(strJson);
            if (u != null)
                users.add(u);
        }
        return users;
    }

    static List<StringValue> userListToJsonList(List<User> users) {
        List<StringValue> output = new ArrayList<>();
        for (User u : users)
            output.add(DaoUtils.userToJson(u));
        return output;
    }

    //endregion

    //region Question

    static StringValue questionToJson(Question question) {
        JSONObject json = new JSONObject();
        json.put(DaoUtils.TEXT, question.getText());
        json.put(DaoUtils.HINT, question.getHint());
        json.put(DaoUtils.ANSWERS, new JSONArray());
        for (int i = 0; i < question.getAnswers().length; i++)
            json.getJSONArray(DaoUtils.ANSWERS).put(question.getAnswers()[i]);
        return StringValue.newBuilder(json.toString()).setExcludeFromIndexes(true).build();
    }

    static Question jsonToQuestion(StringValue strJson) {
        try {
            JSONObject json = new JSONObject(strJson.get());
            String[] answers = new String[4];
            for (int i = 0; i < answers.length; i++)
                answers[i] = json.getJSONArray(DaoUtils.ANSWERS).getString(i);
            return new Question(json.getString(DaoUtils.TEXT),
                    json.getString(DaoUtils.HINT),
                    answers);
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }

    static List<Question> jsonListToQuestionList(List<StringValue> input) {
        List<Question> questions = new ArrayList<>();
        for (StringValue strJson : input) {
            Question q = DaoUtils.jsonToQuestion(strJson);
            if (q != null)
                questions.add(q);
        }
        return questions;
    }

    static List<StringValue> questionListTojsonList(List<Question> questions) {
        List<StringValue> output = new ArrayList<>();
        for (Question q : questions)
            output.add(DaoUtils.questionToJson(q));
        return output;
    }

    //endregion

    //endregion
}
