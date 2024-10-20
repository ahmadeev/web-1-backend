package org.example;

import com.fastcgi.FCGIInterface;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Locale;

import static java.lang.String.format;

public class App {
    public static Logger logger = Logger.getLogger("org.example");
    public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public App() {
        logger.setLevel(Level.ALL);
    }

//    public static boolean isPointValid(Point point) {
//        if (point == null) return false;
//        return false;
//    }

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

    public static HashMap<String, Double> parseQueryString(String queryString) {
        String[] queryStringArray = queryString.split("&");
        HashMap<String, Double> parsedStringByKeyValue = new HashMap<>();
        for (int i = 0; i < queryStringArray.length; i++) {
            var temp = queryStringArray[i].split("=");
            try {
                parsedStringByKeyValue.put(temp[0], Double.parseDouble(temp[1]));
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
        return parsedStringByKeyValue;
    }


    public static void main(String[] args) {
        logger.info("Модуль App был запущен.");

        FCGIInterface fcgiInterface = new FCGIInterface();

        while (true) {
            int result = fcgiInterface.FCGIaccept();
            if (result < 0) {
                logger.severe("Ошибка при принятии FastCGI-запроса, код: " + result);
                break;
            }

            Point point = null;
            try {
                logger.info("Начало парсинга строки запроса.");

                //--------------get
                Properties params = FCGIInterface.request.params;
                String queryString = params.getProperty("QUERY_STRING");

                if (queryString == null) {
                    throw new Exception("Строка запроса null!");
                }

                HashMap<String, Double> parsedStringByKeyValue = parseQueryString(queryString);
                //------------------

                point = new Point(
                        parsedStringByKeyValue.get("xType"),
                        parsedStringByKeyValue.get("yType"),
                        parsedStringByKeyValue.get("RType")
                );

                logger.info("Конец парсинга строки запроса.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Произошла ошибка: ", e);
            }

            String httpResponse;
            if (point != null) {
                var scriptTime = (new Date()).getTime() - point.getStartTime().getTime();
                String content = format(
                        Locale.ENGLISH, "{\"x\":%f,\"y\":%f,\"R\":%f,\"isHit\":%b,\"currentTime\":\"%s\",\"scriptTime\":\"%d\"}",
                        point.getX(),
                        point.getY(),
                        point.getR(),
                        point.isHit(),
                        sdf.format(point.getStartTime()),
                        scriptTime
                );
                logger.info("Валидация прошла успешно, время работы сценария: " + scriptTime);
                httpResponse = getOKResponse(content);

            } else {
                logger.info("Запрос не прошел валидацию.");
                httpResponse = getBadRequestErrorResponse();
            }

            logger.info("Строка ответа собрана.");
            System.out.println(httpResponse);
            logger.info("\n---------------------ОТВЕТ:\n" + httpResponse + "-------------------------");

        }
    }
}
