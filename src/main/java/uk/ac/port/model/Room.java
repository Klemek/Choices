package uk.ac.port.model;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.port.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
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

    public Room(List<Question> questions, String masterId) {
        this(null, Utils.getRandomString(6), questions, 0, masterId, Room.State.REGISTERING, new ArrayList<>());
    }

    public Room(Long id, String simpleId, List<Question> questions, int round, String masterId, Room.State state, List<User> users) {
        this.id = id;
        this.simpleId = simpleId;
        this.questions = questions;
        this.round = round;
        this.masterId = masterId;
        this.state = state;
        this.users = users;
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

    public void next() {
        switch (state) {
            case REGISTERING:
                state = Room.State.ANSWERING;
                break;
            case ANSWERING:
                state = Room.State.RESULTS;
                break;
            case RESULTS:
                if (round + 1 < questions.size()) {
                    round++;
                    state = Room.State.ANSWERING;
                    for (User u : users)
                        u.setAnswer(0);
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

        switch (state) {
            case RESULTS:
                output.put("correctAnswer", questions.get(round).getCorrectAnswer());
            case ANSWERING:
                JSONArray answers = new JSONArray();
                for (String answer : questions.get(round).getAnswers()) {
                    answers.put(answer);
                }
                output.put("answers", answers);
                output.put("question", questions.get(round).getText());
                output.put("round", round);
            case CLOSED:
                output.put("roundCount", questions.size());
            case REGISTERING:
                break;
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
                ", questions.size=" + questions.size() +
                ", round=" + round +
                ", masterId='" + masterId + '\'' +
                ", state=" + state +
                ", users.size=" + users.size() +
                '}';
    }
}
