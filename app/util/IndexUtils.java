package util;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.ScoreDoc;

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

    /**
     * Serializes a {@link ScoreDoc}
     * 
     * @param scoreDoc
     * @return
     */
    public static String serializeScoreDoc(ScoreDoc scoreDoc) {
        ByteBuffer buff = ByteBuffer.allocate(4 + 4 + 4);
        buff.putInt(scoreDoc.doc);
        buff.putFloat(scoreDoc.score);
        buff.putInt(scoreDoc.shardIndex);
        return Base64.encodeBase64String(buff.array());
    }

    /**
     * Deserializes a {@link ScoreDoc}.
     * 
     * @param serData
     * @return
     */
    public static ScoreDoc derializeScoreDoc(String serData) {
        try {
            ByteBuffer buff = ByteBuffer.wrap(Base64.decodeBase64(serData));
            int doc = buff.getInt();
            float score = buff.getFloat();
            int shardIndex = buff.getInt();
            ScoreDoc scoreDoc = new ScoreDoc(doc, score, shardIndex);
            return scoreDoc;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> docToMap(Document doc) {
        if (doc == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        for (IndexableField field : doc.getFields()) {
            String name = field.name();
            String value = field.stringValue();
            result.put(name, value);
        }
        return result;
    }
}
