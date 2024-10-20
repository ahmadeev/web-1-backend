package org.example;

import com.fastcgi.FCGIInterface;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.example.RequestHandler.*;
import static org.example.Validator.isInputValid;

public class App {
    public static Logger logger = Logger.getLogger("org.example");
    public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    public static FCGIInterface fcgiInterface = new FCGIInterface();

    public App() {
        logger.setLevel(Level.ALL);
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
