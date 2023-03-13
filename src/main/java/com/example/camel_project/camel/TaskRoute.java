package com.example.camel_project.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import com.example.camel_project.db.H2Repository;

@Component
public class TaskRoute extends RouteBuilder {

    private final CountProcessor countProcessor;

    public TaskRoute(CountProcessor countProcessor) {
        this.countProcessor = countProcessor;
    }

    // выстраиваем маршрут обработки
    @Override
    public void configure() {
        // обработчик ошибок
        errorHandler(
           deadLetterChannel("jms:queue:invalid-queue"));

        from("file:data?noop=true")// откуда брать файлы
        .streamCaching()// берем оттуда поток событий
        .choice()
        .when(header("CamelFileName").endsWith(".xml")) // когда приходит событие с заголовком CamelFileName и файл заканчивается на .xml
            .to("jms:queue:empty")
            .process(countProcessor)
        .when(header("CamelFileName").endsWith(".txt"))
            .bean(H2Repository.class, "writeMessageToDb") // вызываем метод writeMessageToDb
            .to("jms:queue:empty")
            .process(countProcessor)
        .otherwise()
            .process(countProcessor)
            .throwException(new Exception(" Wrong extension !"))
        .end() // завершение choise
            .choice()
            .when(header("to").isNotNull())
            .to("smtp://localhost");
    }
}
