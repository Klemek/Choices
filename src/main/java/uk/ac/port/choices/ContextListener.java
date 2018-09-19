package uk.ac.port.choices;

import fr.klemek.logger.Logger;

import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import uk.ac.port.choices.dao.RoomDao;
import uk.ac.port.choices.utils.Lang;
import uk.ac.port.choices.utils.Utils;

/**
 * Class which listen to server init and closure.
 */
@WebListener
public class ContextListener implements ServletContextListener {

    private static String authCallback;
    private static String appPath;
    private static Level logLevel;

    public ContextListener() {
        super();
    }

    /**
     * Called at the server launch.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ContextListener.loadParameters(sce);
            Logger.init("logging.properties", ContextListener.logLevel);

            Logger.log(Level.INFO, "auth.callback={0}", ContextListener.authCallback);
            Logger.log(Level.INFO, "app.path={0}", ContextListener.appPath);
            Logger.log(Level.INFO, "app.logLevel={0}", ContextListener.logLevel);

            Lang.init(Boolean.parseBoolean(sce.getServletContext().getInitParameter("app.storeLang")));
            Utils.initRandomWords();
            RoomDao.deleteAllRooms();
            Logger.log(Level.INFO, "Server started");
        } catch (Exception e) {
            throw new IllegalStateException("There was an error during initialization", e);
        }
    }

    /**
     * Called at the server closure.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Logger.log(Level.INFO, "Server closed");
    }

    private static void loadParameters(ServletContextEvent sce) {
        ContextListener.authCallback = sce.getServletContext().getInitParameter("auth.callback");
        ContextListener.appPath = sce.getServletContext().getInitParameter("app.path");
        ContextListener.logLevel = Level.parse(sce.getServletContext().getInitParameter("app.logLevel"));
    }

    public static String getAuthCallback() {
        return authCallback;
    }

    public static String getAppPath() {
        return appPath;
    }

}
