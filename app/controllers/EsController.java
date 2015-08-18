package controllers;

import java.util.HashMap;
import java.util.Map;

import play.mvc.Result;

import com.github.ddth.commons.utils.SerializationUtils;

/**
 * ElasticSearch interface.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class EsController extends BaseController {

    private static Result doResponse(int status, String message) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", status);
        response.put("error", message);
        return doResponse(response);
    }

    private static Result doResponse(boolean acknowledged) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("acknowledged", acknowledged);
        return doResponse(response);
    }

    private static Result doResponse(Map<String, Object> result) {
        return ok(SerializationUtils.toJsonString(result));
    }

    // /*
    // * Handles: PUT:/createIndex/:indexName
    // */
    // public static Result createIndex(final String indexName) {
    // IndexApi indexApi = Registry.getIndexApi();
    // try {
    // if (!indexApi.isValidIndexName(indexName)) {
    // return doResponse(400, "InvalidIndexNameException: Invalid index name ["
    // + indexName + "]");
    // }
    // IIndex index =
    // indexApi.createIndex(PlayAppUtils.appConfigString("index.secret"),
    // indexName);
    // if (index != null) {
    // return doResponse(true);
    // } else {
    // final String logMsg = "Cannot create index [" + indexName + "]";
    // Logger.warn(logMsg);
    // return doResponse(500, logMsg);
    // }
    // } catch (Exception e) {
    // final String logMsg = "Exception [" + e.getClass() + "]: " +
    // e.getMessage();
    // Logger.error(logMsg, e);
    // return doResponse(500, logMsg);
    // }
    // }

}
