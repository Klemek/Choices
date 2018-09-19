package fr.klemek.choices.utils;

import fr.klemek.choices.dao.LangDao;
import fr.klemek.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Store langage storage utilities.
 */
public final class Lang {

    public static final String MODEL_KIND = "Lang";

    public static final String KEY_KEY = "key";
    public static final String KEY_VALUE = "value";
    private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle("lang_default");

    private static boolean store;
    private static long hashCode = -1;

    //following only used if store is true
    private static JSONObject json;
    private static Map<String, String> values;


    private Lang() {
    }

    private static Set<String> getSortedKeys() {
        return new TreeSet<>(DEFAULT_BUNDLE.keySet());
    }

    /**
     * Load default values and get current values from the datastore.
     * @param store the option to use memory storage of values
     */
    public static void init(boolean store) {
        Lang.store = store;
        Lang.hashCode = -1;
        Lang.values = new HashMap<>();
        Lang.json = null;
        //Load datastore or defaults values
        Map<String, String> currentValues = LangDao.listStrings();
        for (String key : DEFAULT_BUNDLE.keySet())
            if (currentValues.containsKey(key)) {
                if (Lang.store)
                    Lang.values.put(key, currentValues.get(key));
            } else {
                LangDao.updateString(key, DEFAULT_BUNDLE.getString(key)); //create entry
                if (Lang.store)
                    Lang.values.put(key, DEFAULT_BUNDLE.getString(key));
            }

        //remove extra keys
        for (String key : currentValues.keySet())
            if (!DEFAULT_BUNDLE.containsKey(key))
                LangDao.deleteString(key);

        Lang.toJson(true);
    }

    /**
     * Returns a JSON object containing all lang strings.
     *
     * @return a JSON object containing all lang strings
     */
    public static JSONObject toJson() {
        return toJson(false);
    }

    private static JSONObject toJson(boolean force) {
        if (Lang.json == null || force) {
            Lang.hashCode = -1;
            Lang.getHashCode(); //reload hashcode

            JSONObject tmpJson = new JSONObject();
            Map<String, String> tmpValues = Lang.store ? Lang.values : LangDao.listStrings();
            for (String key : Lang.getSortedKeys())
                tmpJson.put(key, tmpValues.get(key));

            if (!Lang.store)
                return tmpJson;

            Lang.json = tmpJson;
        }
        return Lang.json;
    }

    /**
     * Returns the hashCode of all lang strings altogether.
     *
     * @return the hashCode of all lang strings altogether
     */
    public static long getHashCode() {
        if (Lang.hashCode == -1) {
            Lang.hashCode = 0;
            Map<String, String> tmpValues = Lang.store ? Lang.values : LangDao.listStrings();
            for (String key : Lang.getSortedKeys())
                Lang.hashCode += tmpValues.get(key).hashCode();
        }
        return Lang.hashCode;
    }

    /**
     * Update current values.
     *
     * @param strJson the json to import for update.
     * @return false if the json isn't well formated
     */
    public static boolean update(String strJson) {
        JSONObject json;
        try {
            json = new JSONObject(strJson);
        } catch (JSONException e) {
            Logger.log(Level.WARNING, e);
            return false;
        }
        boolean change = false;
        for (Map.Entry<String, String> entry : Lang.store ? Lang.values.entrySet() : LangDao.listStrings().entrySet()) {
            if (json.has(entry.getKey())) {
                String newVal = json.getString(entry.getKey());
                if (!newVal.equals(entry.getValue())) {
                    change = true;
                    if (Lang.store)
                        Lang.values.put(entry.getKey(), newVal);
                    LangDao.updateString(entry.getKey(), newVal);
                }
            }
        }
        if (change)
            Lang.toJson(true); //update JSON and HashCode
        return true;
    }
}
