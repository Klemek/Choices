package uk.ac.port.choices.model;

import fr.klemek.betterlists.BetterArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.port.choices.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {

    //region Keys

    //Entity

    public static final String KEY_QUESTIONS = "questions";
    public static final String KEY_ROUND = "round";
    public static final String KEY_MASTERID = "masterid";
    public static final String KEY_STATE = "state";
    public static final String KEY_USERS = "users";
    public static final String KEY_SIMPLEID = "simpleId";
    public static final String KEY_LOCK = "lock";

    //JSON

    static final String KEY_ID = "id";
    static final String KEY_QUESTION = "question";
    static final String KEY_ROUND_COUNT = "roundCount";

    //endregion

    public enum State {
        REGISTERING,
        ANSWERING,
        RESULTS,
        CLOSED
    }

    private Long id;
    private final String simpleId;
    private final List<Question> questions;
    private int round;
    private final String masterId;
    private Room.State state;
    private final List<User> users;
    private boolean lock;

    public Room(List<Question> questions, String masterId) {
        this(null, Utils.getRandomString(6, "I", "l", "O", "0"), questions, 0, masterId, Room.State.REGISTERING, new ArrayList<>(), false);
    }

    public Room(Long id, String simpleId, List<Question> questions, int round, String masterId, Room.State state, List<User> users, boolean lock) {
        this.id = id;
        this.simpleId = simpleId;
        this.questions = questions;
        this.round = round;
        this.masterId = masterId;
        this.state = state;
        this.users = users;
        this.lock = lock;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getRound() {
        return round;
    }

    public String getMasterId() {
        return masterId;
    }

    public Room.State getState() {
        return state;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getSimpleId() {
        return simpleId;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLocked() {
        return lock;
    }

    public void next() {
        switch (state) {
            case REGISTERING:
                state = Room.State.ANSWERING;
                break;
            case ANSWERING:
                state = Room.State.RESULTS;
                break;
            case RESULTS:
                for (User u : users)
                    u.setAnswer(0);
                if (round + 1 < questions.size()) {
                    round++;
                    state = Room.State.ANSWERING;
                } else {
                    state = Room.State.CLOSED;
                }
                break;
            case CLOSED:
                break;
        }
    }

    public static Room.State parseState(String strState) {
        for (Room.State state : Room.State.values())
            if (strState.equals(state.toString()))
                return state;
        return Room.State.REGISTERING;
    }

    public JSONObject toJSON() {
        JSONObject output = new JSONObject();
        output.put(Room.KEY_ID, simpleId);

        output.put(Room.KEY_USERS, new JSONArray(BetterArrayList.fromList(users).select(User::toJSON)));
        output.put(Room.KEY_STATE, state.toString());
        output.put(Room.KEY_ROUND_COUNT, questions.size());
        output.put(Room.KEY_LOCK, lock);

        if (state == Room.State.ANSWERING || state == Room.State.RESULTS) {
            output.put(Room.KEY_QUESTION, questions.get(round).toJSON());
            output.put(Room.KEY_ROUND, round);
        }

        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return round == room.round &&
                Objects.equals(id, room.id) &&
                Objects.equals(simpleId, room.simpleId) &&
                Objects.equals(masterId, room.masterId) &&
                state == room.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, simpleId, round, masterId, state);
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", simpleId='" + simpleId + '\'' +
                ", questions=" + questions +
                ", round=" + round +
                ", masterId='" + masterId + '\'' +
                ", state=" + state +
                ", users=" + users +
                ", lock=" + lock +
                '}';
    }
}
