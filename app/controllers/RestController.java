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
        result.put(Constants.RESPONSE_FIELD_MESSAGE, message);
        response().setHeader(CONTENT_TYPE, "application/json");
        response().setHeader(CONTENT_ENCODING, "utf-8");
        return ok(SerializationUtils.toJsonString(result));
    }

    /*----------------------------------------------------------------------*/

    /*
     * Handles POST/create/:indexName
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
     * Handles POST/index/:indexName
     */
    public static Result indexDocumentPost(final String indexName) {
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
     * Handles POST/truncate/:indexName
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
}
