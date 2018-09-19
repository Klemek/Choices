package fr.klemek.choices.api;

import fr.klemek.choices.TestUtils;
import fr.klemek.choices.dao.QuestionPackDao;
import fr.klemek.choices.dao.RoomDao;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.model.Room;
import fr.klemek.choices.model.User;
import fr.klemek.choices.oauth2.Oauth2CallbackServlet;
import fr.klemek.choices.utils.Utils;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class RoomServletTestMock {

    private Room room;
    private QuestionPack pack;

    @Before
    public void setUp() {
        room = new Room("masterid", "simpleid");
        RoomDao.createRoom(room);
    }

    @After
    public void tearDown() {
        if (room != null && room.getId() != null)
            RoomDao.deleteRoom(room);
        if (pack != null && pack.getId() != null)
            QuestionPackDao.deleteQuestionPack(pack);
    }

    @Test
    public void testSendResultsNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234/results", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testSendResultsForbidden() {
        String userid = "userid";

        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testSendResultsBadRequest1() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        Map<String, String> params = new HashMap<>();
        params.put("datetime", "1536160106079");
        params.put("duration", "4321000");
        params.put("target", "4");
        params.put("packId", "1");
        params.put("users", "[]");
        params.put("questions", "[]");
        params.put("videos", "[]");
        params.put("teachers", "[]");

        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.sendMailToAdmin(anyString(), anyString())).thenReturn(false);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testSendResultsBadRequest2() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);

        Map<String, String> params = new HashMap<>();
        params.put("datetime", "1536160106079");
        params.put("packId", "" + pack.getId());
        params.put("users", "[]");
        params.put("questions", "[]");
        params.put("videos", "[]");
        params.put("teachers", "[]");

        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.sendMailToAdmin(anyString(), anyString())).thenReturn(false);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testSendResultsBadRequest3() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);

        Map<String, String> params = new HashMap<>();
        params.put("datetime", "1536160106079");
        params.put("duration", "4321000");
        params.put("target", "4");
        params.put("packId", "" + pack.getId());
        params.put("users", "[{}]");
        params.put("questions", "[]");
        params.put("videos", "[]");
        params.put("teachers", "[]");

        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.sendMailToAdmin(anyString(), anyString())).thenReturn(false);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testSendResults() throws Exception {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());
        session.put(Oauth2CallbackServlet.SESSION_USER_NAME, "test1");
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, "email1");

        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);

        Map<String, String> params = new HashMap<>();
        params.put("datetime", "1536160106079");
        params.put("duration", "4321000");
        params.put("target", "4");
        params.put("packId", "" + pack.getId());
        params.put("users", "" +
                "[{\"id\":\"1\",\"name\":\"test1\",\"email\":\"email1\",\"score\":6,\"groupTeaching\":2,\"teachingFailed\":1}," +
                "{\"id\":\"2\",\"name\":\"test2\",\"email\":\"email2\",\"score\":5,\"groupTeaching\":1,\"teachingFailed\":0}," +
                "{\"id\":\"3\",\"name\":\"guest3\",\"score\":4,\"groupTeaching\":0,\"teachingFailed\":0}]");
        params.put("questions", "" +
                "[{\"right\" : 1,\"wrong\" : 1,\"unanswered\" : 1,\"score\" : 0}," +
                "{\"right\" : 2,\"wrong\" : 1,\"unanswered\" : 0,\"score\" : 0}," +
                "{\"right\" : 4,\"wrong\" : 0,\"unanswered\" : 0,\"score\" : 1}," +
                "{\"right\" : 4,\"wrong\" : 0,\"unanswered\" : 0,\"score\" : 2}," +
                "{\"right\" : 4,\"wrong\" : 0,\"unanswered\" : 0,\"score\" : 3}," +
                "{\"right\" : 4,\"wrong\" : 0,\"unanswered\" : 0,\"score\" : 4}," +
                "{\"right\" : 4,\"wrong\" : 0,\"unanswered\" : 0,\"score\" : 4}]");
        params.put("videos", "[\"[link]http://link\"]");
        params.put("teachers", "[[\"1\",\"2\"],[]]");

        String expected = "<h2>" + Utils.getString("mail.title") + "</h2>\n" +
                "<ul>\n" +
                "<li>Date : 05/09/18 at 15:08 UTC</li>\n" +
                "<li>Topic : 'name'</li>\n" +
                "<li>Started by : test1 (<a href=\"mailto:email1\">email1</a>)</li>\n" +
                "<li>Target to mastery : 4 questions</li>\n" +
                "<li>Users : 3</li>\n" +
                "<li>Questions : 7</li>\n" +
                "<li>Duration : 1 hour 12 min.</li>\n" +
                "</ul>\n" +
                "<h3>Users</h3>\n" +
                "<table>\n" +
                "<thead><tr><th>Name</th><th>Score</th><th>Group teaching*</th><th>Group teaching failed**</th></tr></thead>\n" +
                "<tbody>\n" +
                "<tr><td>test1</td><td>6</td><td>2</td><td>1</td></tr>\n" +
                "<tr><td>test2</td><td>5</td><td>1</td><td>0</td></tr>\n" +
                "<tr><td>guest3</td><td>4</td><td>0</td><td>0</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<h3>History</h3>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr><th>Step</th><th>Information</th></tr>\n" +
                "</thead>\n" +
                "<tbody><tr><td>Question 1</td><td>Right : 1 / Wrong : 1 / Unanswered : 1 / Remaining : 4</td></tr>\n" +
                "<tr><td>Question 2</td><td>Right : 2 / Wrong : 1 / Unanswered : 0 / Remaining : 4</td></tr>\n" +
                "<tr><td>Teacher</td><td>test1</td></tr>\n" +
                "<tr><td>Teacher</td><td>test2</td></tr>\n" +
                "<tr><td>Question 3</td><td>Right : 4 / Wrong : 0 / Unanswered : 0 / Remaining : 3</td></tr>\n" +
                "<tr><td>Question 4</td><td>Right : 4 / Wrong : 0 / Unanswered : 0 / Remaining : 2</td></tr>\n" +
                "<tr><td>Video</td><td><a href=\"http://link\">link</a></td></tr>\n" +
                "<tr><td>Question 5</td><td>Right : 4 / Wrong : 0 / Unanswered : 0 / Remaining : 1</td></tr>\n" +
                "<tr><td>Question 6</td><td>Right : 4 / Wrong : 0 / Unanswered : 0 / Remaining : 0</td></tr>\n" +
                "<tr><td>Teacher</td><td><i>Skipped</i></td></tr>\n" +
                "<tr><td>Question 7</td><td>Right : 4 / Wrong : 0 / Unanswered : 0 / Remaining : 0</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "* Times user teached after a question<br/>\n" +
                "** Times user failed to teach after a question";

        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.sendMailToAdmin(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));

        PowerMockito.verifyStatic(Utils.class, VerificationModeFactory.times(2));
        Utils.sendMailToAdmin(Utils.getString("mail.title"), expected);
    }

    @Test
    public void testSendResultsError() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);

        Map<String, String> params = new HashMap<>();
        params.put("datetime", "1536160106079");
        params.put("duration", "4321000");
        params.put("target", "4");
        params.put("packId", "" + pack.getId());
        params.put("users", "[]");
        params.put("questions", "[]");
        params.put("videos", "[]");
        params.put("teachers", "[]");

        PowerMockito.spy(Utils.class);
        PowerMockito.when(Utils.sendMailToAdmin(anyString(), anyString())).thenReturn(false);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/results", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(500, res.getInt("code"));
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
    }
}
