package uk.ac.port.api;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.ac.port.TestUtils;
import uk.ac.port.oauth2.Oauth2CallbackServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "java.*", "javax.*", "org.*"})
public class SessionServletTest {

    @Test
    public void testServletOptions(){
        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new SessionServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
    }

    @Test
    public void testGetSessionNull() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/session", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new SessionServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);

        assertEquals(200, res.getInt("code"));
        assertEquals(0,res.getJSONObject("value").length());
    }

    @Test
    public void testGetSession() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID,"userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL,"email");
        session.put(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL,"imageurl");
        session.put(Oauth2CallbackServlet.SESSION_USER_NAME,"username");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/session", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new SessionServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertEquals("userid",value.getString(Oauth2CallbackServlet.SESSION_USER_ID));
        assertEquals("email",value.getString(Oauth2CallbackServlet.SESSION_USER_EMAIL));
        assertEquals("imageurl",value.getString(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL));
        assertEquals("username",value.getString(Oauth2CallbackServlet.SESSION_USER_NAME));
    }

}
