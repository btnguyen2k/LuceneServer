package api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucene.IActionQueue;
import lucene.IIndex;
import lucene.IIndexFactory;
import lucene.action.BaseAction;
import lucene.spec.FieldSpec;
import lucene.spec.IndexSpec;
import play.Logger;
import util.IndexException;
import util.IndexUtils;

import com.github.ddth.commons.utils.DPathUtils;

/**
 * Engine to generate IDs.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class IndexApi {

    private IActionQueue actionQueue;
    private IIndexFactory indexFactory;

    public IActionQueue getActionQueue() {
        return this.actionQueue;
    }

    public IndexApi setActionQueue(IActionQueue actionQueue) {
        this.actionQueue = actionQueue;
        return this;
    }

    public IIndexFactory getIndexFactory() {
        return this.indexFactory;
    }

    public IndexApi setIndexFactory(IIndexFactory indexFactory) {
        this.indexFactory = indexFactory;
        return this;
    }

    /**
     * Init method.
     * 
     * @return
     */
    public IndexApi init() {
        updateThread = new UpdateThread();
        updateThread.start();
        return this;
    }

    /**
     * Destroy method.
     */
    public void destroy() {
        if (updateThread != null) {
            try {
                updateThread.stopExecution();
            } catch (Exception e) {
                // EMPTY
            } finally {
                updateThread = null;
            }
        }
    }

    private UpdateThread updateThread;

    private final class UpdateThread extends Thread {
        private boolean running = true;
        private IActionQueue actionQueue;

        public UpdateThread() {
            super("UpdateThread");
            actionQueue = getActionQueue();
            setDaemon(true);
        }

        public void stopExecution() {
            this.running = false;
        }

        public void run() {
            while (running && !isInterrupted()) {
                try {
                    BaseAction action = null;
                    do {
                        action = actionQueue != null ? actionQueue.take() : null;
                        if (action != null) {
                            final String indexName = action.indexName();
                            IIndex index = createIndex(indexName, null);
                            if (index != null) {
                                index.performAction(action);
                            } else {
                                Logger.warn("Cannot create instance of index [" + indexName
                                        + "] to perform async-action.");
                            }
                        }
                    } while (action != null);
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
        }
    }

    /*----------------------------------------------------------------------*/
    /**
     * <pre>
     * -= Create a new index =-
     * Input:
     * {
     *   "fields": {
     *     "field_name_1": {"type": "id, or string, or long", "store" (optional): true/false, "index" (optional): true/false},
     *     "field_name_2": {"type": "id, or string, or long", "store" (optional): true/false, "index" (optional): true/false}
     *   },
     *   "override" (optional): true/false
     * }
     * Output:
     * {"status":200/400/403/500,"message":"successful or failed message"}
     * Note:
     * - if "type" is not provided, default "string" type will be used.
     * - existing fields will not be changed, unless "override" is true.
     * </pre>
     */
    /*----------------------------------------------------------------------*/
    /**
     * API: Creates a new index.
     * 
     * @param _indexName
     * @param requestData
     * @return
     * @throws IndexException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public IIndex createIndex(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        if (!IndexUtils.isValidName(indexName)) {
            throw new IndexException(400, "InvalidIndexNameException: Invalid index name ["
                    + indexName + "]");
        }

        // validate request data
        Map<String, Map<String, Object>> fieldSpecs = DPathUtils.getValue(requestData, "fields",
                Map.class);
        if (fieldSpecs != null) {
            for (Entry<String, Map<String, Object>> entry : fieldSpecs.entrySet()) {
                String fieldName = entry.getKey();
                Map<String, Object> fieldData = entry.getValue();
                FieldSpec fieldSpec = FieldSpec.newInstance(fieldName, fieldData);
                if (fieldSpec == null) {
                    final String logMsg = "Invalid spec for field [" + fieldName + "]: "
                            + fieldName + "=" + fieldData;
                    throw new IndexException(400, logMsg);
                }
            }
        }

        IndexSpec indexSpec = IndexSpec.newInstance(indexName, requestData);
        Boolean override = DPathUtils.getValue(requestData, "override", Boolean.class);
        return createIndex(indexSpec, override != null ? override.booleanValue() : false);
    }

    /**
     * Creates a new index instance.
     * 
     * @param spec
     * @return
     * @throws IndexException
     * @throws IOException
     */
    private IIndex createIndex(IndexSpec spec, boolean override) throws IndexException, IOException {
        // TODO verify secret
        IIndex index = indexFactory.createIndex(spec, actionQueue);
        index.updateSpec(spec, override);
        return index;
    }

    /*----------------------------------------------------------------------*/
    /**
     * <pre>
     * -= Index a document =-
     * Input:
     * {
     *   "docs": [
     *     {
     *       "field_name_1": (string field) "value for this field",
     *       "field_name_2": (id field) "value",
     *       "field_name_3": (long field) 12345678
     *     },
     *     {
     *       2nd document
     *     },
     *     ...
     *   ]
     * }
     * Output:
     * {"status":200/400/403/500,"message":"successful or failed message"}
     * Note:
     * - index will be automatically created if not exist,
     * - field's type will be automatically determined based on field's value,
     * - error if field's new value's type does not match existing one,
     * </pre>
     */
    /*----------------------------------------------------------------------*/
    /**
     * API: Indexes document(s).
     * 
     * @param _indexName
     * @param requestData
     * @return number of documents have been scheduled for indexing
     * @throws IndexException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public int indexDocuments(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        if (!IndexUtils.isValidName(indexName)) {
            throw new IndexException(400, "InvalidIndexNameException: Invalid index name ["
                    + indexName + "]");
        }

        // validate request data
        List<Map<String, Object>> docs = DPathUtils.getValue(requestData, "docs", List.class);
        if (docs == null || docs.size() == 0) {
            final String logMsg = "Invalid or empty doc list to index";
            throw new IndexException(400, logMsg);
        }
        IndexSpec spec = IndexSpec.newInstance(indexName);
        IIndex index = indexFactory.createIndex(spec, actionQueue);
        if (index == null) {
            final String logMsg = "Cannot get index instance [" + indexName + "]";
            throw new IndexException(500, logMsg);
        }
        for (Map<String, Object> doc : docs) {
            if (!index.validateDocument(doc)) {
                final String logMsg = "Invalid document: one or more fields' value do not match index's schema.";
                throw new IndexException(400, logMsg);
            }
        }

        return index.indexDocuments(docs);
    }
}
