package fr.klemek.choices.model;

import fr.klemek.betterlists.BetterArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

public class Room {

    //region Keys

    public static final String MODEL_KIND = "Room2";

    //Entity

    public static final String KEY_MASTERID = "masterid";
    public static final String KEY_USERS = "users";
    public static final String KEY_SIMPLEID = "simpleId";
    public static final String KEY_LOCK = "lock";
    public static final String KEY_LOCK_ANSWERS = "lockAnswers";

    //JSON

    public static final String KEY_RESET = "reset";
    public static final String KEY_PACK = "pack";

    static final String KEY_ID = "id";

    //endregion
    private final String simpleId;
    private final String masterId;
    private final List<User> users;
    private Long id;
    private boolean lock;
    private boolean lockAnswers;

    /**
     * Create a new fresh room.
     *
     * @param masterId the master's g+ id
     * @param simpleId the room's id
     */
    public Room(String masterId, String simpleId) {
        this(
                null,
                simpleId,
                masterId,
                new ArrayList<>(),
                false,
                false
        );
    }

    /**
     * Create a new room.
     *
     * @param id          the datastore id
     * @param simpleId    the simple id
     * @param masterId    the master's g+ id
     * @param users       the list of User
     * @param lock        if its locked
     * @param lockAnswers if user's can answer
     */
    public Room(Long id, String simpleId,
                String masterId, List<User> users, boolean lock, boolean lockAnswers) {
        this.id = id;
        this.simpleId = simpleId;
        this.masterId = masterId;
        this.users = users;
        this.lock = lock;
        this.lockAnswers = lockAnswers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMasterId() {
        return masterId;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getSimpleId() {
        return simpleId;
    }

    public boolean areAnswersLocked() {
        return lockAnswers;
    }

    public void setAnswersLocked(boolean lockAnswers) {
        this.lockAnswers = lockAnswers;
    }

    public boolean isLocked() {
        return lock;
    }

    public void setLocked(boolean lock) {
        this.lock = lock;
    }

    /**
     * Reset all user's answers.
     */
    public void resetAnswers() {
        for (User u : users)
            u.setAnswer(0);
    }

    /**
     * Return a JSONObject representing this object.
     *
     * @return a JSONObject representing this object
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put(Room.KEY_ID, simpleId);
        output.put(Room.KEY_USERS, new JSONArray(BetterArrayList.fromList(users).select(User::toJson)));
        output.put(Room.KEY_LOCK, lock);
        output.put(Room.KEY_LOCK_ANSWERS, lockAnswers);
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return lock == room.lock
                && lockAnswers == room.lockAnswers
                && Objects.equals(simpleId, room.simpleId)
                && Objects.equals(masterId, room.masterId)
                && Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simpleId, masterId, id, lock, lockAnswers);
    }

    @Override
    public String toString() {
        return "Room{"
                + "simpleId='" + simpleId + '\''
                + ", masterId='" + masterId + '\''
                + ", users=" + users
                + ", id=" + id
                + ", lock=" + lock
                + ", lockAnswers=" + lockAnswers
                + '}';
    }
}
