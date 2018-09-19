/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.klemek.choices.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import fr.klemek.choices.ContextListener;
import fr.klemek.choices.utils.Utils;
import fr.klemek.logger.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "oauth2callback", value = "/oauth2callback")
@SuppressWarnings("serial")
public class Oauth2CallbackServlet extends HttpServlet {

    public static final String SESSION_USER_EMAIL = "userEmail";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_IMAGE_URL = "userImageUrl";
    public static final String SESSION_USER_NAME = "userName";
    public static final List<String> SESSION_USER = Collections.unmodifiableList(Arrays.asList(
            Oauth2CallbackServlet.SESSION_USER_EMAIL,
            Oauth2CallbackServlet.SESSION_USER_ID,
            Oauth2CallbackServlet.SESSION_USER_IMAGE_URL,
            Oauth2CallbackServlet.SESSION_USER_NAME));
    static final String SESSION_TOKEN = "token";
    private static final Collection<String> SCOPES = Arrays.asList("email", "profile");
    private static final String USERINFO_ENDPOINT
            = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {

        try {
            // Ensure that this is no request forgery going on, and that the user
            // sending us this connect request is the user that was supposed to.
            if (req.getSession().getAttribute(LoginServlet.STATE) == null
                    || !req.getParameter(LoginServlet.STATE)
                    .equals(req.getSession().getAttribute(LoginServlet.STATE))) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.sendRedirect(ContextListener.getAppPath() + "/login");
                return;
            }

            req.getSession().removeAttribute(LoginServlet.STATE);     // Remove one-time use state.

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    Oauth2CallbackServlet.HTTP_TRANSPORT,
                    Oauth2CallbackServlet.JSON_FACTORY,
                    Utils.getString("auth.clientID"),
                    Utils.getString("auth.clientSecret"),
                    Oauth2CallbackServlet.SCOPES).build();

            TokenResponse tokenResponse =
                    flow.newTokenRequest(req.getParameter("code"))
                            .setRedirectUri(ContextListener.getAuthCallback())
                            .execute();

            req.getSession().setAttribute(Oauth2CallbackServlet.SESSION_TOKEN,
                    tokenResponse.getAccessToken()); // Keep track of the token.
            Credential credential = flow.createAndStoreCredential(tokenResponse, null);
            HttpRequestFactory requestFactory = Oauth2CallbackServlet.HTTP_TRANSPORT
                    .createRequestFactory(credential);

            // Make an authenticated request.
            GenericUrl url = new GenericUrl(Oauth2CallbackServlet.USERINFO_ENDPOINT);
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().setContentType("application/json");

            String jsonIdentity = request.execute().parseAsString();

            @SuppressWarnings("unchecked")
            HashMap<String, String> userIdResult =
                    new ObjectMapper().readValue(jsonIdentity, HashMap.class);
            // From this map, extract the relevant profile info and store it in the session.
            req.getSession().setAttribute(Oauth2CallbackServlet.SESSION_USER_EMAIL,
                    userIdResult.get("email"));
            req.getSession().setAttribute(Oauth2CallbackServlet.SESSION_USER_ID,
                    userIdResult.get("sub"));
            req.getSession().setAttribute(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL,
                    userIdResult.get("picture"));
            req.getSession().setAttribute(Oauth2CallbackServlet.SESSION_USER_NAME,
                    userIdResult.get("name"));
            resp.sendRedirect((String) req.getSession().getAttribute(LoginServlet.REDIRECT));
        } catch (IOException e) {
            Logger.log(e);
            resp.setStatus(500);
        }
    }
}
