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

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import util.IndexException;
import util.IndexUtils;

import com.github.ddth.commons.utils.DPathUtils;

/**
 * High level index APIs.
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
     *   "secret": "authkey",
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
     * @param indexName
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
     *   "secret": "authkey",
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
     * @param indexName
     * @param requestData
     * @return number of documents have been scheduled for indexing
     * @throws IndexException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public int indexDocuments(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        // TODO verify secret

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

    /*----------------------------------------------------------------------*/
    /**
     * <pre>
     * -= Truncate an index =-
     * Input:
     * {
     *   "secret": "authkey"
     * }
     * Output:
     * {"status":200/400/403/500,"message":"successful or failed message"}
     * </pre>
     */
    /*----------------------------------------------------------------------*/
    /**
     * API: Truncates an index.
     * 
     * @param indexName
     * @param requestData
     * @return
     * @throws IndexException
     * @throws IOException
     */
    public boolean truncateIndex(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        // TODO verify secret

        if (!IndexUtils.isValidName(indexName)) {
            throw new IndexException(400, "InvalidIndexNameException: Invalid index name ["
                    + indexName + "]");
        }

        IndexSpec spec = IndexSpec.newInstance(indexName);
        IIndex index = indexFactory.openIndex(spec, actionQueue);
        if (index != null) {
            return index.truncate();
        }
        return false;
    }

    /*----------------------------------------------------------------------*/
    /**
     * <pre>
     * -= Delete document(s) from an index =-
     * Input:
     * {
     *   "secret": "authkey",
     *   "query" : "query to match document(s) for deletion", or
     *   "terms" : {
     *       "field1": "value1",
     *       "field2": value2,
     *       ...
     *   }
     * }
     * Output:
     * {"status":200/400/403/500,"message":"successful or failed message"}
     * Note:
     * - only one of "query" or "terms" will be used to match documents for deletion,
     * - if both "query" and "terms" are provided, "query" will have higher priority
     * </pre>
     */
    /*----------------------------------------------------------------------*/
    /**
     * API: Delete documents.
     * 
     * @param indexName
     * @param requestData
     * @return
     * @throws IndexException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public boolean deleteDocuments(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        // TODO verify secret

        if (!IndexUtils.isValidName(indexName)) {
            throw new IndexException(400, "InvalidIndexNameException: Invalid index name ["
                    + indexName + "]");
        }

        IndexSpec spec = IndexSpec.newInstance(indexName);
        IIndex index = indexFactory.openIndex(spec, actionQueue);
        if (index != null) {
            String query = DPathUtils.getValue(requestData, "query", String.class);
            if (!StringUtils.isBlank(query)) {
                if (index.validateQuery(query)) {
                    return index.deleteDocuments(query);
                } else {
                    throw new IndexException(400, "InvalidQueryException: Invalid query [" + query
                            + "]");
                }
            }
            Map<String, Object> terms = DPathUtils.getValue(requestData, "terms", Map.class);
            if (terms != null && terms.size() > 0) {
                return index.deleteDocuments(terms);
            } else {
                throw new IndexException(400, "InvalidQueryException: No query or terms supplied");
            }
        }
        return false;
    }

    /*----------------------------------------------------------------------*/
    /**
     * <pre>
     * -= Search document(s): GET method =-
     * Input:
     * ?q=Lucene query to search for document(s)
     * ?s=start offset (for pagination)
     * ?l=limit number of returned documents
     * ?b=bookmark returned from last search (for pagination)
     * Output:
     * {
     *   "status"  : 200/400/403/500,
     *   "message" : "successful or failed message",
     *   "num_hits": total number of hits,
     *   "bookmark": "bookmark of this search (for pagination)",
     *   "docs"    : [{"doc1 field":"doc2 value",...},{"doc2 field":"doc2 value",...},...]
     * }
     * </pre>
     */
    /**
     * <pre>
     * -= Search document(s): POST method =-
     * Input:
     * {
     *   "secret": "authkey",
     *   "query" : "query to match document(s) for deletion", or
     *   "terms" : {
     *       "field1": "value1",
     *       "field2": value2,
     *       ...
     *   },
     *   "start"   : start offset (for pagination),
     *   "limit"   : limit number of returned documents,
     *   "bookmark": "bookmark returned from last search (for pagination)"
     * }
     * Output:
     * {
     *   "status"  : 200/400/403/500,
     *   "message" : "successful or failed message",
     *   "num_hits": total number of hits,
     *   "bookmark": "bookmark of this search (for pagination)",
     *   "docs"    : [{"doc1 field":"doc2 value",...},{"doc2 field":"doc2 value",...},...]
     * }
     * </pre>
     */
    /*----------------------------------------------------------------------*/

    public final static String PARAM_SEARCH_QUERY = "query";
    public final static String PARAM_SEARCH_START = "start";
    public final static String PARAM_SEARCH_LIMIT = "limit";
    public final static String PARAM_SEARCH_BOOKMARK = "bookmark";

    /**
     * API: Search documents.
     * 
     * @param indexName
     * @param requestData
     * @return
     * @throws IndexException
     * @throws IOException
     */
    public Map<String, Object> searchDocuments(String indexName, Map<String, Object> requestData)
            throws IndexException, IOException {
        if (!IndexUtils.isValidName(indexName)) {
            throw new IndexException(400, "InvalidIndexNameException: Invalid index name ["
                    + indexName + "]");
        }

        IndexSpec spec = IndexSpec.newInstance(indexName);
        IIndex index = indexFactory.openIndex(spec, actionQueue);
        if (index == null) {
            throw new IndexException(400, "Index [" + indexName + "] does not exist");
        }

        String query = DPathUtils.getValue(requestData, PARAM_SEARCH_QUERY, String.class);
        if (!index.validateQuery(query)) {
            throw new IndexException(400, "InvalidQueryException: Invalid query [" + query + "]");
        }

        String bookmark = DPathUtils.getValue(requestData, PARAM_SEARCH_BOOKMARK, String.class);
        Integer start = DPathUtils.getValue(requestData, PARAM_SEARCH_START, Integer.class);
        Integer limit = DPathUtils.getValue(requestData, PARAM_SEARCH_LIMIT, Integer.class);

        return index.searchDocuments(query, bookmark, start != null ? start.intValue() : 0,
                limit != null ? limit.intValue() : 0);
    }
}
