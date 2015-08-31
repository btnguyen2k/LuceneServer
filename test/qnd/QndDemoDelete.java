package qnd;

import java.util.HashMap;
import java.util.Map;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

import com.github.ddth.commons.utils.SerializationUtils;

public class QndDemoDelete {

    public static void main(String[] args) throws Exception {
        Map<String, Object> requestData = new HashMap<String, Object>();
        requestData.put("secret", "secret");
        requestData.put("query", "html");
        HttpResponse response = HttpRequest.post("http://localhost:9000/delete/demo")
                .body(SerializationUtils.toJsonString(requestData)).send();
        System.out.println(response.bodyText());
    }

}
