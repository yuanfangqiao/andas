package com.yuanfangqiao.juli.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@Service
@Slf4j
public class ConfigSchedule {

    @Autowired
    ServerProperties serverProperties;

    @Scheduled(fixedRate = 1000*10)
    public void configCheck(){
        // not working in websocket
        Duration duration = serverProperties.getNetty().getIdleTimeout();
        //log.info("idle:{}", duration.getSeconds());
    }

}
