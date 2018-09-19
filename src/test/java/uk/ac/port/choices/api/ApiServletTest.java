package uk.ac.port.choices.api;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.dao.TestUtilsDao;
import uk.ac.port.choices.oauth2.Oauth2CallbackServlet;
import uk.ac.port.choices.utils.Lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiServletTest {

    @Test
    public void testServletOptions() {
        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
    }

    @Test
    public void testGetInfosNoSession() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new ApiServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(200, res.getInt("code"));
        assertEquals(2, res.getJSONObject("value").length());
        assertTrue(res.getJSONObject("value").has("langHash"));
        assertTrue(res.getJSONObject("value").has("appPath"));
    }

    @Test
    public void testGetInfos() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, "email");
        session.put(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL, "imageurl");
        session.put(Oauth2CallbackServlet.SESSION_USER_NAME, "username");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new ApiServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertEquals("userid", value.getString(Oauth2CallbackServlet.SESSION_USER_ID));
        assertEquals("email", value.getString(Oauth2CallbackServlet.SESSION_USER_EMAIL));
        assertEquals("imageurl", value.getString(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL));
        assertEquals("username", value.getString(Oauth2CallbackServlet.SESSION_USER_NAME));
        assertTrue(res.getJSONObject("value").has("langHash"));
        assertTrue(res.getJSONObject("value").has("appPath"));
        assertFalse(value.getBoolean("admin"));
    }

    @Test
    public void testGetLang() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/lang", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        assertEquals(Lang.getHashCode(), res.getJSONObject("value").getInt("hash"));
        JSONObject json = res.getJSONObject("value").getJSONObject("lang");
        assertEquals(1, json.length());
        assertEquals(TestUtils.langEntry.getValue(), json.getString(TestUtils.langEntry.getKey()));
    }

    @Test
    public void testUpdateLangForbidden() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/lang", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testUpdateLangBadRequest() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/lang", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testUpdateLangBadRequest2() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("lang", "");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/lang", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testUpdateLang() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        JSONObject json = new JSONObject();
        json.put(TestUtils.langEntry.getKey(), TestUtils.langEntry.getKey());

        Map<String, String> params = new HashMap<>();
        params.put("lang", json.toString());

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/lang", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new ApiServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        assertEquals(Lang.getHashCode(), res.getJSONObject("value").getInt("hash"));
        //from response
        json = res.getJSONObject("value").getJSONObject("lang");
        assertEquals(1, json.length());
        assertEquals(TestUtils.langEntry.getKey(), json.getString(TestUtils.langEntry.getKey()));
        assertTrue(res.getJSONObject("value").has("hash"));

        //from memory
        json = Lang.toJson();
        assertEquals(TestUtils.langEntry.getKey(), json.getString(TestUtils.langEntry.getKey()));

        TestUtilsDao.deleteAllLang();
        Lang.init(true);
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
        TestUtilsDao.deleteAllLang();
        Lang.init(true);
    }

}
