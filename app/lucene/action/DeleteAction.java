package lucene.action;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.commons.utils.SerializationUtils;

public class DeleteAction extends BaseAction {
    public final static int DELETE_METHOD_QUERY = 0;
    public final static int DELETE_METHOD_TERM = 1;

    private final static String ATTR_DELETE_METHOD = "method";
    private final static String ATTR_DELETE_QUERY = "query";
    private final static String ATTR_DELETE_TERM = "term";

    public DeleteAction(String indexName) {
        super(indexName);
    }

    @JsonIgnore
    public int deleteMethod() {
        Integer result = getAttribute(ATTR_DELETE_METHOD, Integer.class);
        return result != null ? result.intValue() : 0;
    }

    public DeleteAction deleteMethod(int value) {
        setAttribute(ATTR_DELETE_METHOD, value);
        return this;
    }

    @JsonIgnore
    public String query() {
        return getAttribute(ATTR_DELETE_QUERY, String.class);
    }

    public DeleteAction query(String value) {
        setAttribute(ATTR_DELETE_QUERY, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Object> term() {
        Object result = getAttribute(ATTR_DELETE_TERM);
        try {
            return result instanceof Map ? (Map<String, Object>) result : SerializationUtils
                    .fromJsonString(result.toString(), Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public DeleteAction term(Map<String, Object> fieldsAndValues) {
        setAttribute(ATTR_DELETE_TERM, fieldsAndValues);
        return this;
    }
}
