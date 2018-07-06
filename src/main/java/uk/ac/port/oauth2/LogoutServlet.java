package uk.ac.port.oauth2;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import uk.ac.port.utils.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "logout", value = "/logout")
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    private static final String REVOKE_ENDPOINT = "https://accounts.google.com/o/oauth2/revoke?token=";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            HttpSession session = req.getSession(false);
            if (session != null) {
                LogoutServlet.invalidateToken(req);
                session.invalidate();
            }
            // rebuild session
            req.getSession();

            resp.sendRedirect("/login");
        } catch (IOException e) {
            Logger.log(e);
            resp.setStatus(500);
        }
    }

    private static void invalidateToken(HttpServletRequest req) throws IOException {
        String token = (String) req.getSession().getAttribute(Oauth2CallbackServlet.SESSION_TOKEN);
        if (token != null && !token.isEmpty()) {
            HttpRequestFactory requestFactory = LogoutServlet.HTTP_TRANSPORT.createRequestFactory();
            GenericUrl url = new GenericUrl(LogoutServlet.REVOKE_ENDPOINT + token);
            HttpRequest request = requestFactory.buildGetRequest(url);
            try {
                request.execute();
            } catch (HttpResponseException e) {
                Logger.log(e);
            }
        }
    }
}