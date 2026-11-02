package ru.tusman4ik.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import ru.tusman4ik.taskservice.vo.BasePackageName;

@EnableRetry
@SpringBootApplication
public class App {

    @Bean
    public BasePackageName basePackageName() {
        return new BasePackageName(this.getClass().getPackageName());
    }

    static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
