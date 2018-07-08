package uk.ac.port.choices.api;

import fr.klemek.betterlists.BetterArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.port.choices.dao.QuestionPackDao;
import uk.ac.port.choices.model.Question;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.utils.Logger;
import uk.ac.port.choices.utils.ServletUtils;
import uk.ac.port.choices.utils.Utils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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

    private static void listPacks(HttpServletResponse response) {
        List<QuestionPack> packs = QuestionPackDao.listQuestionPacks();
        JSONArray array = new JSONArray(BetterArrayList.fromList(packs).select(QuestionsServlet::getPackInfo));
        ServletUtils.sendJSONResponse(response, array);
    }

    private static void listPacksFull(boolean admin, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        List<QuestionPack> packs = QuestionPackDao.listQuestionPacks();
        JSONArray array = new JSONArray(BetterArrayList.fromList(packs).select(QuestionPack::toJSON));
        ServletUtils.sendJSONResponse(response, array);
    }

    private static void createPack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;

        Map<String, String> params = ServletUtils.readParameters(request); //because of PUT request
        String name = params.get(QuestionPack.KEY_NAME);
        List<Question> questions = QuestionsServlet.getQuestionList(params.get(QuestionPack.KEY_QUESTIONS));
        if (name == null
                || name.trim().isEmpty()
                || questions.isEmpty()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        QuestionPack pack = new QuestionPack(name, questions);
        QuestionPackDao.createQuestionPack(pack);
        if (pack.getId() != null) {
            ServletUtils.sendJSONResponse(response, pack.toJSON());
        } else {
            ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void getPackDetails(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;
        ServletUtils.sendJSONResponse(response, pack.toJSON());
    }

    private static void updatePack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;

        String name = request.getParameter(QuestionPack.KEY_NAME);
        List<Question> questions = QuestionsServlet.getQuestionList(request.getParameter(QuestionPack.KEY_QUESTIONS));
        if (name == null
                || name.trim().isEmpty()
                || questions.isEmpty()) {
            ServletUtils.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        pack = new QuestionPack(pack.getId(), name, questions);
        QuestionPackDao.updateQuestionPack(pack);
        ServletUtils.sendJSONResponse(response, pack.toJSON());
    }

    private static void deletePack(boolean admin, HttpServletRequest request, HttpServletResponse response) {
        if (QuestionsServlet.isForbidden(response, admin))
            return;
        QuestionPack pack = ServletUtils.getQuestionPackFromRequest(request, response);
        if (pack == null)
            return;
        QuestionPackDao.deleteQuestionPack(pack);
        ServletUtils.sendOK(response);
    }


    private static boolean isForbidden(HttpServletResponse response, boolean admin) {
        if (!admin) {
            ServletUtils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return true;
        }
        return false;
    }

    private static JSONObject getPackInfo(QuestionPack pack) {
        JSONObject json = new JSONObject();
        json.put("id", pack.getId());
        json.put("name", pack.getName());
        return json;
    }

    private static List<Question> getQuestionList(String strJsonList) {
        JSONArray questionArray;
        try {
            questionArray = new JSONArray(strJsonList == null ? "" : strJsonList);
        } catch (JSONException ignored) {
            return Collections.emptyList();
        }
        return BetterArrayList
                .fromList(Utils.jArrayToJObjectList(questionArray))
                .select(json -> Question.fromJSON(json.toString()))
                .where(Objects::nonNull);
    }
}