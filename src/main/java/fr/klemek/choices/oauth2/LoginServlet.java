package fr.klemek.choices.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import fr.klemek.choices.ContextListener;
import fr.klemek.choices.utils.Utils;
import fr.klemek.logger.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "login", value = "/login")
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

    static final String REDIRECT = "redirect";
    static final String STATE = "state";
    private static final Collection<String> SCOPES = Arrays.asList("email", "profile");
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {

            // prevent request forgery
            String state = new BigInteger(130, new SecureRandom()).toString(32);
            req.getSession().setAttribute(LoginServlet.STATE, state);

            String loginDest = req.getParameter(LoginServlet.REDIRECT);
            req.getSession().setAttribute(LoginServlet.REDIRECT, loginDest == null
                    ? ContextListener.getAppPath() : loginDest);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    LoginServlet.HTTP_TRANSPORT,
                    LoginServlet.JSON_FACTORY,
                    Utils.getString("auth.clientID"),
                    Utils.getString("auth.clientSecret"),
                    LoginServlet.SCOPES)
                    .build();

            // Callback url should be the one registered in Google Developers Console
            String url =
                    flow.newAuthorizationUrl()
                            .setRedirectUri(ContextListener.getAuthCallback())
                            .setState(state)            // Prevent request forgery
                            .build();
            resp.sendRedirect(url);
        } catch (IOException e) {
            Logger.log(e);
            resp.setStatus(500);
        }
    }
}