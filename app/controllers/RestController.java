package controllers;

import globals.Registry;

import java.util.HashMap;
import java.util.Map;

import lucene.IIndex;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Result;
import util.Constants;
import util.IndexException;
import api.IndexApi;

import com.github.ddth.commons.utils.SerializationUtils;

/**
 * REST APIs controller.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
@BodyParser.Of(value = BodyParser.Raw.class, maxLength = 10 * 1024 * 1024)
public class RestController extends BaseController {

    private static Result doResponse(int status, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(Constants.RESPONSE_FIELD_STATUS, status);
        if (message != null) {
            result.put(Constants.RESPONSE_FIELD_MESSAGE, message);
        }
        response().setHeader(CONTENT_TYPE, "application/json");
        response().setHeader(CONTENT_ENCODING, "utf-8");
        return ok(SerializationUtils.toJsonString(result));
    }

    private static Result doResponse(int status, String message, Map<String, Object> responseData) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (responseData != null) {
            result.putAll(responseData);
        }
        result.put(Constants.RESPONSE_FIELD_STATUS, status);
        if (message != null) {
            result.put(Constants.RESPONSE_FIELD_MESSAGE, message);
        }
        response().setHeader(CONTENT_TYPE, "application/json");
        response().setHeader(CONTENT_ENCODING, "utf-8");
        return ok(SerializationUtils.toJsonString(result));
    }

    /*----------------------------------------------------------------------*/

    /*
     * Handles POST/:indexName/create
     */
    public static Result createIndexPost(final String indexName) {
        return createIndex(indexName);
    }

    /*
     * Handles PUT/:indexName
     */
    public static Result createIndexPut(final String indexName) {
        return createIndex(indexName);
    }

    private static Result createIndex(final String indexName) {
        try {
            Map<String, Object> requestData = parseRequest();
            IndexApi indexApi = Registry.getIndexApi();
            try {
                IIndex index = indexApi.createIndex(indexName, requestData);
                if (index != null) {
                    return doResponse(200, "Successful");
                } else {
                    final String logMsg = "Cannot create index [" + indexName + "]";
                    Logger.warn(logMsg);
                    return doResponse(500, logMsg);
                }
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

    /*----------------------------------------------------------------------*/
    /*
     * Handles PUT/:indexName/
     */
    public static Result indexDocumentsPut(final String indexName) {
        return indexDocuments(indexName);
    }

    /*
     * Handles POST/:indexName/index
     */
    public static Result indexDocumentsPost(final String indexName) {
        return indexDocuments(indexName);
    }

    private static Result indexDocuments(final String indexName) {
        try {
            Map<String, Object> requestData = parseRequest();
            IndexApi indexApi = Registry.getIndexApi();
            try {
                int numDocs = indexApi.indexDocuments(indexName, requestData);
                return doResponse(200, "[" + numDocs
                        + "] document(s) have been scheduled for indexing");
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

    /*----------------------------------------------------------------------*/

    /*
     * Handles POST/:indexName/truncate
     */
    public static Result truncateIndexPost(final String indexName) {
        try {
            Map<String, Object> requestData = parseRequest();
            IndexApi indexApi = Registry.getIndexApi();
            try {
                if (indexApi.truncateIndex(indexName, requestData)) {
                    return doResponse(200, "Index [" + indexName
                            + "] has been scheduled for truncating");
                } else {
                    return doResponse(200, "Index [" + indexName
                            + "] has not been scheduled for truncating, maybe it doesnot exist?");
                }
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

    /*----------------------------------------------------------------------*/
    /*
     * Handles DELETE/:indexName/
     */
    public static Result deleteDocumentsDelete(final String indexName) {
        return deleteDocuments(indexName);
    }

    /*
     * Handles POST/:indexName/delete
     */
    public static Result deleteDocumentsPost(final String indexName) {
        return deleteDocuments(indexName);
    }

    private static Result deleteDocuments(final String indexName) {
        try {
            Map<String, Object> requestData = parseRequest();
            IndexApi indexApi = Registry.getIndexApi();
            try {
                if (indexApi.deleteDocuments(indexName, requestData)) {
                    return doResponse(200, "Documents of index [" + indexName
                            + "] have been scheduled for deleting");
                } else {
                    return doResponse(200, "Cannot delete documents from index [" + indexName
                            + "], maybe it doesnot exist?");
                }
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

    /*----------------------------------------------------------------------*/
    /*
     * Handles GET/:indexName/?q=$query&s=$start&l=$limit&b=$bookmark
     */
    public static Result searchDocumentsGet(final String indexName) {
        try {
            IndexApi indexApi = Registry.getIndexApi();
            try {
                Map<String, Object> requestData = new HashMap<String, Object>();
                requestData.put(IndexApi.PARAM_SEARCH_QUERY, request().getQueryString("q"));
                if (requestData.get(IndexApi.PARAM_SEARCH_QUERY) == null) {
                    requestData.put(IndexApi.PARAM_SEARCH_QUERY, request().getQueryString("query"));
                }

                requestData.put(IndexApi.PARAM_SEARCH_START, request().getQueryString("s"));
                if (requestData.get(IndexApi.PARAM_SEARCH_START) == null) {
                    requestData.put(IndexApi.PARAM_SEARCH_START, request().getQueryString("start"));
                }

                requestData.put(IndexApi.PARAM_SEARCH_LIMIT, request().getQueryString("l"));
                if (requestData.get(IndexApi.PARAM_SEARCH_LIMIT) == null) {
                    requestData.put(IndexApi.PARAM_SEARCH_LIMIT, request().getQueryString("limit"));
                }

                requestData.put(IndexApi.PARAM_SEARCH_BOOKMARK, request().getQueryString("b"));
                if (requestData.get(IndexApi.PARAM_SEARCH_BOOKMARK) == null) {
                    requestData.put(IndexApi.PARAM_SEARCH_BOOKMARK,
                            request().getQueryString("bookmark"));
                }

                Map<String, Object> result = indexApi.searchDocuments(indexName, requestData);
                return doResponse(200, "Successful", result);
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

    /*
     * Handles POST/:indexName/search
     */
    public static Result searchDocumentsPost(final String indexName) {
        try {
            Map<String, Object> requestData = parseRequest();
            IndexApi indexApi = Registry.getIndexApi();
            try {
                Map<String, Object> result = indexApi.searchDocuments(indexName, requestData);
                return doResponse(200, "Successful", result);
            } catch (IndexException e) {
                Logger.error(e.getMessage(), e);
                return doResponse(e.getStatus(), e.getMessage());
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }

}
