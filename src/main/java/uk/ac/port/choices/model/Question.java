package uk.ac.port.choices.model;

import java.util.Arrays;
import java.util.Objects;

public class Question {

    private final String text;
    private final String[] answers;

    public Question(String text, String[] answers) {
        this.text = text;
        this.answers = answers;
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
                ", answers=" + Arrays.toString(answers) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(text, question.text) &&
                Arrays.equals(answers, question.answers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(text);
        result = 31 * result + Arrays.hashCode(answers);
        return result;
    }
}
