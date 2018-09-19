package uk.ac.port.choices.dao;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;

import fr.klemek.logger.Logger;

import java.util.List;
import java.util.logging.Level;

import uk.ac.port.choices.model.QuestionPack;

public final class QuestionPackDao {

    private static KeyFactory keyFactory;

    private QuestionPackDao() {
    }

    private static KeyFactory getKeyFactory() {
        if (QuestionPackDao.keyFactory == null)
            QuestionPackDao.keyFactory = DaoUtils.getDatastore()
                    .newKeyFactory().setKind(QuestionPack.MODEL_KIND);// Is used for creating keys later
        return QuestionPackDao.keyFactory;
    }

    //region Functions

    /**
     * Register a new question pack in datastore.
     *
     * @param pack the question pack to register
     * @return the new id created or null on error
     */
    @SuppressWarnings("unchecked")
    public static Long createQuestionPack(QuestionPack pack) {
        IncompleteKey key = QuestionPackDao.getKeyFactory().newKey();
        FullEntity<IncompleteKey> incRoomEntity = (FullEntity<IncompleteKey>) DaoUtils
                .questionPackToEntityBuilder(pack, Entity.newBuilder(key)).build();
        Entity roomEntity = DaoUtils.getDatastore().add(incRoomEntity); // Save the Entity
        pack.setId(roomEntity.getKey().getId());
        Logger.log(Level.INFO, "QuestionPack {0} created : {1}", pack.getId(), pack);
        return roomEntity.getKey().getId();
    }

    /**
     * Update a question pack in the datastore.
     *
     * @param pack the pack to update
     */
    public static void updateQuestionPack(QuestionPack pack) {
        Key key = QuestionPackDao.getKeyFactory().newKey(pack.getId());
        Entity entity = (Entity) DaoUtils.questionPackToEntityBuilder(pack,
                Entity.newBuilder(key)).build();
        DaoUtils.getDatastore().update(entity);
        Logger.log(Level.INFO, "QuestionPack {0} updated : {1}", pack.getId(), pack);
    }

    /**
     * Get a question pack from its id in the datastore.
     *
     * @param id the id to find
     * @return the found question pack or null
     */
    public static QuestionPack getQuestionPackById(long id) {
        Key key = QuestionPackDao.getKeyFactory().newKey(id);
        Entity entity = DaoUtils.getDatastore().get(key);
        return entity == null ? null : DaoUtils.entityToQuestionPack(entity);
    }

    /**
     * Delete a question pack from the datastore.
     *
     * @param pack the pack to delete
     */
    public static void deleteQuestionPack(QuestionPack pack) {
        Key key = QuestionPackDao.getKeyFactory().newKey(pack.getId());
        DaoUtils.getDatastore().delete(key);
        Logger.log(Level.INFO, "QuestionPack {0} deleted", pack.getId());
    }

    /**
     * List all question packs in the datastore.
     *
     * @return the question pack full list
     */
    public static List<QuestionPack> listQuestionPacks() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(QuestionPack.MODEL_KIND)
                .setOrderBy(StructuredQuery.OrderBy.asc(QuestionPack.KEY_NAME))
                .build();
        QueryResults<Entity> resultList = DaoUtils.getDatastore().run(query);
        return DaoUtils.entityListToQuestionPackList(resultList);

    }

    //endregion

}
