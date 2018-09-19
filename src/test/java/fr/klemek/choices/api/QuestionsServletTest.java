package fr.klemek.choices.api;

import fr.klemek.choices.TestUtils;
import fr.klemek.choices.dao.QuestionPackDao;
import fr.klemek.choices.dao.TestUtilsDao;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.oauth2.Oauth2CallbackServlet;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QuestionsServletTest {


    private QuestionPack pack;

    @Before
    public void setUp() {
        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);
    }

    @After
    public void tearDown() {
        if (pack != null && pack.getId() != null)
            QuestionPackDao.deleteQuestionPack(pack);
    }

    @Test
    public void testServletOptions() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("OPTIONS", "", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new RoomServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
    }

    @Test
    public void testNoSession() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/qgqzgq", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(401, res.getInt("code"));
    }

    @Test
    public void testListPack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        //add disabled pack
        QuestionPack pack2 = new QuestionPack("name2", "video", "message", false, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack2);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/list", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        JSONArray list = res.getJSONArray("value");
        assertEquals(1, list.length());
        JSONObject json = list.getJSONObject(0);
        assertEquals((long) pack.getId(), json.getLong("id"));
        assertEquals(pack.getName(), json.getString("name"));
        assertEquals(1, json.getInt("questionCount"));

        QuestionPackDao.deleteQuestionPack(pack2);
    }

    @Test
    public void testListPackFullForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/all", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testListPackFull() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/all", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        JSONArray list = res.getJSONArray("value");
        assertEquals(1, list.length());
        JSONObject json = list.getJSONObject(0);
        assertEquals((long) pack.getId(), json.getLong("id"));
        assertEquals(pack.getName(), json.getString("name"));
        assertTrue(json.has("questions"));
    }

    @Test
    public void testCreatePackForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/questions/create", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testCreatePackBadRequest() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/questions/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testCreatePack() {
        QuestionPackDao.deleteQuestionPack(pack);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());
        params.put("video", pack.getVideo());
        params.put("message", pack.getMessage());
        params.put("enabled", "" + pack.isEnabled());
        params.put("questions", String.format("[%s]", pack.getQuestions().get(0).toJson()));

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/questions/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));

        JSONObject value = res.getJSONObject("value");

        assertTrue(value.has("id"));
        Long id = value.getLong("id");
        pack = QuestionPackDao.getQuestionPackById(id);
        assertNotNull(pack);
        assertEquals(1, pack.getQuestions().size());
    }

    @Test
    public void testGetPackDetailsForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testGetPackDetails() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/" + pack.getId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        assertEquals(pack.getName(), res.getJSONObject("value").getString("name"));
    }

    @Test
    public void testGetPackDetailsNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testUpdatePackForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testUpdatePackNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testUpdatePackBadRequest() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/" + pack.getId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testUpdatePack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", "name2");
        params.put("video", "video2");
        params.put("message", "message2");
        params.put("enabled", "false");
        params.put("questions", String.format("[%s]", pack.getQuestions().get(0).toJson()));

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/" + pack.getId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));

        pack = QuestionPackDao.getQuestionPackById(pack.getId());
        assertEquals("name2", pack.getName());
        assertEquals("video2", pack.getVideo());
        assertEquals("message2", pack.getMessage());
        assertFalse(pack.isEnabled());
    }

    @Test
    public void testDeletePackForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testDeletePackNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testDeletePack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, TestUtils.ADMIN_EMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/" + pack.getId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));

        assertNull(QuestionPackDao.getQuestionPackById(pack.getId()));
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
        TestUtilsDao.deleteAllQuestionPack();
    }
}
