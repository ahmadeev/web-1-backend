package org.example;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class App {
    public static Logger logger = Logger.getLogger("org.example");
    public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    public static FCGIInterface fcgiInterface = new FCGIInterface();

    public static ArrayList<Double> validX = new ArrayList<>(Arrays.asList(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0));
    public static ArrayList<Double> validR = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

    public static double lowValidY = -3;
    public static double highValidY = 3;

    public App() {
        logger.setLevel(Level.ALL);
    }

    public static boolean isInputValid(HashMap<String, Double> parsedStringByKeyValue) {
        double x = parsedStringByKeyValue.get("xType");
        double y = parsedStringByKeyValue.get("yType");
        double R = parsedStringByKeyValue.get("RType");

        return validX.contains(x) && validR.contains(R) && y >= lowValidY && y <= highValidY;
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

    public static HashMap<String, Double> parseQueryString(String queryString) {
        String[] queryStringArray = queryString.split("&");
        HashMap<String, Double> parsedStringByKeyValue = new HashMap<>();
        for (int i = 0; i < queryStringArray.length; i++) {
            var temp = queryStringArray[i].split("=");
            try {
                parsedStringByKeyValue.put(temp[0], Double.parseDouble(temp[1].replace(",", ".")));
            } catch (Exception e) {
                logger.severe(e.getMessage());
                //  в этом блоке можно добавлять весь ответ целиком
                //  для валидации: проверка на длину запроса, проверка на наличие x, y, r
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

    public static void main(String[] args) {
        logger.info("Модуль App был запущен.");

        while (true) {
            int result = fcgiInterface.FCGIaccept();
            if (result < 0) {
                logger.severe("Ошибка при принятии FastCGI-запроса, код: " + result);
                break;
            }

            Point point = null;
            try {
                logger.info("Начало парсинга строки запроса.");

                HashMap<String, Double> parsedStringByKeyValue = null;

                Properties params = FCGIInterface.request.params;
                String queryString = null;
                if (params.getProperty("REQUEST_METHOD").equals("GET")) {
                    logger.info("Обрабатывается GET-запрос");
                    queryString = params.getProperty("QUERY_STRING");
                } else if (params.getProperty("REQUEST_METHOD").equals("POST")) {
                    logger.info("Обрабатывается POST-запрос");
                    queryString = readRequestBody();
                }

                if (queryString == null) {
                    throw new Exception("Строка запроса null!");
                }

                parsedStringByKeyValue = parseQueryString(queryString);

                if (parsedStringByKeyValue == null) {
                    throw new Exception("Разобранная строка null!");
                }

                if (!isInputValid(parsedStringByKeyValue)) {
                    throw new Exception("Невалидные введенные значения!");
                }

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
                logger.info("Валидация прошла успешно, время работы сценария: " + scriptTime + " ms");
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
