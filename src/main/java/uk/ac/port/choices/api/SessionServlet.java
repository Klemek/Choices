package uk.ac.port.choices.api;

import org.json.JSONObject;
import uk.ac.port.choices.oauth2.Oauth2CallbackServlet;
import uk.ac.port.choices.utils.Logger;
import uk.ac.port.choices.utils.ServletUtils;
import uk.ac.port.choices.utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/session/*")
public class SessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response){
        try {
            Map<String, Runnable> map = new LinkedHashMap<>();
            map.put("GET /api/session", () -> SessionServlet.getSession(request, response));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void getSession(HttpServletRequest request, HttpServletResponse response){
        JSONObject json = new JSONObject();

        for(String key:Oauth2CallbackServlet.SESSION_USER){
            if(request.getSession().getAttribute(key) == null){
                json = new JSONObject();
                break;
            }
            json.put(key, request.getSession().getAttribute(key));
            if (key.equals(Oauth2CallbackServlet.SESSION_USER_EMAIL))
                json.put("admin", Utils.isAdmin((String) request.getSession().getAttribute(key)));
        }

        ServletUtils.sendJSONResponse(response, json);
    }
}
