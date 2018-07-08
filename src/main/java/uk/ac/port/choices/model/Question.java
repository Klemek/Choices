package uk.ac.port.choices.model;

import java.util.Arrays;
import java.util.Objects;

public class Question {

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
