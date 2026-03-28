package com.clientservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 客户服务系统启动类
 *
 * @author client-service
 * @since 2026-02-02
 */
@SpringBootApplication
@MapperScan("com.clientservice.infrastructure.persistence.mapper")
public class ClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientServiceApplication.class, args);
    }
}
