package uk.ac.port;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.ac.port.api.RoomServletTest;
import uk.ac.port.api.SessionServletTest;
import uk.ac.port.dao.RoomDaoTest;
import uk.ac.port.utils.ServletUtilsTest;
import uk.ac.port.utils.UtilsTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UtilsTest.class, ServletUtilsTest.class,
        RoomDaoTest.class,
        SessionServletTest.class, RoomServletTest.class
})
public class AllTests {
}
