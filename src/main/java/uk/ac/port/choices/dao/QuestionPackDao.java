package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import uk.ac.port.choices.model.QuestionPack;
import uk.ac.port.choices.utils.Logger;

import java.util.List;
import java.util.logging.Level;

public class QuestionPackDao {

    static final String KIND = "QuestionPack";
    private static KeyFactory keyFactory;

    private QuestionPackDao() {
    }

    private static KeyFactory getKeyFactory() {
        if (QuestionPackDao.keyFactory == null)
            QuestionPackDao.keyFactory = DaoUtils.getDatastore().newKeyFactory().setKind(QuestionPackDao.KIND);// Is used for creating keys later
        return QuestionPackDao.keyFactory;
    }

    //region Functions

    @SuppressWarnings("unchecked")
    public static Long createQuestionPack(QuestionPack pack) {
        IncompleteKey key = QuestionPackDao.getKeyFactory().newKey();
        FullEntity<IncompleteKey> incRoomEntity = (FullEntity<IncompleteKey>) DaoUtils.questionPackToEntityBuilder(pack, Entity.newBuilder(key)).build();
        Entity roomEntity = DaoUtils.getDatastore().add(incRoomEntity); // Save the Entity
        pack.setId(roomEntity.getKey().getId());
        Logger.log(Level.INFO, "QuestionPack {0} created : {1}", pack.getId(), pack);
        return roomEntity.getKey().getId();
    }

    public static void updateQuestionPack(QuestionPack pack) {
        Key key = QuestionPackDao.getKeyFactory().newKey(pack.getId());
        Entity entity = (Entity) DaoUtils.questionPackToEntityBuilder(pack, Entity.newBuilder(key)).build();
        DaoUtils.getDatastore().update(entity);
        Logger.log(Level.INFO, "QuestionPack {0} updated : {1}", pack.getId(), pack);
    }

    public static QuestionPack getQuestionPackById(long id) {
        Key key = QuestionPackDao.getKeyFactory().newKey(id);
        Entity entity = DaoUtils.getDatastore().get(key);
        return entity == null ? null : DaoUtils.entityToQuestionPack(entity);
    }

    public static void deleteQuestionPack(QuestionPack pack) {
        Key key = QuestionPackDao.getKeyFactory().newKey(pack.getId());
        DaoUtils.getDatastore().delete(key);
        Logger.log(Level.INFO, "QuestionPack {0} deleted", pack.getId());
    }

    public static List<QuestionPack> listQuestionPacks() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(QuestionPackDao.KIND)
                .setOrderBy(StructuredQuery.OrderBy.asc(QuestionPack.KEY_NAME))
                .build();
        QueryResults<Entity> resultList = DaoUtils.getDatastore().run(query);
        return DaoUtils.entityListToQuestionPackList(resultList);

    }

    //endregion

}
