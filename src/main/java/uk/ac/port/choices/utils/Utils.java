package uk.ac.port.choices.utils;

import fr.klemek.betterlists.BetterArrayList;
import fr.klemek.betterlists.BetterList;
import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Utility class that store useful misc functions.
 */
public final class Utils {

    private static final ResourceBundle RELEASE_BUNDLE = ResourceBundle.getBundle("release");

    private static int wordCount = 0;

    private Utils() {
    }

    /**
     * Get a configuration string by its key.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    public static String getString(String key) {
        try {
            return Utils.RELEASE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            Logger.log(Level.SEVERE, "Missing configuration string {0}", key);
            return null;
        }
    }

    /**
     * Check if the given mail is in the admin list.
     *
     * @param email the mail to check
     * @return true if its admin
     */
    public static boolean isAdmin(String email) {
        String admins = Utils.getString("admins");
        if (admins == null)
            return false;
        BetterList<String> configAdmins = new BetterArrayList<>(Arrays.asList(admins.split(";")));
        return configAdmins.any(admin -> admin.equals(email));
    }

    /*
     * Other
     */

    /**
     * Transform a JSONArray into a List of wanted class.
     *
     * @param src the source JSONArray
     * @return a list
     */
    public static <T> List<T> jarrayToList(JSONArray src) {
        List<T> lst = new ArrayList<>(src.length());
        try {
            for (int i = 0; i < src.length(); i++)
                lst.add((T) src.get(i));
        } catch (ClassCastException | NullPointerException e) {
            throw new JSONException("Cannot cast class", e);
        }
        return lst;
    }

    /**
     * Try to parse a String as int value.
     *
     * @param text the String value
     * @return the parsed Int or null
     */
    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Try to parse a String as long value.
     *
     * @param text the String value
     * @return the parsed Long or null
     */
    public static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
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

    /**
     * Get the first not-null item from arguments.
     *
     * @param items the items to take from
     * @param <T>   the class of the items
     * @return the first not-null item or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T coalesce(T... items) {
        for (T i : items) if (i != null) return i;
        return null;
    }

    /**
     * Check if a String is alphanumeric including some chars.
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

    /**
     * Check if the first string contains the second in a non case sensitive way.
     *
     * @param s1 first string
     * @param s2 second string
     * @return the result of the check
     */
    public static boolean containsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    /**
     * Read the number of words to be selected at random.
     */
    public static void initRandomWords() {
        wordCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Utils.class.getClassLoader().getResourceAsStream("words.txt")))) {
            wordCount = (int) reader.lines().count();
            Logger.log(Level.INFO, "{0} words loaded for room names", wordCount);
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    /**
     * Get a random word from the list file.
     *
     * @return the random select word or a random string if there is none
     */
    public static String getRandomWord() {
        if (wordCount > 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Utils.class.getClassLoader().getResourceAsStream("words.txt")))) {
                String word;
                int tries = 3;
                while (tries > 0) {
                    int position = ThreadLocalRandom.current().nextInt(wordCount);
                    word = reader.lines().skip(position).findFirst().orElse("");
                    if (word.trim().length() > 0)
                        return word.trim();
                    tries--;
                }
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        return Utils.getRandomString(6, "I", "l", "O", "0");
    }

    /**
     * Generate a random string with numbers, uppercase and lowercase letters.
     *
     * @param length  the length of the string
     * @param avoided every substring or char to avoid
     * @return the generated string
     */
    public static String getRandomString(int length, String... avoided) {
        boolean correct;
        String generated;
        do {
            generated = Utils.getRandomString(length);
            correct = true;
            for (String avoidedUnit : avoided) {
                if (generated.contains(avoidedUnit)) {
                    correct = false;
                    break;
                }
            }
        } while (!correct);
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
     * Send an email to the system administrator defined in the configuration.
     *
     * @param title    the mail's title
     * @param htmlBody the HTML value to be written as email body
     * @return true if the operation is successful
     */
    public static boolean sendMailToAdmin(String title, String htmlBody) {
        String recipient = Utils.getString("mail.recipient");
        if (recipient == null)
            return false;
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Utils.getString("mail.sender"), Utils.getString("app.name")));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            msg.setSubject(title);

            Multipart mp = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(getMailHtml(title, htmlBody), "text/html");
            mp.addBodyPart(htmlPart);
            msg.setContent(mp);

            Transport.send(msg);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            Logger.log(e, "Cannot send mail");
            return false;
        }
    }

    /**
     * Get the header of a standard email in HTML.
     *
     * @param title    the mail's title
     * @param htmlBody the HTML value to be written as email body
     * @return the entire HTML of the email
     */
    private static String getMailHtml(String title, String htmlBody) {

        htmlBody = htmlBody.replace("<table>",
                "<table style=\"border-collapse: collapse;border: 1px solid black;\">")
                .replace("<th>",
                        "<th style=\"border: 1px solid black;padding:0.5rem;\">")
                .replace("<td>",
                        "<td style=\"border: 1px solid black;padding:0.5rem;\">");

        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head>\n"
                + "<!--[if gte mso 9]><xml>\n"
                + "<o:OfficeDocumentSettings>\n"
                + "<o:AllowPNG/>\n"
                + "<o:PixelsPerInch>96</o:PixelsPerInch>\n"
                + "</o:OfficeDocumentSettings>\n"
                + "</xml><![endif]-->\n"
                + "<title>" + title + "</title>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n"
                + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0 \">\n"
                + "<meta name=\"format-detection\" content=\"telephone=no\">\n"
                + "<!--[if !mso]><!-->\n"
                + "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700,800\" rel=\"stylesheet\">\n"
                + "<!--<![endif]-->"
                + "</head>\n"
                + "<body style=\"margin:0px; padding:0px;\" bgcolor=\"#000000\">\n"
                + htmlBody + "\n"
                + "</body>\n"
                + "</html>";
    }

    /**
     * Transform links into correct HTML links.
     *
     * @param link the link to transform
     * @param mail if the link is an email
     * @return the correct HTML link
     */
    public static String getHtmlLink(String link, boolean mail) {
        if (link == null)
            return "";
        String link2 = link;
        String name = link;
        int pos = link.indexOf(']');
        if (link.startsWith("[")) {
            name = link.substring(1, pos);
            link2 = link.substring(pos + 1);
        }
        if (mail || link2.startsWith("http"))
            return "<a href=\"" + (mail ? "mailto:" : "") + link2 + "\">" + name + "</a>";
        else
            return link;
    }

    /**
     * Returns a millis time duration into hours and minutes.
     *
     * @param millis the time duration
     * @return a text of the duration in hours and minutes
     */
    public static String getNiceDuration(long millis) {
        long minutes = millis / 60000;
        long hours = minutes / 60;
        minutes %= 60;

        String output = String.format("%02d min.", minutes);
        if (hours > 0) {
            output = String.format("%d hour%s ", hours, hours > 1 ? "s" : "") + output;
        }
        return output;
    }

    /**
     * Convert a timestamp into a readable datetime.
     *
     * @param time the long timestamp
     * @return a string readable datetime
     */
    public static String convertTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(time);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy 'at' HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(cal.getTime());
    }
}
