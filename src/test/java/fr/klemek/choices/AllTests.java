package fr.klemek.choices;

import fr.klemek.choices.api.ApiServletTest;
import fr.klemek.choices.api.QuestionsServletTest;
import fr.klemek.choices.api.RoomServletTest;
import fr.klemek.choices.api.RoomServletTestMock;
import fr.klemek.choices.dao.DaoUtilsTest;
import fr.klemek.choices.dao.LangDaoTest;
import fr.klemek.choices.dao.QuestionPackDaoTest;
import fr.klemek.choices.dao.RoomDaoTest;
import fr.klemek.choices.model.QuestionPackTest;
import fr.klemek.choices.model.QuestionTest;
import fr.klemek.choices.model.RoomTest;
import fr.klemek.choices.model.UserTest;
import fr.klemek.choices.utils.LangNoStoreTest;
import fr.klemek.choices.utils.LangTest;
import fr.klemek.choices.utils.ServletUtilsTest;
import fr.klemek.choices.utils.UtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class,
        RoomTest.class, QuestionTest.class, UserTest.class, QuestionPackTest.class,
        DaoUtilsTest.class, RoomDaoTest.class, QuestionPackDaoTest.class, LangDaoTest.class,
        LangTest.class, LangNoStoreTest.class,
        ApiServletTest.class, RoomServletTest.class, RoomServletTestMock.class, QuestionsServletTest.class
})
public class AllTests {
}
