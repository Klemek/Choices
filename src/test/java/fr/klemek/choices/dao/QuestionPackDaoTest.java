package fr.klemek.choices.dao;

import fr.klemek.choices.TestUtils;
import fr.klemek.choices.model.QuestionPack;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QuestionPackDaoTest {

    private static QuestionPack pack;

    @Test
    public void createQuestionPack() {
        Long id = QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack);

        assertNotNull(id);
        assertEquals(id, QuestionPackDaoTest.pack.getId());
        assertNotEquals(0L, (long) QuestionPackDaoTest.pack.getId());

        //QuestionPackDao.deleteQuestionPack(QuestionPackDaoTest.pack);
    }

    @Test
    public void updateQuestionPack() {
        assertNotNull(QuestionPackDao.createQuestionPack(QuestionPackDaoTest.pack));

        QuestionPackDaoTest.pack = new QuestionPack(QuestionPackDaoTest.pack.getId(), "name2", "video2", "message2", true, QuestionPackDaoTest.pack.getQuestions());
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

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));

        TestUtilsDao.deleteAllQuestionPack();

        QuestionPackDaoTest.pack = new QuestionPack(1L, "name", "video", "message", true, Arrays.asList(TestUtils.question));
    }
}