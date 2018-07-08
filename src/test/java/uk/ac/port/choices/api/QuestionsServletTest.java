package uk.ac.port.choices.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.dao.QuestionPackDao;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.oauth2.Oauth2CallbackServlet;
import uk.ac.port.choices.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"java.*", "javax.*", "org.*"})
@PrepareForTest({Utils.class})
public class QuestionsServletTest {

    private static final String ADMINEMAIL = "adminemail";
    private QuestionPack pack;

    @Before
    public void setUp() {
        PowerMockito.spy(Utils.class);
        when(Utils.isAdmin(anyString())).thenReturn(false);
        when(Utils.isAdmin(eq(QuestionsServletTest.ADMINEMAIL))).thenReturn(true);

        Question question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});
        List<Question> questionList = new ArrayList<>();
        questionList.add(question);

        pack = new QuestionPack("name", questionList);

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
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
    }

    @Test
    public void testNoSession() {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/qgqzgq", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);

        assertEquals(401, res.getInt("code"));
    }

    @Test
    public void testListPack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/list", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        JSONArray list = res.getJSONArray("value");
        assertEquals(1, list.length());
        JSONObject json = list.getJSONObject(0);
        assertEquals((long) pack.getId(), json.getLong("id"));
        assertEquals(pack.getName(), json.getString("name"));
    }

    @Test
    public void testListPackFullForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/all", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testListPackFull() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/all", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testCreatePackBadRequest() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/questions/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testCreatePack() {
        QuestionPackDao.deleteQuestionPack(pack);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());
        params.put("questions", String.format("[%s]", pack.getQuestions().get(0).toJSON()));

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/questions/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testGetPackDetails() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/" + pack.getId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        assertEquals(pack.getName(), res.getJSONObject("value").getString("name"));
    }

    @Test
    public void testGetPackDetailsNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testUpdatePackNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testUpdatePackBadRequest() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", pack.getName());

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/" + pack.getId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testUpdatePack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        Map<String, String> params = new HashMap<>();
        params.put("name", "name2");
        params.put("questions", String.format("[%s]", pack.getQuestions().get(0).toJSON()));

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/questions/" + pack.getId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));

        pack = QuestionPackDao.getQuestionPackById(pack.getId());
        assertEquals("name2", pack.getName());
    }

    @Test
    public void testDeletePackForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testDeletePackNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testDeletePack() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, QuestionsServletTest.ADMINEMAIL);

        StringWriter writer = new StringWriter();

        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/questions/" + pack.getId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);
        new QuestionsServlet().service(request, response);
        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));

        assertNull(QuestionPackDao.getQuestionPackById(pack.getId()));
    }
}
