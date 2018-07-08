package uk.ac.port.choices.model;

import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.port.choices.utils.Logger;

import java.util.Objects;

public class User {

    //region keys

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGEURL = "imageUrl";
    public static final String ANSWER = "answer";

    //endregion

    private final String id;
    private final String name;
    private final String imageUrl;
    private int answer;

    public User(String id) {
        this(id, null, null);
    }

    public User(String id, String name, String imageUrl) {
        this(id,name,imageUrl,0);
    }

    public User(String id, String name, String imageUrl,int answer) {
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

    public JSONObject toJSON() {
        JSONObject output = new JSONObject();
        output.put(User.ID, id);
        output.put(User.NAME, name);
        output.put(User.IMAGEURL, imageUrl);
        output.put(User.ANSWER, answer);
        return output;
    }

    public static User fromJSON(String strJSON) {
        try {
            JSONObject json = new JSONObject(strJSON);
            return new User(
                    json.getString(User.ID),
                    json.getString(User.NAME),
                    json.getString(User.IMAGEURL),
                    json.getInt(User.ANSWER)
            );
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
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
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", answer=" + answer +
                '}';
    }
}
