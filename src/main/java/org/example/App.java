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

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("org.example");
        logger.setLevel(Level.ALL);
        logger.info("Модуль App был запущен.");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        FCGIInterface fcgiInterface = new FCGIInterface();

        while (true) {
            int result = fcgiInterface.FCGIaccept();
            if (result < 0) {
                logger.severe("Ошибка при принятии FastCGI-запроса, код: " + result);
                break;
            }

            String request = "Пустой ответ";
            Point point = null;
            try {
                logger.info("Начало парсинга строки запроса.");
                Properties params = FCGIInterface.request.params;
                String queryString = params.getProperty("QUERY_STRING");
                request = queryString == null ? request + ", не была передана строка запроса" : queryString;

                String[] parsedString = request.split("&");
                HashMap<String, Double> keyValue = new HashMap<>();
                for (int i = 0; i < parsedString.length; i++) {
                    var temp = parsedString[i].split("=");
                    try {
                        keyValue.put(temp[0], Double.parseDouble(temp[1]));
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                }

                point = new Point(keyValue.get("xType"), keyValue.get("yType"), keyValue.get("RType"));

                logger.info("Конец парсинга строки запроса.");
            } catch (Exception e) {
                request = "Произошла ошибка при парсинге строки запроса: \n" + e.getMessage();
                logger.log(Level.SEVERE, "Произошла ошибка", e);
            }

            String content = "пустой ответик";
            if (point != null) {
                var scriptTime = (new Date()).getTime() - point.getStartTime().getTime();
                logger.info("" + scriptTime);

                content = format(
                        Locale.ENGLISH, "{\"x\":%f,\"y\":%f,\"R\":%f,\"isHit\":%b,\"currentTime\":\"%s\",\"scriptTime\":\"%d\"}",
                        point.getX(),
                        point.getY(),
                        point.getR(),
                        point.isHit(),
                        sdf.format(point.getStartTime()),
                        scriptTime
                );
            }

            String httpResponse = """
                HTTP/1.1 200 OK
                Content-Type: text/html
                Content-Length: %d
                \r\n\r\n%s
            """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

            logger.info("Строка ответа собрана.");

            System.out.println(httpResponse);

            logger.info("\n---------------------ОТВЕТ:\n" + httpResponse + "-------------------------");
        }
    }
}
