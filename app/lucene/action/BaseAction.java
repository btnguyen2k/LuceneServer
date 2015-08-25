package lucene.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ddth.queue.UniversalQueueMessage;

public abstract class BaseAction extends UniversalQueueMessage {
    private final static String ATTR_INDEX_NAME = "index";

    public BaseAction(String indexName) {
        indexName(indexName);
    }

    @JsonIgnore
    public String indexName() {
        return getAttribute(ATTR_INDEX_NAME, String.class);
    }

    public BaseAction indexName(String value) {
        setAttribute(ATTR_INDEX_NAME, value);
        return this;
    }
}
