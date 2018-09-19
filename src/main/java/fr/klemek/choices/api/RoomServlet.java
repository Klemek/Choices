package fr.klemek.choices.api;

import fr.klemek.betterlists.BetterArrayList;
import fr.klemek.choices.dao.QuestionPackDao;
import fr.klemek.choices.dao.RoomDao;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.model.Room;
import fr.klemek.choices.model.User;
import fr.klemek.choices.oauth2.Oauth2CallbackServlet;
import fr.klemek.choices.utils.ServletUtils;
import fr.klemek.choices.utils.Utils;
import fr.klemek.logger.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/api/room/*")
public class RoomServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userId = ServletUtils.getUser(request, response);
            if (userId == null)
                return;
            Map<String, Runnable> map = new LinkedHashMap<>();

            map.put("PUT /api/room/create", () -> RoomServlet.createRoom(userId, request, response));
            map.put("GET /api/room/{}", () -> RoomServlet.getRoomInfo(userId, request, response));
            map.put("POST /api/room/{}", () -> RoomServlet
                    .updateRoom(userId, request, response));
            map.put("DELETE /api/room/{}/kick/{}", () -> RoomServlet
                    .kickFromRoom(userId, request, response));
            map.put("DELETE /api/room/{}/delete", () -> RoomServlet
                    .deleteRoom(userId, request, response));
            map.put("POST /api/room/{}/results", () -> RoomServlet.sendResults(userId, request, response));
            map.put("POST /api/room/{}/join", () -> RoomServlet.joinRoom(request, response));
            map.put("DELETE /api/room/{}/quit", () -> RoomServlet
                    .quitRoom(userId, request, response));
            map.put("POST /api/room/{}/answer/{}", () -> RoomServlet
                    .answerRoomQuestion(userId, request, response));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/room/create .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void createRoom(String userId, HttpServletRequest request,
                                   HttpServletResponse response) {
        Map<String, String> params = ServletUtils.readParameters(request); //because of PUT request

        Long packId = Utils.tryParseLong(params.get("packId"));
        QuestionPack pack = packId == null ? null : QuestionPackDao.getQuestionPackById(packId);

        if (pack == null || !pack.isEnabled()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        List<String> takenWords = RoomDao.listRoomSimpleIds();
        String simpleId;
        do {
            simpleId = Utils.getRandomWord();
        } while (takenWords.contains(simpleId));

        Room room = new Room(userId, simpleId);

        String lock = request.getParameter(Room.KEY_LOCK);
        if (lock != null)
            room.setLocked(Boolean.parseBoolean(lock));

        RoomDao.createRoom(room);
        if (room.getId() != null) {
            JSONObject json = room.toJson();
            json.put(Room.KEY_PACK, pack.toJson());
            ServletUtils.sendJsonResponse(response, json);
        } else {
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/room/{} .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void getRoomInfo(String userId, HttpServletRequest request,
                                    HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        ServletUtils.sendJsonResponse(response, room.toJson());
    }

    /**
     * POST /api/room/{}/join .
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void joinRoom(HttpServletRequest request, HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null)
            return;
        if (room.isLocked()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Room is locked");
            return;
        }
        User u = RoomServlet.getUser(request);
        if (!room.getUsers().contains(u)) {
            room.getUsers().add(RoomServlet.getUser(request));
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOk(response);
    }

    /**
     * DELETE /api/room/{}/quit .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void quitRoom(String userId, HttpServletRequest request,
                                 HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, false))
            return;
        User u = RoomServlet.getUser(request);
        if (room.getUsers().contains(u)) {
            room.getUsers().remove(u);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOk(response);
    }

    /**
     * DELETE /api/room/{}/delete .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void deleteRoom(String userId, HttpServletRequest request,
                                   HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        RoomDao.deleteRoom(room);
        ServletUtils.sendOk(response);
    }

    /**
     * DELETE /api/room/{}/kick/{} .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void kickFromRoom(String userId, HttpServletRequest request,
                                     HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;
        String[] path = request.getRequestURI().split("/");
        User u = new User(path[5]);
        if (room.getUsers().contains(u)) {
            room.getUsers().remove(u);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendJsonResponse(response, room.toJson());
    }

    /**
     * POST /api/room/{}/answer/{} .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void answerRoomQuestion(String userId, HttpServletRequest request,
                                           HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, false))
            return;
        if (!room.areAnswersLocked()) {
            String[] path = request.getRequestURI().split("/");
            Integer answer = Utils.tryParseInt(path[5]);
            if (answer == null || answer < 0 || answer > 4) {
                ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BetterArrayList.fromList(room.getUsers()).first(u -> u.getId().equals(userId))
                    .setAnswer(answer);
            RoomDao.updateRoom(room);
        }
        ServletUtils.sendOk(response);
    }


    /**
     * POST /api/room/{} .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void updateRoom(String userId, HttpServletRequest request,
                                   HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;

        boolean update = false;

        String lock = request.getParameter(Room.KEY_LOCK);
        if (lock != null) {
            room.setLocked(Boolean.parseBoolean(lock));
            update = true;
        }

        String lockAnswers = request.getParameter(Room.KEY_LOCK_ANSWERS);
        if (lockAnswers != null) {
            room.setAnswersLocked(Boolean.parseBoolean(lockAnswers));
            update = true;
        }

        String reset = request.getParameter(Room.KEY_RESET);
        if (reset != null) {
            room.resetAnswers();
            update = true;
        }

        if (update)
            RoomDao.updateRoom(room);
        ServletUtils.sendJsonResponse(response, room.toJson());
    }

    /**
     * POST /api/room/{}/results .
     *
     * @param userId   the current user id
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void sendResults(String userId, HttpServletRequest request,
                                    HttpServletResponse response) {
        Room room = ServletUtils.getRoomFromRequest(request, response);
        if (room == null || RoomServlet.isForbidden(response, userId, room, true))
            return;

        String userName = (String) request.getSession().getAttribute(Oauth2CallbackServlet.SESSION_USER_NAME);
        String userMail = (String) request.getSession().getAttribute(Oauth2CallbackServlet.SESSION_USER_EMAIL);

        try {

            Long datetime = Utils.tryParseLong(request.getParameter("datetime"));
            Long duration = Utils.tryParseLong(request.getParameter("duration"));
            Integer target = Utils.tryParseInt(request.getParameter("target"));
            Long packId = Utils.tryParseLong(request.getParameter("packId"));
            QuestionPack pack = packId == null ? null : QuestionPackDao.getQuestionPackById(packId);
            if (pack == null || duration == null || datetime == null || target == null) {
                ServletUtils.sendError(response, 400);
                return;
            }

            List<JSONObject> users = Utils.jarrayToList(new JSONArray(request.getParameter("users")));
            List<JSONObject> questions = Utils.jarrayToList(new JSONArray(request.getParameter("questions")));
            List<String> videos = Utils.jarrayToList(new JSONArray(request.getParameter("videos")));
            List<JSONArray> teachers = Utils.jarrayToList(new JSONArray(request.getParameter("teachers")));

            StringBuilder mailBody = new StringBuilder();

            mailBody.append("<h2>"+Utils.getString("mail.title")+"</h2>\n"
                    + "<ul>\n"
                    + "<li>Date : " + Utils.convertTime(datetime) + " UTC</li>\n"
                    + "<li>Topic : '" + pack.getName() + "'</li>\n"
                    + "<li>Started by : " + userName + " (" + Utils.getHtmlLink(userMail, true) + ")</li>\n"
                    + "<li>Target to mastery : " + target + " questions</li>\n"
                    + "<li>Users : " + users.size() + "</li>\n"
                    + "<li>Questions : " + questions.size() + "</li>\n"
                    + "<li>Duration : " + Utils.getNiceDuration(duration) + "</li>\n"
                    + "</ul>\n"
                    + "<h3>Users</h3>\n"
                    + "<table>\n"
                    + "<thead><tr><th>Name</th><th>Score</th><th>Group teaching*</th>"
                    + "<th>Group teaching failed**</th></tr></thead>\n"
                    + "<tbody>\n");

            Map<String, String> usersNames = new HashMap<>();

            for (JSONObject user : users) {
                String name = user.getString("name");
                usersNames.put(user.getString("id"), name);
                mailBody.append("<tr><td>" + name + "</td>"
                        + "<td>" + user.getInt("score") + "</td>"
                        + "<td>" + user.getInt("groupTeaching") + "</td>"
                        + "<td>" + user.getInt("teachingFailed") + "</td>"
                        + "</tr>\n");
            }

            mailBody.append("</tbody>\n"
                    + "</table>\n"
                    + "<h3>History</h3>\n"
                    + "<table>\n"
                    + "<thead>\n"
                    + "<tr><th>Step</th><th>Information</th></tr>\n"
                    + "</thead>\n"
                    + "<tbody>");

            int round = 1;
            int video = 0;
            int teacher = 0;

            for (JSONObject question : questions) {
                mailBody.append("<tr><td>Question " + round + "</td>"
                        + "<td>Right : " + question.getInt("right") + " / "
                        + "Wrong : " + question.getInt("wrong") + " / "
                        + "Unanswered : " + question.getInt("unanswered") + " / "
                        + "Remaining : " + (target - question.getInt("score")) + "</td></tr>\n");
                if (round % target == 0 && video < videos.size()) {
                    mailBody.append("<tr><td>Video</td>"
                            + "<td>" + Utils.getHtmlLink(videos.get(video++), false) + "</td></tr>\n");
                } else if (round % target == target / 2 && teacher < teachers.size()) {
                    List<String> idList = Utils.jarrayToList(teachers.get(teacher++));
                    if (idList.isEmpty())
                        mailBody.append("<tr><td>Teacher</td><td><i>Skipped</i></td></tr>\n");
                    else
                        for (String id : idList)
                            mailBody.append("<tr><td>Teacher</td>"
                                    + "<td>" + usersNames.getOrDefault(id, "Unkown") + "</td></tr>\n");
                }
                round++;
            }

            mailBody.append("</tbody>\n"
                    + "</table>\n"
                    + "* Times user teached after a question<br/>\n"
                    + "** Times user failed to teach after a question");

            if (Utils.sendMailToAdmin(Utils.getString("mail.title"), mailBody.toString()))
                ServletUtils.sendOk(response);
            else
                ServletUtils.sendError(response, 500);
        } catch (JSONException e) {
            Logger.log(Level.WARNING, e);
            ServletUtils.sendError(response, 400);
            return;
        }
    }

    private static User getUser(HttpServletRequest request) {
        String id = (String) request.getSession().getAttribute("userId");
        String imageUrl = (String) request.getSession().getAttribute("userImageUrl");
        String name = (String) request.getSession().getAttribute("userName");
        return new User(id, name, imageUrl);
    }

    private static boolean isForbidden(HttpServletResponse response, String userId, Room room,
                                       boolean master) {
        //master
        if (master && !room.getMasterId().equals(userId)) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        //member
        if (!master && !BetterArrayList.fromList(room.getUsers())
                .any(u -> u.getId().equals(userId))) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }
}