package uk.ac.port.choices.api;

import fr.klemek.betterlists.BetterArrayList;
import uk.ac.port.choices.dao.RoomDao;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.model.User;
import uk.ac.port.choices.utils.Logger;
import uk.ac.port.choices.utils.ServletUtils;
import uk.ac.port.choices.utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/room/*")
public class RoomServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userId = RoomServlet.getUser(request, response);
            if (userId == null)
                return;
            Map<String, Runnable> map = new LinkedHashMap<>();
            map.put("PUT /api/room/create", () -> RoomServlet.createRoom(userId, request, response));
            map.put("GET /api/room/{}", () -> RoomServlet.getRoomInfo(userId, request, response));
            map.put("POST /api/room/{}/join", () -> RoomServlet.joinRoom(request, response));
            map.put("DELETE /api/room/{}/quit", () -> RoomServlet.quitRoom(userId, request, response));
            map.put("DELETE /api/room/{}/delete", () -> RoomServlet.deleteRoom(userId, request, response));
            map.put("DELETE /api/room/{}/kick/{}", () -> RoomServlet.kickFromRoom(userId, request, response));
            map.put("POST /api/room/{}/answer/{}", () -> RoomServlet.answerRoomQuestion(userId, request, response));
            map.put("POST /api/room/{}/next", () -> RoomServlet.nextRoomQuestion(userId, request, response));
            map.put("POST /api/room/{}/lock", () -> RoomServlet.lockRoom(userId, request, response, true));
            map.put("POST /api/room/{}/unlock", () -> RoomServlet.lockRoom(userId, request, response, false));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/room/create
     */
    private static void createRoom(String userId, HttpServletRequest request, HttpServletResponse response) {
        // Map<String, String> params = ServletUtils.readParameters(request); //because of PUT request

        List<Question> questionList = new ArrayList<>();
        questionList.add(new Question("What is 1+1", "Use addition", new String[]{"2", "1", "3", "4"}));
        questionList.add(new Question("What is 2*2", "Use multiplication", new String[]{"4", "2", "3", "1"}));
        questionList.add(new Question("What is 1+2", "Use addition", new String[]{"3", "2", "1", "4"}));
        questionList.add(new Question("What is 2+2", "Use addition", new String[]{"4", "2", "3", "1"}));

        Room room = new Room(questionList, userId);
        RoomDao.createRoom(room);
        if (room.getId() != null) {
            ServletUtils.sendJSONResponse(response, room.toJSON());
        } else {
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/room/{}
     */
    private static void getRoomInfo(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        ServletUtils.sendJSONResponse(response, room.toJSON());
    }

    /**
     * POST /api/room/{}/join
     */
    private static void joinRoom(HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null)
            return;
        if (room.isLocked()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Room is locked");
            return;
        }
        User u = RoomServlet.getUser(request);
        if (!room.getUsers().contains(u)) {
            room.getUsers().add(RoomServlet.getUser(request));
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOK(response);
    }

    /**
     * DELETE /api/room/{}/quit
     */
    private static void quitRoom(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, false))
            return;
        User u = RoomServlet.getUser(request);
        if (room.getUsers().contains(u)) {
            room.getUsers().remove(u);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOK(response);
    }

    /**
     * DELETE /api/room/{}/delete
     */
    private static void deleteRoom(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        RoomDao.deleteRoom(room);
        ServletUtils.sendOK(response);
    }

    /**
     * DELETE /api/room/{}/kick/{}
     */
    private static void kickFromRoom(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        String[] path = request.getRequestURI().split("/");
        User u = new User(path[5]);
        if (room.getUsers().contains(u)) {
            room.getUsers().remove(u);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendJSONResponse(response, room.toJSON());
    }

    /**
     * POST /api/room/{}/answer/{}
     */
    private static void answerRoomQuestion(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, false))
            return;
        if (room.getState() == Room.State.ANSWERING) {
            String[] path = request.getRequestURI().split("/");
            Integer answer = Utils.stringToInteger(path[5]);
            if (answer == null || answer < 0 || answer > 4) {
                ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BetterArrayList.fromList(room.getUsers()).first(u -> u.getId().equals(userId)).setAnswer(answer);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOK(response);
    }

    /**
     * POST /api/room/{}/next
     */
    private static void nextRoomQuestion(String userId, HttpServletRequest request, HttpServletResponse response) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        room.next();
        RoomDao.updateRoom(room);
        ServletUtils.sendJSONResponse(response, room.toJSON());
    }

    /**
     * POST /api/room/{}/lock or unlock
     */
    private static void lockRoom(String userId, HttpServletRequest request, HttpServletResponse response, boolean lock) {
        Room room = RoomServlet.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        room.setLock(lock);
        RoomDao.updateRoom(room);
        ServletUtils.sendJSONResponse(response, room.toJSON());
    }

    private static String getUser(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getSession().getAttribute("userId");
        if (userId == null)
            ServletUtils.sendError(response, HttpServletResponse.SC_UNAUTHORIZED);
        return userId;
    }

    private static User getUser(HttpServletRequest request) {
        String id = (String) request.getSession().getAttribute("userId");
        String imageUrl = (String) request.getSession().getAttribute("userImageUrl");
        String name = (String) request.getSession().getAttribute("userName");
        return new User(id, name, imageUrl);
    }

    private static Room getRoomFromRequest(HttpServletRequest request, HttpServletResponse response) {
        String[] path = request.getRequestURI().split("/");
        String simpleId = path[3];
        Room room = RoomDao.getRoomBySimpleId(simpleId);
        if (room == null)
            ServletUtils.sendError(response, HttpServletResponse.SC_NOT_FOUND);
        return room;
    }

    private static boolean isForbidden(HttpServletResponse response, String userId, Room room, boolean master) {
        //master
        if (master && !room.getMasterId().equals(userId)) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        //member
        if (!master && !BetterArrayList.fromList(room.getUsers()).any(u -> u.getId().equals(userId))) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }
}