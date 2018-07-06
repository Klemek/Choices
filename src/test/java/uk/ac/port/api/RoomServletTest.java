package uk.ac.port.api;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.ac.port.TestUtils;
import uk.ac.port.dao.RoomDao;
import uk.ac.port.model.Question;
import uk.ac.port.model.Room;
import uk.ac.port.model.User;
import uk.ac.port.oauth2.Oauth2CallbackServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"java.*", "javax.*", "org.*"})
public class RoomServletTest {

    private Room room;
    private static RoomDao dao;

    @BeforeClass
    public static void setUpClass() {
        RoomServletTest.dao = new RoomDao();
    }

    @Before
    public void setUp() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", new String[]{"1", "2", "3", "4"}, 2));
        room = new Room(questionList, "masterid");
        RoomServletTest.dao.createRoom(room);
    }

    @After
    public void tearDown() {
        if (room != null)
            RoomServletTest.dao.deleteRoom(room);
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
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/qgqzgq", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);

        assertEquals(401, res.getInt("code"));
    }

    @Test
    public void testCreateRoom() {
        RoomServletTest.dao.deleteRoom(room);

        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/room/create", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertTrue(value.has("id"));
        String simpleId = value.getString("id");

        room = RoomServletTest.dao.getRoomBySimpleId(simpleId);
        assertNotNull(room);

        assertEquals(userid, room.getMasterId());
    }

    @Test
    public void testGetRoomInfo() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/" + room.getSimpleId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertEquals(room.getSimpleId(), value.get("id"));
    }

    @Test
    public void testGetRoomInfoNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testGetRoomInfoForbidden() {
        String userid = "userid";

        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomServletTest.dao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/" + room.getSimpleId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testJoinRoom() {
        String userid = "userid";

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);
        session.put(Oauth2CallbackServlet.SESSION_USER_EMAIL, "email");
        session.put(Oauth2CallbackServlet.SESSION_USER_IMAGE_URL, "imageurl");
        session.put(Oauth2CallbackServlet.SESSION_USER_NAME, "username");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/join", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));

        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().size());
        assertEquals(userid, room.getUsers().get(0).getId());
    }

    @Test
    public void testJoinRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234/join", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testQuitRoomUser() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomServletTest.dao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(0, room.getUsers().size());
    }

    @Test
    public void testQuitRoomMaster() {
        room.getUsers().add(new User("userid", "name", "imageUrl", 0));
        RoomServletTest.dao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertNull(room);
    }

    @Test
    public void testQuitRoomForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testQuitRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/1234/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testAnswerRoomQuestionNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234/answer/1", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testAnswerRoomQuestion() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        room.next(); //answering
        RoomServletTest.dao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/1", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().get(0).getAnswer());
    }

    @Test
    public void testAnswerRoomQuestionBadRequest() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        room.next(); //answering
        RoomServletTest.dao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/A", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(400, res.getInt("code"));
        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(0, room.getUsers().get(0).getAnswer());
    }

    @Test
    public void testAnswerRoomQuestionForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/1", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testNextRoomQuestion() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/next", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomServletTest.dao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(Room.State.ANSWERING, room.getState());
    }

    @Test
    public void testNextRoomQuestionNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234/next", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testNextRoomQuestionForbidden() {
        String userid = "userid";

        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomServletTest.dao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/next", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }
}
