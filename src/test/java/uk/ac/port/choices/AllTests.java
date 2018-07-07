package uk.ac.port.choices;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.ac.port.choices.api.RoomServletTest;
import uk.ac.port.choices.api.SessionServletTest;
import uk.ac.port.choices.dao.RoomDaoTest;
import uk.ac.port.choices.utils.ServletUtilsTest;
import uk.ac.port.choices.utils.UtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class,
        RoomDaoTest.class,
        SessionServletTest.class, RoomServletTest.class
})
public class AllTests {
}
