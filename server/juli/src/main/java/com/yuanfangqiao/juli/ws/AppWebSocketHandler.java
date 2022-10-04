package com.yuanfangqiao.juli.ws;

import com.yuanfangqiao.juli.util.MapUrlParamsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@Slf4j
public class AppWebSocketHandler implements WebSocketHandler {

    private Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1L), Duration.ofSeconds(1L));

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Map<String, String> queryMap = MapUrlParamsUtils.urlToMap(handshakeInfo.getUri().getQuery());
        String id = queryMap.getOrDefault("id", "defaultId");

        Mono<Void> input = session.receive()
                // connect
                .doOnSubscribe(subscription -> {
                    log.info("app onOpen, ws:{}, id:{}, remote:{}", session.getId(), id, session.getHandshakeInfo().getRemoteAddress());
                })
                // disconnect
                .doOnTerminate(()->{
                    log.warn("app onClose, ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress());
                })
                // error
                .doOnError(throwable -> {
                    log.error("app onError, ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress(), throwable);
                })
                // msg handle
                .doOnNext( msg -> {
                    log.info("app onMessage onNext, ws:{}, id:{}", session.getId(),id);
                    if(WebSocketMessage.Type.TEXT.equals(msg.getType())){
                        log.info("app onMessage TEXT, ws:{}, id:{}, msg:{}", session.getId(),id ,msg.getPayloadAsText());
                        try {
                            DeviceSender deviceSender = DeviceSenderPool.get(id);
                            if(deviceSender != null){
                                deviceSender.sendData(msg.getPayloadAsText());
                                log.info("app send to device TEXT, ws:{}, id:{}, msg:{}", session.getId(),id ,msg.getPayloadAsText());
                            } else {
                                log.info("app send to device TEXT, device offline, ws:{}, id:{}, msg:{}", session.getId(),id ,msg.getPayloadAsText());
                            }

                        }catch (Exception e){
                            log.error("app send to device TEXT, error, ws:{}, id:{}, msg:{}", session.getId(),id ,msg.getPayloadAsText(),e);
                        }

                    } else if (WebSocketMessage.Type.BINARY.equals(msg.getType())){
                        ByteBuffer byteBuffer = msg.getPayload().asByteBuffer();
                        int len = byteBuffer.limit() - byteBuffer.position();
                        log.info("app onMessage BINARY, ws:{}, id:{},len:{}", session.getId(),id, len);
                    } else if(WebSocketMessage.Type.PING.equals(msg.getType())){
                        // 这个需要单独方reactor处理ping消息
                        log.info("app onMessage PING, ws:{}, id:{}", session.getId(),id);
                        session.pongMessage(dataBufferFactory -> dataBufferFactory.allocateBuffer(0));
                    } else if(WebSocketMessage.Type.PING.equals(msg.getType())){
                        log.info("app onMessage PONG, ws:{}, id:{}", session.getId(),id);
                    }
                })
                .then();

        Mono<Void> output = session.send(Flux.create(sink ->
                AppSenderPool.put(id, new AppSender(session, sink))))
                .doOnError(throwable -> {
                    log.error("app output onError",throwable);
                });
        /**
         * Mono.zip()
         * zip input and output
         */
        return Mono.zip(input, output).then();
    }


}
