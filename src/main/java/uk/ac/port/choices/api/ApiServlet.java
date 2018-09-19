package uk.ac.port.choices.api;

import fr.klemek.logger.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import uk.ac.port.choices.ContextListener;
import uk.ac.port.choices.oauth2.Oauth2CallbackServlet;
import uk.ac.port.choices.utils.Lang;
import uk.ac.port.choices.utils.ServletUtils;
import uk.ac.port.choices.utils.Utils;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Runnable> map = new LinkedHashMap<>();
            map.put("GET /api", () -> ApiServlet.getInfos(request, response));
            map.put("GET /api/lang", () -> ApiServlet.getLang(response));
            map.put("POST /api/lang", () -> ApiServlet.updateLang(request, response));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api .
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void getInfos(HttpServletRequest request, HttpServletResponse response) {
        JSONObject json = new JSONObject();

        for (String key : Oauth2CallbackServlet.SESSION_USER) {
            if (request.getSession().getAttribute(key) == null) {
                json = new JSONObject();
                break;
            }
            json.put(key, request.getSession().getAttribute(key));
            if (key.equals(Oauth2CallbackServlet.SESSION_USER_EMAIL))
                json.put("admin", Utils.isAdmin((String) request.getSession().getAttribute(key)));
        }

        json.put("langHash", Lang.getHashCode());
        json.put("appPath", ContextListener.getAppPath());

        ServletUtils.sendJsonResponse(response, json);
    }

    /**
     * GET /api/lang/hash .
     *
     * @param response the HttpServletResponse
     */
    private static void getLang(HttpServletResponse response) {
        JSONObject json = new JSONObject();
        json.put("hash", Lang.getHashCode());
        json.put("lang", Lang.toJson());
        ServletUtils.sendJsonResponse(response, json);
    }

    /**
     * POST /api/lang .
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void updateLang(HttpServletRequest request, HttpServletResponse response) {
        if (isForbidden(request, response))
            return;
        String strJson = request.getParameter("lang");
        if (strJson == null || !Lang.update(strJson)) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ApiServlet.getLang(response); //send response
    }

    private static boolean isForbidden(HttpServletRequest request, HttpServletResponse response) {
        if (!ServletUtils.isUserAdmin(request)) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }
}
