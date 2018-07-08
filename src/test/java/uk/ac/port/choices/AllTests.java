package uk.ac.port.choices;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.ac.port.choices.api.QuestionsServletTest;
import uk.ac.port.choices.api.RoomServletTest;
import uk.ac.port.choices.api.SessionServletTest;
import uk.ac.port.choices.dao.QuestionPackDaoTest;
import uk.ac.port.choices.dao.RoomDaoTest;
import uk.ac.port.choices.model.QuestionPackTest;
import uk.ac.port.choices.model.QuestionTest;
import uk.ac.port.choices.model.RoomTest;
import uk.ac.port.choices.model.UserTest;
import uk.ac.port.choices.utils.ServletUtilsTest;
import uk.ac.port.choices.utils.UtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class,
        RoomTest.class, QuestionTest.class, UserTest.class, QuestionPackTest.class,
        RoomDaoTest.class, QuestionPackDaoTest.class,
        SessionServletTest.class, RoomServletTest.class, QuestionsServletTest.class
})
public class AllTests {
}
