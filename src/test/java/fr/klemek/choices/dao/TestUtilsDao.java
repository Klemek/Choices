package fr.klemek.choices.dao;

import fr.klemek.choices.model.QuestionPack;

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
