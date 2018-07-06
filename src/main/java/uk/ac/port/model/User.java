package uk.ac.port.model;

import org.json.JSONObject;

import java.util.Objects;

public class User {

    private final String id;
    private final String name;
    private final String imageUrl;
    private int answer;

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

    JSONObject toJSON(){
        JSONObject output = new JSONObject();
        output.put("id", id);
        output.put("name",name);
        output.put("imageUrl",imageUrl);
        output.put("answer", answer);
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
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", answer=" + answer +
                '}';
    }
}
