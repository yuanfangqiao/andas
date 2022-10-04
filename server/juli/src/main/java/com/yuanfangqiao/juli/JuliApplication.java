package com.yuanfangqiao.juli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.netty.ReactorNetty;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@SpringBootApplication
@EnableScheduling
public class JuliApplication {

    public static void main(String[] args) {
        System.setProperty(ReactorNetty.POOL_MAX_IDLE_TIME, "10");
        System.setProperty(ReactorNetty.NATIVE, "true");
        SpringApplication.run(JuliApplication.class, args);
    }

}
