package com.example.camel_project.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CountProcessor implements Processor {
    private final static int BATCH_OF_MESSAGES_SIZE = 5;
    private final static String TXT = "txt";
    private final static String XML = "xml";
    private final static String UNDEFINED = "undefined";

    private long timestamp;
    private final Map<String, Integer> fileTypesMap;
    private int summOfFiles = 0;

    public CountProcessor() {
        fileTypesMap = new HashMap<>();
        fileTypesMap.put(XML, 0);
        fileTypesMap.put(TXT, 0);
        fileTypesMap.put(UNDEFINED, 0);
    }

    // шаг обработки события из camel
    public void process(Exchange exchange) {
        timestamp = System.nanoTime();
        messageExtensionHandler(exchange);
        summOfFiles++;

        if(summOfFiles % BATCH_OF_MESSAGES_SIZE == 0) {
            sendEmail(exchange);
        }

        if(log.isDebugEnabled()) {
            fileTypesMap
                    .forEach(
                            (x, y) -> log.debug(
                                    "Extension = " + x
                                            + " Count of files = " + y
                            )
                    );
        }
    }

    // обрабатываем события из camel
    private void messageExtensionHandler(Exchange exchange) {
        String fileName = getFileName(exchange);
        if (log.isDebugEnabled()) {
            log.debug("--- FileName ---");
            log.debug(fileName);
        }
        String extension = getExtension(fileName);
        if (log.isDebugEnabled()) {
            log.debug("--- FileExtension ---");
            log.debug(extension);
        }


        if(extension.equals(TXT) || extension.equals(XML)) {
            increaseCountFileWithExtension(extension);
        } else {
            increaseCountFileWithExtension(UNDEFINED);
        }
    }

    private String getFileName(Exchange exchange) {
        return exchange
                .getMessage()
                .getHeader("CamelFileName")
                .toString();
    }

    private String getExtension(String filename) {
        String[] parsedBlocks = filename.split("\\.");
        return parsedBlocks[parsedBlocks.length - 1].toLowerCase();
    }

    void increaseCountFileWithExtension(String extension) {
            int buffer = fileTypesMap.get(extension);
            fileTypesMap.put(extension, ++buffer);
    }

    private void sendEmail(Exchange exchange) {
        String mailBody = getLetterBody(getDeltaTime());
        Message message = exchange.getOut();
        message.setHeader("host", "localhost");
        message.setHeader("to", "moskow100@rambler.ru");
        message.setHeader("From", "moskow100@rambler.ru");
        message.setHeader("Subject", "Count of files");
        message.setBody(mailBody);
    }

    private long getDeltaTime() {
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - timestamp;
        timestamp = currentTime;
        return deltaTime;
    }

    private String getLetterBody(long deltaTime) {
        StringBuilder mailBody = new StringBuilder();
        mailBody.append("countTxt: ");
        mailBody.append(fileTypesMap.get(TXT));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("countXml: ");
        mailBody.append(fileTypesMap.get(XML));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("countOther: ");
        mailBody.append(fileTypesMap.get(UNDEFINED));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("elapsed time: ");
        mailBody.append(deltaTime);
        mailBody.append(" nanoseconds");
        return mailBody.toString();
    }
}
