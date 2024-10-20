package org.example;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    public static Logger logger = Logger.getLogger("org.example");

    public RequestHandler() {
        logger.setLevel(Level.ALL);
    }

    public static String getOKResponse(String content) {
        return """
                HTTP/1.1 200 OK
                Content-Type: text/html
                Content-Length: %d
                \r\n\r\n%s
            """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);
    }

    public static String getBadRequestErrorResponse() {
        return """
                HTTP/2 400 Bad Request
                Content-Type: text/html
                Content-Length: %d
                \r\n\r\n%s
            """.formatted("Invalid data types".getBytes(StandardCharsets.UTF_8).length, "Invalid data types");
    }

    public static String readRequestBody() throws IOException {
        FCGIInterface.request.inStream.fill();
        var contentLength = FCGIInterface.request.inStream.available();
        var buffer = ByteBuffer.allocate(contentLength);
        var readBytes =
                FCGIInterface.request.inStream.read(buffer.array(), 0,
                        contentLength);
        var requestBodyRaw = new byte[readBytes];
        buffer.get(requestBodyRaw);
        buffer.clear();
        String requestBody = new String(requestBodyRaw, StandardCharsets.UTF_8);
        logger.info(requestBody);
        return requestBody;
    }

    public static HashMap<String, Double> parseQueryString(String queryString) {
        String[] queryStringArray = queryString.split("&");
        HashMap<String, Double> parsedStringByKeyValue = new HashMap<>();
        for (int i = 0; i < queryStringArray.length; i++) {
            var temp = queryStringArray[i].split("=");
            try {
                parsedStringByKeyValue.put(temp[0], Double.parseDouble(temp[1].replace(",", ".")));
            } catch (Exception e) {
                logger.severe(e.getMessage());
                parsedStringByKeyValue.put(temp[0], null);
            }
        }

        if (parsedStringByKeyValue.size() != 3
                || parsedStringByKeyValue.get("xType") == null
                || parsedStringByKeyValue.get("yType") == null
                || parsedStringByKeyValue.get("RType") == null) {
            return null;
        }

        return parsedStringByKeyValue;
    }


}
