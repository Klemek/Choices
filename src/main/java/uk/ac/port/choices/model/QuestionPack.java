package uk.ac.port.choices.model;

import fr.klemek.betterlists.BetterArrayList;

import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuestionPack {

    //region Keys

    public static final String MODEL_KIND = "QuestionPack3";

    //Entity

    public static final String KEY_NAME = "name";
    public static final String KEY_QUESTIONS = "questions";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_VIDEO = "video";
    public static final String KEY_QUESTION_COUNT = "questionCount";
    public static final String KEY_ENABLED = "enabled";

    //JSON
    static final String KEY_ID = "id";
    //endregion
    private final String name;
    private final String message;
    private final String video;
    private final List<Question> questions;
    private final boolean enabled;
    private Long id;

    public QuestionPack(String name, String video, String message, boolean enabled, List<Question> questions) {
        this(null, name, video, message, enabled, questions);
    }

    /**
     * Create a new question pack.
     *
     * @param id        the datastore id
     * @param name      the name
     * @param video     the associated video
     * @param message   the message to show at the end
     * @param questions a list of Question
     */
    public QuestionPack(Long id, String name, String video, String message, boolean enabled, List<Question> questions) {
        this.id = id;
        this.name = name;
        this.video = video;
        this.message = message;
        this.enabled = enabled;
        this.questions = questions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getVideo() {
        return video;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Return a JSONObject representing this object.
     *
     * @return a JSONObject representing this object
     */
    public JSONObject toJson() {
        return toJson(true);
    }

    /**
     * Return a JSONObject representing this object.
     *
     * @param full all details
     * @return a JSONObject representing this object
     */
    public JSONObject toJson(boolean full) {
        JSONObject output = new JSONObject();
        output.put(QuestionPack.KEY_ID, id);
        output.put(QuestionPack.KEY_NAME, name);
        if (full) {
            output.put(QuestionPack.KEY_ENABLED, enabled);
            output.put(QuestionPack.KEY_VIDEO, video);
            output.put(QuestionPack.KEY_MESSAGE, message);
            output.put(QuestionPack.KEY_QUESTIONS, new JSONArray(BetterArrayList
                    .fromList(questions).select(Question::toJson)));
        } else {
            output.put(QuestionPack.KEY_QUESTION_COUNT, questions.size());
        }
        return output;
    }

    @Override
    public String toString() {
        return "QuestionPack{" +
                "name='" + name + '\'' +
                ", message='" + message + '\'' +
                ", video='" + video + '\'' +
                ", questions=" + questions +
                ", enabled=" + enabled +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionPack that = (QuestionPack) o;
        return enabled == that.enabled &&
                Objects.equals(name, that.name) &&
                Objects.equals(message, that.message) &&
                Objects.equals(video, that.video) &&
                Objects.equals(questions, that.questions) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, message, video, questions, enabled, id);
    }
}
