package fr.klemek.choices.dao;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;

import fr.klemek.choices.utils.Lang;
import fr.klemek.logger.Logger;

import java.util.Map;
import java.util.logging.Level;

public final class LangDao {

    private static KeyFactory keyFactory;

    private LangDao() {
    }

    private static KeyFactory getKeyFactory() {
        if (LangDao.keyFactory == null)
            LangDao.keyFactory = DaoUtils.getDatastore()
                    .newKeyFactory().setKind(Lang.MODEL_KIND);// Is used for creating keys later
        return LangDao.keyFactory;
    }

    //region Functions

    /**
     * Get lang string in the datastore by key or null if not fond.
     *
     * @param key   the lang key
     * @return Entity found or null
     */
    private static Entity getByKey(String key) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(Lang.MODEL_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(Lang.KEY_KEY, key))
                .setLimit(1)
                .build();
        QueryResults<Entity> lang = DaoUtils.getDatastore().run(query);
        return lang.hasNext() ? lang.next() : null;
    }

    /**
     * Create or update lang string in the datastore.
     *
     * @param key   the lang key
     * @param value the lang value
     */
    public static void updateString(String key, String value) {
        Entity entity = LangDao.getByKey(key);
        if (entity == null) {
            //create
            IncompleteKey ikey = LangDao.getKeyFactory().newKey();
            FullEntity<IncompleteKey> incLangEntity = (FullEntity<IncompleteKey>) DaoUtils
                    .langToEntityBuilder(key, value, Entity.newBuilder(ikey)).build();
            DaoUtils.getDatastore().add(incLangEntity); // Save the Entity
            Logger.log(Level.INFO, "Lang {0} created : {1}", key, value);
        } else {
            //update
            Entity updatedEntity = (Entity) DaoUtils
                    .langToEntityBuilder(key, value, Entity.newBuilder(entity.getKey())).build();
            DaoUtils.getDatastore().update(updatedEntity);
            Logger.log(Level.INFO, "Lang {0} updated : {1}", key, value);
        }
    }

    /**
     * Delete lang string in the datastore.
     *
     * @param key the lang key
     */
    public static void deleteString(String key) {
        Entity entity = LangDao.getByKey(key);
        if (entity != null) {
            DaoUtils.getDatastore().delete(entity.getKey());
            Logger.log(Level.INFO, "Lang {0} deleted", key);
        }

    }

    /**
     * List all lang strings in the datastore.
     *
     * @return a Map containing each String
     */
    public static Map<String, String> listStrings() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(Lang.MODEL_KIND)
                .setOrderBy(StructuredQuery.OrderBy.asc(Lang.KEY_KEY))
                .build();
        QueryResults<Entity> resultList = DaoUtils.getDatastore().run(query);
        return DaoUtils.entityListToLang(resultList);
    }

    //endregion

}
