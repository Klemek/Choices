package uk.ac.port.choices.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class that store useful misc functions.
 *
 * @author Clement Gouin
 */
public final class Utils {


    private Utils() {
    }

    /*
     * Other
     */

    /**
     * Generate a random string with numbers, uppercase and lowercase letters.
     *
     * @param length the length of the string
     * @param avoided every substring or char to avoid
     * @return the generated string
     */
    public static String getRandomString(int length, String...avoided) {
        boolean correct;
        String generated;
        do{
            generated = Utils.getRandomString(length);
            correct = true;
            for(String avoidedUnit:avoided){
                if(generated.contains(avoidedUnit)){
                    correct = false;
                    break;
                }
            }
        }while(!correct);
        return generated;
    }

    /**
     * Generate a random string with numbers, uppercase and lowercase letters.
     *
     * @param length the length of the string
     * @return the generated string
     */
    public static String getRandomString(int length) {
        StringBuilder output = new StringBuilder();
        int pos;
        for (int i = 0; i < length; i++) { // 48-57 65-90 97-122
            pos = ThreadLocalRandom.current().nextInt(62);
            if (pos < 10)
                output.append((char) (pos + 48)); // numbers
            else if (pos < 36)
                output.append((char) (pos + 55)); // uppercase letters
            else
                output.append((char) (pos + 61)); // lowercase letters
        }
        return output.toString();
    }

    /**
     * Transform a JSONArray into a List of jsonObject.
     *
     * @param src the source JSONArray
     * @return a list of jsonObject
     */
    public static List<JSONObject> jArrayToJObjectList(JSONArray src) {
        List<JSONObject> lst = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); i++)
            lst.add(src.getJSONObject(i));
        return lst;
    }

    public static List<String> jArrayToStringList(JSONArray src) {
        List<String> lst = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); i++)
            lst.add(src.getString(i));
        return lst;
    }

    /**
     * Convert a string to an Integer.
     *
     * @param text a text to convert to Integer
     * @return Integer or null if the string couldn't be converted
     */
    public static Integer stringToInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * Return the class name from the calling class in th stack trace.
     *
     * @param stackLevel the level in the stack trace
     * @return the classname of th calling class
     */
    public static String getCallingClassName(int stackLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackLevel >= stackTrace.length)
            return null;
        String[] source = stackTrace[stackLevel].getClassName().split("\\.");
        return source[source.length - 1];
    }

    @SuppressWarnings("unchecked")
    public static <T> T coalesce(T... items) {
        for (T i : items) if (i != null) return i;
        return null;
    }

    /**
     * Check if a String is alphanumeric including some chars
     *
     * @param source   the String to test
     * @param included included chars other than alphanumerics
     * @return true if it passes
     */
    public static boolean isAlphaNumeric(String source, Character... included) {
        if (source == null)
            return true;
        List<Character> includedList = Arrays.asList(included);
        for (char c : source.toCharArray())
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && !includedList.contains(c))
                return false;
        return true;
    }

    public static boolean containsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    /**
     * Navigate through a JSONObject by keys
     *
     * @param source the original JSONObject
     * @param keys   the keys to find successively
     * @return the found JSONObject or null if the key was not found
     */
    public static JSONObject navigateJSON(JSONObject source, String... keys) {
        JSONObject obj = source;
        for (String key : keys) {
            if (!obj.has(key))
                return null;
            obj = obj.getJSONObject(key);
        }
        return obj;
    }
}
