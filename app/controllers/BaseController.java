package controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import play.api.templates.Html;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import util.Constants;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.SerializationUtils;

/**
 * Base class for other controllers.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class BaseController extends Controller {

    protected static String getClientIp() {
        // obtain client IP
        String clientIPHeader = request().getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(clientIPHeader)) {
            clientIPHeader = request().getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(clientIPHeader)) {
            clientIPHeader = request().getHeader("Real-IP");
        }
        if (StringUtils.isBlank(clientIPHeader)) {
            clientIPHeader = request().remoteAddress();
        }
        return clientIPHeader;
    }

    /**
     * Extracts raw text content from request.
     * 
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    protected static String extractRequestContent() throws IOException {
        // obtain request body
        RequestBody requestBody = request().body();
        String requestContent = null;
        JsonNode jsonNode = requestBody.asJson();
        if (jsonNode != null) {
            requestContent = jsonNode.toString();
        } else {
            RawBuffer rawBuffer = requestBody.asRaw();
            if (rawBuffer != null) {
                byte[] buffer = rawBuffer.asBytes();
                if (buffer == null) {
                    buffer = FileUtils.readFileToByteArray(rawBuffer.asFile());
                }
                requestContent = new String(buffer, Constants.UTF8);
            } else {
                requestContent = requestBody.asText();
            }
        }
        // String clientIp = getClientIp();
        // String method = request().method();
        // String uri = request().uri();
        // Logger.info("[" + method + "] request [" + uri + "] from [" +
        // clientIp + "]: "
        // + requestContent);
        return requestContent;
    }

    /**
     * Parses request into a Map.
     * 
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> parseRequest() throws IOException {
        String requestContent = extractRequestContent();
        return SerializationUtils.fromJsonString(requestContent, Map.class);
    }

    /**
     * Utility method to render a HTML page.
     * 
     * @param view
     * @param params
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected static Html render(String view, Object... params) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        String clazzName = "views.html." + view;
        Class<?> clazz = Class.forName(clazzName);

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("render")) {
                Lang lang = lang();
                Object[] combinedParams = new Object[params.length + 1];
                combinedParams[params.length] = lang;
                for (int i = 0; i < params.length; i++) {
                    combinedParams[i] = params[i];
                }
                return (Html) method.invoke(null, combinedParams);
            }
        }
        return null;
    }
}
