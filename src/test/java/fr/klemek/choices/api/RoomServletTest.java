package fr.klemek.choices.api;

import fr.klemek.choices.TestUtils;
import fr.klemek.choices.dao.QuestionPackDao;
import fr.klemek.choices.dao.RoomDao;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.model.Room;
import fr.klemek.choices.model.User;
import fr.klemek.choices.oauth2.Oauth2CallbackServlet;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RoomServletTest {

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
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/qgqzgq", null, null, null);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);

        assertEquals(401, res.getInt("code"));
    }

    @Test
    public void testCreateRoom() {
        RoomDao.deleteRoom(room);

        pack = new QuestionPack("name", "video", "message", true, Arrays.asList(TestUtils.question));
        QuestionPackDao.createQuestionPack(pack);

        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        Map<String, String> params = new HashMap<>();
        params.put("packId", pack.getId().toString());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/room/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertTrue(value.has("id"));
        String simpleId = value.getString("id");
        assertTrue(value.has("pack"));
        assertEquals(pack.getName(), value.getJSONObject("pack").get("name"));

        room = RoomDao.getRoomBySimpleId(simpleId);
        assertNotNull(room);

        assertEquals(userid, room.getMasterId());
    }

    @Test
    public void testCreateRoomBadRequest() {
        RoomDao.deleteRoom(room);

        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        Map<String, String> params = new HashMap<>();
        params.put("packId", "1234");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("PUT", "/api/room/create", params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
    }

    @Test
    public void testGetRoomInfo() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/" + room.getSimpleId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
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

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testGetRoomInfoForbidden() {
        String userid = "userid";

        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("GET", "/api/room/" + room.getSimpleId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
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

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));

        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().size());
        assertEquals(userid, room.getUsers().get(0).getId());
    }

    @Test
    public void testJoinRoomClosed() {
        room.setLocked(true);
        RoomDao.updateRoom(room);

        String userid = "userid";

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/join", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testJoinRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234/join", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testQuitRoomUser() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(0, room.getUsers().size());
    }

    @Test
    public void testQuitRoomForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/quit", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
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

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testDeleteRoom() {
        room.getUsers().add(new User("userid", "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/delete", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertNull(room);
    }

    @Test
    public void testDeleteRoomForbidden() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/delete", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testDeleteRoomNotFound() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/12345/delete", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testKickFromRoom() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/kick/" + userid, null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(0, room.getUsers().size());
    }

    @Test
    public void testKickFromRoomForbidden() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/kick/" + userid, null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testKickFromRoomNotFound() {
        String userid = "userid";

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/12345/kick/" + userid, null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
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

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testAnswerRoomQuestion() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        room.resetAnswers(); //answering
        RoomDao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/1", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().get(0).getAnswer());
    }

    @Test
    public void testAnswerRoomQuestionBadRequest() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/A", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(400, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
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

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testUpdateRoomResetAnswers() {
        String userid = "userid";
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());
        room.getUsers().add(new User(userid, "name", "imageUrl", 1));
        RoomDao.updateRoom(room);
        assertEquals(1, room.getUsers().get(0).getAnswer());

        Map<String, String> params = new HashMap<>();
        params.put(Room.KEY_RESET, "true");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(0, room.getUsers().get(0).getAnswer());
    }

    @Test
    public void testUpdateRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/1234", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testUpdateRoomForbidden() {
        String userid = "userid";

        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testLockRoom() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        Map<String, String> params = new HashMap<>();
        params.put(Room.KEY_LOCK, "true");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertTrue(room.isLocked());
    }

    @Test
    public void testUnlockRoom() {
        room.setLocked(true);
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        Map<String, String> params = new HashMap<>();
        params.put(Room.KEY_LOCK, "false");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertFalse(room.isLocked());
    }

    @Test
    public void testLockAnswers() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        Map<String, String> params = new HashMap<>();
        params.put(Room.KEY_LOCK_ANSWERS, "true");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertTrue(room.areAnswersLocked());
    }

    @Test
    public void testUnlockAnswers() {
        room.setAnswersLocked(true);
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        Map<String, String> params = new HashMap<>();
        params.put(Room.KEY_LOCK_ANSWERS, "false");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId(), params, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJson(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertFalse(room.areAnswersLocked());
    }

    @BeforeClass
    public static void setUpClass() {
        assertTrue(TestUtils.prepareTestClass(true));
    }
}
