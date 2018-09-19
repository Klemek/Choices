package uk.ac.port.choices.dao;

import uk.ac.port.choices.model.QuestionPack;

public final class TestUtilsDao {

    private TestUtilsDao() {

    }

    public static void deleteAllLang() {
        for (String key : LangDao.listStrings().keySet())
            LangDao.deleteString(key);
    }

    public static void deleteAllQuestionPack() {
        for (QuestionPack questionPack : QuestionPackDao.listQuestionPacks())
            QuestionPackDao.deleteQuestionPack(questionPack);
    }
}
