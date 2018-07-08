package uk.ac.port.choices.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.port.choices.utils.Logger;
import uk.ac.port.choices.utils.Utils;

import java.util.Arrays;
import java.util.Objects;

public class Question {

    //region Keys

    static final String TEXT = "text";
    static final String HINT = "hint";
    static final String ANSWERS = "answers";

    //endregion

    private final String text;
    private final String hint;
    private final String[] answers;

    public Question(String text, String hint, String[] answers) {
        this.text = text;
        this.hint = hint;
        this.answers = answers;
    }

    public String getHint() {
        return hint;
    }

    public String getText() {
        return text;
    }

    public String[] getAnswers() {
        return answers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", hint='" + hint + '\'' +
                ", answers=" + Arrays.toString(answers) +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject output = new JSONObject();
        output.put(Question.TEXT, text);
        output.put(Question.HINT, hint);
        output.put(Question.ANSWERS, new JSONArray(answers));
        return output;
    }

    public static Question fromJSON(String strJSON) {
        try {
            JSONObject json = new JSONObject(strJSON);
            return new Question(
                    json.getString(Question.TEXT),
                    json.getString(Question.HINT),
                    Utils.jArrayToStringList(json.getJSONArray(Question.ANSWERS)).toArray(new String[0])
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
        Question question = (Question) o;
        return Objects.equals(text, question.text) &&
                Objects.equals(hint, question.hint) &&
                Arrays.equals(answers, question.answers);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(text, hint);
        result = 31 * result + Arrays.hashCode(answers);
        return result;
    }
}
