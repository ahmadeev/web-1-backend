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
        logger.info("315 только начал");
        FCGIInterface fcgiInterface = new FCGIInterface();

        while (true) {
            int result = fcgiInterface.FCGIaccept();
            logger.info("" + result);
            if (result < 0) {
                logger.severe("Ошибка при принятии FastCGI-запроса, код: " + result);
                break; // выйти из цикла, если возникла ошибка
            }

            String request = "пустенько";

            try {
                Properties params = FCGIInterface.request.params;
                String queryString = params.getProperty("QUERY_STRING");
                request = queryString == null ? request + "null'чик" : queryString;
                logger.info("был тут");
            } catch (Exception e) {
                request = "ошибка: " + e.getMessage();
                logger.log(Level.SEVERE, "Произошла ошибка", e);
            }

//            String content = """
//                <html>
//                <head><title>Java FastCGI Hello World</title></head>
//                <body><h1>Hello, World!</h1><p>%s</p></body>
//                </html>
//            """.formatted(request);

            String content = """
                <h1>Hello, World!</h1>
                <p>%s</p>
            """.formatted(request);

//            String content = request;

            logger.info("почти дошел");

            String httpResponse = """
                HTTP/1.1 200 OK
                Content-Type: text/html
                Content-Length: %d
                \r\n\r\n%s
            """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);
            logger.info("чуть не упал, больно");
            System.out.println(httpResponse);
            logger.info("ДОБРАЛСЯ ЖИВОЙ И НЕВРЕДИМЫЙ\n---------------");
            logger.info(httpResponse);
            logger.info("--------------------");
        }
    }
}
