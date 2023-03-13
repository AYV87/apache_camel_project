package com.example.camel_project.jms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;

@Component
@Slf4j
public class JmsFileListener {

    final String queue = "empty";

    // метод слушает очередь empty
    @JmsListener(destination = queue)
    public void receiveXmlFile(BytesMessage bytesMessage) {
        try {
            // получаем данные
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            String out = new String(bytes);
            // выводим в консоль
            log.info(
                    String.format(" Queue = %s; File contains = %s",
                            queue, out)
            );
        } catch (Exception e) {
            log.error("Ошибка в классе JmsFileListener", e);
        }
    }
}
