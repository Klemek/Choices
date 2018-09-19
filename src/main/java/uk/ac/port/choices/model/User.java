package uk.ac.port.choices.model;

import fr.klemek.logger.Logger;

import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    //region keys

    static final String KEY_ID = "id";
    static final String KEY_NAME = "name";
    static final String KEY_IMAGEURL = "imageUrl";
    static final String KEY_ANSWER = "answer";

    //endregion

    private final String id;
    private final String name;
    private final String imageUrl;
    private int answer;

    public User(String id) {
        this(id, null, null);
    }

    public User(String id, String name, String imageUrl) {
        this(id, name, imageUrl, 0);
    }

    /**
     * Create a new user.
     *
     * @param id       the datastore id
     * @param name     the name
     * @param imageUrl the url of his image
     * @param answer   his current answer
     */
    public User(String id, String name, String imageUrl, int answer) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    /**
     * Return a JSONObject representing this object.
     *
     * @return a JSONObject representing this object
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put(User.KEY_ID, id);
        output.put(User.KEY_NAME, name);
        output.put(User.KEY_IMAGEURL, imageUrl);
        output.put(User.KEY_ANSWER, answer);
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", imageUrl='" + imageUrl + '\''
                + ", answer=" + answer
                + '}';
    }

    /**
     * Recreate an User from it JSON value.
     *
     * @param strJson the JSON value as String
     * @return the recreated User or null on error
     */
    public static User fromJson(String strJson) {
        try {
            JSONObject json = new JSONObject(strJson);
            return new User(
                    json.getString(User.KEY_ID),
                    json.getString(User.KEY_NAME),
                    json.getString(User.KEY_IMAGEURL),
                    json.getInt(User.KEY_ANSWER)
            );
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }
}
