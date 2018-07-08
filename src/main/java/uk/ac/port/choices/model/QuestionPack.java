package uk.ac.port.choices.model;

import fr.klemek.betterlists.BetterArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class QuestionPack {

    //region Keys

    //Entity

    static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_QUESTIONS = "questions";

    //endregion

    private Long id;
    private final String name;
    private final List<Question> questions;

    public QuestionPack(String name, List<Question> questions) {
        this(null, name, questions);
    }

    public QuestionPack(Long id, String name, List<Question> questions) {
        this.id = id;
        this.name = name;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public JSONObject toJSON() {
        JSONObject output = new JSONObject();
        output.put(QuestionPack.KEY_ID, id);
        output.put(QuestionPack.KEY_NAME, name);
        output.put(QuestionPack.KEY_QUESTIONS, new JSONArray(BetterArrayList.fromList(questions).select(Question::toJSON)));
        return output;
    }

    @Override
    public String toString() {
        return "QuestionPack{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", questions=" + questions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionPack that = (QuestionPack) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(questions, that.questions);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, questions);
    }
}
