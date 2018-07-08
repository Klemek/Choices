package uk.ac.port.choices.api;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.ac.port.choices.TestUtils;
import uk.ac.port.choices.dao.QuestionPackDao;
import uk.ac.port.choices.dao.RoomDao;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.oauth2.Oauth2CallbackServlet;

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
    private QuestionPack pack;

    @Before
    public void setUp() {
        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", "hint", new String[]{"2", "1", "3", "4"}));
        room = new Room(questionList, "masterid");
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
        RoomDao.deleteRoom(room);

        Question question = new Question("What is 1+1", "hint", new String[]{"1", "2", "3", "4"});
        List<Question> questionList = new ArrayList<>();
        questionList.add(question);

        pack = new QuestionPack("name", questionList);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        JSONObject value = res.getJSONObject("value");

        assertTrue(value.has("id"));
        String simpleId = value.getString("id");

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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        RoomDao.updateRoom(room);

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

        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().size());
        assertEquals(userid, room.getUsers().get(0).getId());
    }

    @Test
    public void testJoinRoomClosed() {
        room.setLock(true);
        RoomDao.updateRoom(room);

        String userid = "userid";

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/join", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
    public void testDeleteRoom() {
        room.getUsers().add(new User("userid", "name", "imageUrl", 0));
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("DELETE", "/api/room/" + room.getSimpleId() + "/delete", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        RoomDao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/1", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertEquals(1, room.getUsers().get(0).getAnswer());
    }

    @Test
    public void testAnswerRoomQuestionBadRequest() {
        String userid = "userid";
        room.getUsers().add(new User(userid, "name", "imageUrl", 0));
        room.next(); //answering
        RoomDao.updateRoom(room);
        assertEquals(0, room.getUsers().get(0).getAnswer());

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/answer/A", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
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
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
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
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, userid);

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/next", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testLockRoom() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/lock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertTrue(room.isLocked());
    }

    @Test
    public void testLockRoomForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/lock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testLockRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/12345/lock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }

    @Test
    public void testUnlockRoom() {
        room.setLock(true);
        RoomDao.updateRoom(room);

        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, room.getMasterId());

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/unlock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(200, res.getInt("code"));
        room = RoomDao.getRoomBySimpleId(room.getSimpleId());
        assertFalse(room.isLocked());
    }

    @Test
    public void testUnlockRoomForbidden() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/" + room.getSimpleId() + "/unlock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(403, res.getInt("code"));
    }

    @Test
    public void testUnlockRoomNotFound() {
        Map<String, Object> session = new HashMap<>();
        session.put(Oauth2CallbackServlet.SESSION_USER_ID, "userid");

        StringWriter writer = new StringWriter();
        HttpServletRequest request = TestUtils.createMockRequest("POST", "/api/room/12345/unlock", null, null, session);
        HttpServletResponse response = TestUtils.createMockResponse(writer);

        new RoomServlet().service(request, response);

        JSONObject res = TestUtils.getResponseAsJSON(writer);
        assertEquals(404, res.getInt("code"));
    }
}
