package uk.ac.port.choices.dao;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionPackDaoTest {

    private static QuestionPack pack;

    @BeforeClass
    public static void setUp() {
        for (QuestionPack questionPack : QuestionPackDao.listQuestionPacks()) {
            QuestionPackDao.deleteQuestionPack(questionPack);
        }

        Question question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});
        List<Question> questionList = new ArrayList<>();
        questionList.add(question);

        QuestionPackDaoTest.pack = new QuestionPack(1L, "name", questionList);
    }

    @Test
    public void createQuestionPack() {
        Long id = QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack);

        assertNotNull(id);
        assertEquals(id, QuestionPackDaoTest.pack.getId());
        assertNotEquals(0L, (long) QuestionPackDaoTest.pack.getId());

        QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);
    }

    @Test
    public void updateQuestionPack() {
        assertNotNull(QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack));

        QuestionPackDaoTest.pack = new QuestionPack(QuestionPackDaoTest.pack.getId(), "name2", QuestionPackDaoTest.pack.getQuestions());
        QuestionPackDao.updateQuestionPack(QuestionPackDaoTest.pack);

        QuestionPack pack2 = QuestionPackDao.getQuestionPackById(QuestionPackDaoTest.pack.getId());

        assertEquals(QuestionPackDaoTest.pack.getName(), pack2.getName());

        QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);
    }

    @Test
    public void getQuestionPackById() {
        assertNotNull(QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack));

        QuestionPack pack2 = QuestionPackDao.getQuestionPackById(QuestionPackDaoTest.pack.getId());

        assertEquals(QuestionPackDaoTest.pack, pack2);

        QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);
    }

    @Test
    public void deleteQuestionPack() {
        assertNotNull(QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack));

        QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);

        QuestionPack pack2 = QuestionPackDao.getQuestionPackById(QuestionPackDaoTest.pack.getId());

        assertNull(pack2);
    }

    @Test
    public void listQuestionPacks() {
        assertNotNull(QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack));

        List<QuestionPack> lst = QuestionPackDao.listQuestionPacks();

        assertEquals(1, lst.size());
        assertEquals(QuestionPackDaoTest.pack, lst.get(0));

        QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);
    }
}