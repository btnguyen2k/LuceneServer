package lucene.action;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.SerializationUtils;

public class IndexAction extends BaseAction {

    private final static String ATTR_DOC = "doc";

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Object> doc() {
        Object result = getAttribute(ATTR_DOC);
        try {
            return result instanceof Map ? (Map<String, Object>) result : SerializationUtils
                    .fromJsonString(result.toString(), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public IndexAction doc(Map<String, Object> fieldsAndValues) {
        setAttribute(ATTR_DOC, fieldsAndValues);
        return this;
    }
}
