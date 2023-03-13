package com.example.camel_project;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import com.example.camel_project.db.H2Repository;

import java.util.concurrent.TimeUnit;

@Configuration
@SpringBootApplication
public class Application implements ApplicationRunner {

    private final H2Repository h2Repository;

    public Application(H2Repository h2Repository) {
        this.h2Repository = h2Repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        TimeUnit.SECONDS.sleep(4);
        h2Repository.showDataInH2Db();
        h2Repository.cleadH2Db();
        System.exit(0);
    }
}
