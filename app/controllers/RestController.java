package controllers;

import globals.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lucene.FieldSpec;
import lucene.IIndex;
import lucene.IndexSpec;
import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Result;
import util.Constants;
import api.IndexApi;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;

/**
 * REST APIs controller.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
@BodyParser.Of(BodyParser.Raw.class)
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

    @SuppressWarnings("unchecked")
    private static Result validateCreateRequestData(Map<String, Object> requestData) {
        Map<String, Map<String, Object>> fieldSpecs = DPathUtils.getValue(requestData, "fields",
                Map.class);
        if (fieldSpecs != null) {
            for (Entry<String, Map<String, Object>> entry : fieldSpecs.entrySet()) {
                String fieldName = entry.getKey();
                Map<String, Object> fieldData = entry.getValue();
                try {
                    FieldSpec fieldSpec = FieldSpec.newInstance(fieldName, fieldData);
                    if (fieldSpec == null) {
                        final String logMsg = "Invalid spec for field [" + fieldName + "]: "
                                + fieldName + "=" + fieldData;
                        return doResponse(400, logMsg);
                    }
                } catch (Exception e) {
                    final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
                    Logger.error(logMsg, e);
                    return doResponse(500, logMsg);
                }
            }
        }
        return null;
    }

    private static Result createIndex(final String indexName) {
        Map<String, Object> requestData = parseRequest();
        Result result = validateCreateRequestData(requestData);
        if (result != null) {
            return result;
        }
        IndexApi indexApi = Registry.getIndexApi();
        try {
            if (!indexApi.isValidIndexName(indexName)) {
                return doResponse(400, "InvalidIndexNameException: Invalid index name ["
                        + indexName + "]");
            }
            IndexSpec indexSpec = IndexSpec.newInstance(indexName, requestData);
            Boolean override = DPathUtils.getValue(requestData, "override", Boolean.class);
            IIndex index = indexApi.createIndex(indexSpec,
                    override != null ? override.booleanValue() : false);
            if (index != null) {
                return doResponse(200, "Successful");
            } else {
                final String logMsg = "Cannot create index [" + indexName + "]";
                Logger.warn(logMsg);
                return doResponse(500, logMsg);
            }
        } catch (Exception e) {
            final String logMsg = "Exception [" + e.getClass() + "]: " + e.getMessage();
            Logger.error(logMsg, e);
            return doResponse(500, logMsg);
        }
    }
    /*----------------------------------------------------------------------*/

}
