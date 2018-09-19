package uk.ac.port.choices;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.ac.port.choices.api.ApiServletTest;
import uk.ac.port.choices.api.QuestionsServletTest;
import uk.ac.port.choices.api.RoomServletTest;
import uk.ac.port.choices.api.RoomServletTestMock;
import uk.ac.port.choices.dao.DaoUtilsTest;
import uk.ac.port.choices.dao.LangDaoTest;
import uk.ac.port.choices.dao.QuestionPackDaoTest;
import uk.ac.port.choices.dao.RoomDaoTest;
import uk.ac.port.choices.model.QuestionPackTest;
import uk.ac.port.choices.model.QuestionTest;
import uk.ac.port.choices.model.RoomTest;
import uk.ac.port.choices.model.UserTest;
import uk.ac.port.choices.utils.LangNoStoreTest;
import uk.ac.port.choices.utils.LangTest;
import uk.ac.port.choices.utils.ServletUtilsTest;
import uk.ac.port.choices.utils.UtilsTest;

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
