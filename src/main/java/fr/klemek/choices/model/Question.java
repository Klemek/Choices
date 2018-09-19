package fr.klemek.choices.model;

import fr.klemek.choices.utils.Utils;
import fr.klemek.logger.Logger;

import java.util.Arrays;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Question {

    //region Keys

    static final String KEY_TEXT = "text";
    static final String KEY_ANSWERS = "answers";
    static final String KEY_LINKS = "links";

    //endregion

    private final String text;
    private final String[] answers;
    private final String[] links;

    /**
     * Create a new question.
     *
     * @param text    the desired text
     * @param answers an array of string answers, the first being right
     * @param links links associated to each answers
     */
    public Question(String text, String[] answers, String[] links) {
        this.text = text;
        this.answers = answers;
        this.links = links;
    }

    public String getText() {
        return text;
    }

    public String[] getAnswers() {
        return answers;
    }

    public String[] getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "Question{"
                + "text='" + text + '\''
                + ", answers=" + Arrays.toString(answers)
                + ", links=" + Arrays.toString(links)
                + '}';
    }

    /**
     * Return a JSONObject representing this object.
     *
     * @return a JSONObject representing this object
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put(Question.KEY_TEXT, text);
        output.put(Question.KEY_ANSWERS, new JSONArray(answers));
        output.put(Question.KEY_LINKS, new JSONArray(links));
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(text, question.text)
                && Arrays.equals(answers, question.answers)
                && Arrays.equals(links, question.links);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(text);
        result = 31 * result + Arrays.hashCode(answers) + Arrays.hashCode(links);
        return result;
    }

    /**
     * Recreate a Question from it JSON value.
     *
     * @param strJson the JSON value as String
     * @return the recreated Question or null on error
     */
    public static Question fromJson(String strJson) {
        try {
            JSONObject json = new JSONObject(strJson);
            return new Question(
                    json.getString(Question.KEY_TEXT),
                    Utils.jarrayToList(json.getJSONArray(Question.KEY_ANSWERS))
                            .toArray(new String[0]),
                    Utils.jarrayToList(json.getJSONArray(Question.KEY_LINKS))
                            .toArray(new String[0])
            );
        } catch (JSONException e) {
            Logger.log(e);
            return null;
        }
    }
}
