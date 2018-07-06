package uk.ac.port.dao;

import com.google.cloud.datastore.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.port.model.Question;
import uk.ac.port.model.Room;
import uk.ac.port.model.User;
import uk.ac.port.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RoomDao {

    //region Constants

    static final String KIND = "Room";

    //ROOM ENTITY

    static final String QUESTIONS = "questions";
    static final String ROUND = "round";
    static final String MASTERID = "masterid";
    static final String STATE = "state";
    static final String USERS = "users";
    static final String SIMPLEID = "simpleId";

    //USER JSON

    static final String ID = "id";
    static final String NAME = "name";
    static final String IMAGEURL = "imageUrl";
    static final String ANSWER = "answer";

    //QUESTION JSON

    static final String TEXT = "text";
    static final String ANSWERS = "answers";
    static final String CORRECTANSWER = "correctAnswer";

    //endregion

    //region Variables

    private final Datastore datastore;
    private final KeyFactory keyFactory;

    //endregion

    //region Constructor

    public RoomDao() {
        datastore = DatastoreOptions.getDefaultInstance().getService(); // Authorized Datastore service
        keyFactory = datastore.newKeyFactory().setKind(RoomDao.KIND);      // Is used for creating keys later
    }

    //endregion

    //region Functions
    @SuppressWarnings("unchecked")
    public Long createRoom(Room room) {
        IncompleteKey key = keyFactory.newKey();
        FullEntity<IncompleteKey> incRoomEntity = (FullEntity<IncompleteKey>) RoomDao.roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        Entity roomEntity = datastore.add(incRoomEntity); // Save the Entity
        room.setId(roomEntity.getKey().getId());
        Logger.log(Level.INFO, "Room {0} created", room.getSimpleId());
        return roomEntity.getKey().getId();
    }

    public Room getRoomBySimpleId(String simpleId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(RoomDao.KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(RoomDao.SIMPLEID, simpleId))
                .setLimit(1)
                .build();
        QueryResults<Entity> room = datastore.run(query);
        return room.hasNext() ? RoomDao.entityToRoom(room.next()) : null;
    }

    public void updateRoom(Room room) {
        Key key = keyFactory.newKey(room.getId());
        Entity entity = (Entity) RoomDao.roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        datastore.update(entity);
        Logger.log(Level.INFO, "Room {0} updated", room.getSimpleId());
    }

    public void deleteRoom(Room room) {
        Key key = keyFactory.newKey(room.getId());
        datastore.delete(key);
        Logger.log(Level.INFO, "Room {0} deleted", room.getSimpleId());
    }

    //endregion

    //region Class functions

    static Room entityToRoom(Entity entity) {
        List<Question> questions = RoomDao.jsonListToQuestionList(entity.getList(RoomDao.QUESTIONS));
        List<User> users = RoomDao.jsonListToUserList(entity.getList(RoomDao.USERS));
        int round = (int) entity.getLong(RoomDao.ROUND);
        Room.State state = Room.parseState(entity.getString(RoomDao.STATE));
        String masterId = entity.getString(RoomDao.MASTERID);
        String simpleId = entity.getString(RoomDao.SIMPLEID);

        return new Room(entity.getKey().getId(),
                simpleId,
                questions,
                round,
                masterId,
                state,
                users);
    }

    @SuppressWarnings("unchecked")
    static BaseEntity.Builder roomToEntityBuilder(Room room, BaseEntity.Builder builder) {
        return builder.set(RoomDao.USERS, RoomDao.userListToJsonList(room.getUsers()))
                .set(RoomDao.QUESTIONS, RoomDao.questionListTojsonList(room.getQuestions()))
                .set(RoomDao.MASTERID, room.getMasterId())
                .set(RoomDao.STATE, room.getState().toString())
                .set(RoomDao.ROUND, room.getRound())
                .set(RoomDao.SIMPLEID, room.getSimpleId());
    }

    static StringValue userToJson(User user) {
        JSONObject json = new JSONObject();
        json.put(RoomDao.ID, user.getId());
        json.put(RoomDao.NAME, user.getName());
        json.put(RoomDao.IMAGEURL, user.getImageUrl());
        json.put(RoomDao.ANSWER, user.getAnswer());
        return StringValue.newBuilder(json.toString()).setExcludeFromIndexes(true).build();
    }

    static User jsonToUser(StringValue strJson) {
        try {
            JSONObject json = new JSONObject(strJson.get());
            String id = json.getString(RoomDao.ID);
            String name = json.getString(RoomDao.NAME);
            String imageurl = json.getString(RoomDao.IMAGEURL);
            int answer = json.getInt(RoomDao.ANSWER);
            return new User(id, name, imageurl, answer);
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }

    static List<User> jsonListToUserList(List<StringValue> input) {
        List<User> users = new ArrayList<>();
        for (StringValue strJson : input) {
            User u = RoomDao.jsonToUser(strJson);
            if (u != null)
                users.add(u);
        }
        return users;
    }

    static List<StringValue> userListToJsonList(List<User> users) {
        List<StringValue> output = new ArrayList<>();
        for (User u : users)
            output.add(RoomDao.userToJson(u));
        return output;
    }

    static StringValue questionToJson(Question question) {
        JSONObject json = new JSONObject();
        json.put(RoomDao.TEXT, question.getText());
        json.put(RoomDao.CORRECTANSWER, question.getCorrectAnswer());
        json.put(RoomDao.ANSWERS, new JSONArray());
        for (int i = 0; i < question.getAnswers().length; i++)
            json.getJSONArray(RoomDao.ANSWERS).put(question.getAnswers()[i]);
        return StringValue.newBuilder(json.toString()).setExcludeFromIndexes(true).build();
    }

    static Question jsonToQuestion(StringValue strJson) {
        try {
            JSONObject json = new JSONObject(strJson.get());
            String text = json.getString(RoomDao.TEXT);
            String[] answers = new String[4];
            for (int i = 0; i < answers.length; i++)
                answers[i] = json.getJSONArray(RoomDao.ANSWERS).getString(i);
            int correctAnswer = json.getInt(RoomDao.CORRECTANSWER);
            return new Question(text, answers, correctAnswer);
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }

    static List<Question> jsonListToQuestionList(List<StringValue> input) {
        List<Question> questions = new ArrayList<>();
        for (StringValue strJson : input) {
            Question q = RoomDao.jsonToQuestion(strJson);
            if (q != null)
                questions.add(q);
        }
        return questions;
    }

    static List<StringValue> questionListTojsonList(List<Question> questions) {
        List<StringValue> output = new ArrayList<>();
        for (Question q : questions)
            output.add(RoomDao.questionToJson(q));
        return output;
    }

    //endregion

}
