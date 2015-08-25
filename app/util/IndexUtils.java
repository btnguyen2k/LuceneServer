package util;

/**
 * Utility class.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class IndexUtils {
    public static String normalizeName(final String name) {
        return name != null ? name.trim().toLowerCase() : "_";
    }

    /**
     * Checks if a index's/field's name is valid.
     * 
     * @param name
     * @return
     */
    public static boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z0-9_]+$");
    }
}
