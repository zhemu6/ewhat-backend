package com.lushihao.ewhatbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.lushihao.ewhatbackend.mapper")
public class EwhatBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EwhatBackendApplication.class, args);
    }

}
