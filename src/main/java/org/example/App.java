package org.example;

import com.fastcgi.FCGIInterface;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("org.example");
        logger.setLevel(Level.ALL);
        logger.info("Модуль App был запущен.");

        FCGIInterface fcgiInterface = new FCGIInterface();

        while (true) {
            int result = fcgiInterface.FCGIaccept();
            if (result < 0) {
                logger.severe("Ошибка при принятии FastCGI-запроса, код: " + result);
                break;
            }

            String request = "Пустой ответ";

            try {
                logger.info("Начало парсинга строки запроса.");
                Properties params = FCGIInterface.request.params;
                String queryString = params.getProperty("QUERY_STRING");
                request = queryString == null ? request + ", не была передана строка запроса" : queryString;
                logger.info("Конец парсинга строки запроса.");
            } catch (Exception e) {
                request = "Произошла ошибка при парсинге строки запроса: \n" + e.getMessage();
                logger.log(Level.SEVERE, "Произошла ошибка", e);
            }

            String content = """
                <h1>Hello, World!</h1>
                <p>%s</p>
            """.formatted(request);

            String httpResponse = """
                HTTP/1.1 200 OK
                Content-Type: text/html
                Content-Length: %d
                \r\n\r\n%s
            """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

            logger.info("Строка ответа собрана.");

            System.out.println(httpResponse);

            logger.info("\n-------------------ОТВЕТ:\n" + httpResponse + "\n-------------------------");
        }
    }
}
