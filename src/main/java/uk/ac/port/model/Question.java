package uk.ac.port.model;

import java.util.Arrays;
import java.util.Objects;

public class Question {

    private final String text;
    private final String[] answers;
    private final int correctAnswer;

    public Question(String text, String[] answers, int correctAnswer) {
        this.text = text;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
    }

    public String getText() {
        return text;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public String[] getAnswers() {
        return answers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", answers=" + Arrays.toString(answers) +
                ", correctAnswer=" + correctAnswer +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return correctAnswer == question.correctAnswer &&
                Objects.equals(text, question.text) &&
                Arrays.equals(answers, question.answers);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(text, correctAnswer);
        result = 31 * result + Arrays.hashCode(answers);
        return result;
    }
}
