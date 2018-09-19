package fr.klemek.choices.api;

import fr.klemek.betterlists.BetterArrayList;
import fr.klemek.choices.dao.QuestionPackDao;
import fr.klemek.choices.model.Question;
import fr.klemek.choices.model.QuestionPack;
import fr.klemek.choices.utils.ServletUtils;
import fr.klemek.choices.utils.Utils;
import fr.klemek.logger.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

@WebServlet("/api/questions/*")
public class QuestionsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            String userId = ServletUtils.getUser(request, response);
            if (userId == null)
                return;
            boolean admin = ServletUtils.isUserAdmin(request);
            Map<String, Runnable> map = new LinkedHashMap<>();
            map.put("GET /api/questions/list", () -> QuestionsServlet.listPacks(response));
            map.put("GET /api/questions/all", () -> QuestionsServlet.listPacksFull(admin, response));
            map.put("PUT /api/questions/create", () -> QuestionsServlet.createPack(admin, request, response));
            map.put("GET /api/questions/{}", () -> QuestionsServlet.getPackDetails(admin, request, response));
            map.put("POST /api/questions/{}", () -> QuestionsServlet.updatePack(admin, request, response));
            map.put("DELETE /api/questions/{}", () -> QuestionsServlet.deletePack(admin, request, response));
            ServletUtils.mapRequest(request, response, map);
        } catch (Exception e) {
            Logger.log(e);
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/questions/list .
     *
     * @param response the HttpServletResponse
     */
    private static void listPacks(HttpServletResponse response) {
        List<QuestionPack> packs = QuestionPackDao.listQuestionPacks();
        JSONArray array = new JSONArray(BetterArrayList.fromList(packs)
                .where(QuestionPack::isEnabled)
                .select(qp -> qp.toJson(false)));
        ServletUtils.sendJsonResponse(response, array);
    }

    /**
     * GET /api/questions/all .
     *
     * @param admin    if the user is admin
     * @param response the HttpServletResponse
     */
    private static void listPacksFull(boolean admin, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        List<QuestionPack> packs = QuestionPackDao.listQuestionPacks();
        JSONArray array = new JSONArray(BetterArrayList.fromList(packs).select(QuestionPack::toJson));
        ServletUtils.sendJsonResponse(response, array);
    }

    /**
     * PUT /api/questions/create .
     *
     * @param admin    if the user is admin
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void createPack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;

        Map<String, String> params = ServletUtils.readParameters(request); //because of PUT request
        String name = params.get(QuestionPack.KEY_NAME);
        String message = request.getParameter(QuestionPack.KEY_MESSAGE);
        String video = request.getParameter(QuestionPack.KEY_VIDEO);
        boolean enabled = Boolean.parseBoolean(request.getParameter(QuestionPack.KEY_ENABLED));
        List<Question> questions = QuestionsServlet.getQuestionList(params.get(QuestionPack.KEY_QUESTIONS));
        if (name == null
                || name.trim().isEmpty()
                || questions.isEmpty()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        QuestionPack pack = new QuestionPack(name, video, message, enabled, questions);
        QuestionPackDao.createQuestionPack(pack);
        if (pack.getId() != null) {
            ServletUtils.sendJsonResponse(response, pack.toJson());
        } else {
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/questions/{} .
     *
     * @param admin    if the user is admin
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void getPackDetails(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;
        ServletUtils.sendJsonResponse(response, pack.toJson());
    }

    /**
     * POST /api/questions/{} .
     *
     * @param admin    if the user is admin
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void updatePack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;

        String name = request.getParameter(QuestionPack.KEY_NAME);
        String message = request.getParameter(QuestionPack.KEY_MESSAGE);
        String video = request.getParameter(QuestionPack.KEY_VIDEO);
        boolean enabled = Boolean.parseBoolean(request.getParameter(QuestionPack.KEY_ENABLED));
        List<Question> questions = QuestionsServlet.getQuestionList(request.getParameter(QuestionPack.KEY_QUESTIONS));
        if (name == null
                || name.trim().isEmpty()
                || questions.isEmpty()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        pack = new QuestionPack(pack.getId(), name, video, message, enabled, questions);
        QuestionPackDao.updateQuestionPack(pack);
        ServletUtils.sendJsonResponse(response, pack.toJson());
    }

    /**
     * DELETE /api/questions/{} .
     *
     * @param admin    if the user is admin
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     */
    private static void deletePack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;
        QuestionPackDao.deleteQuestionPack(pack);
        ServletUtils.sendOk(response);
    }

    private static boolean isForbidden(HttpServletResponse response, boolean admin) {
        if (!admin) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }

    private static List<Question> getQuestionList(String strJsonList) {
        JSONArray questionArray;
        try {
            questionArray = new JSONArray(strJsonList == null ? "" : strJsonList);
        } catch (JSONException ignored) {
            return Collections.emptyList();
        }
        return BetterArrayList
                .fromList(Utils.jarrayToList(questionArray))
                .select(json -> Question.fromJson(json.toString()))
                .where(Objects::nonNull);
    }
}