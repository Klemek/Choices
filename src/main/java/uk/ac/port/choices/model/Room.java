package uk.ac.port.choices.model;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.port.choices.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {

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
        output.put("id", simpleId);

        JSONArray usersArray = new JSONArray();
        for (User u : users) {
            usersArray.put(u.toJSON());
        }
        output.put("users", usersArray);
        output.put("state", state.toString());
        output.put("roundCount", questions.size());
        output.put("lock", lock);

        if (state == Room.State.ANSWERING || state == Room.State.RESULTS) {
            JSONArray answers = new JSONArray();
            for (String answer : questions.get(round).getAnswers()) {
                answers.put(answer);
            }
            output.put("answers", answers);
            output.put("question", questions.get(round).getText());
            output.put("hint", questions.get(round).getHint());
            output.put("round", round);
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
