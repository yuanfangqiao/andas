package com.yuanfangqiao.juli.ws;

import com.yuanfangqiao.juli.util.MapUrlParamsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DeviceWebSocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Map<String, String> queryMap = MapUrlParamsUtils.urlToMap(handshakeInfo.getUri().getQuery());
        String id = queryMap.getOrDefault("id", "defaultId");

        Mono<Void> input = session.receive()
                // connect
                .doOnSubscribe(subscription -> {
                    log.info("device onOpen, ws:{}, id:{}, remote:{}", session.getId(), id, session.getHandshakeInfo().getRemoteAddress());
                })
                // msg handle
                .doOnNext( msg -> {
                    if(WebSocketMessage.Type.TEXT.equals(msg.getType())){
                        log.info("device onMessage TEXT, ws:{}, id:{}, msg:{}", session.getId(),id ,msg.getPayloadAsText());
                        AppSender appSender = AppSenderPool.get(id);
                        if(appSender != null){
                            // warp byte and send
                            AppSenderPool.get(id).sendString(msg.getPayloadAsText());
                            log.info("device send to app TEXT, ws:{}, id:{}", session.getId(),id);
                        } else {
                            log.info("device send to app TEXT, app offline, ws:{}, id:{}", session.getId(),id);
                        }
                    } else if (WebSocketMessage.Type.BINARY.equals(msg.getType())){
                        ByteBuffer byteBuffer = msg.getPayload().asByteBuffer();

                        int len = byteBuffer.limit() - byteBuffer.position();
                        log.info("device onMessage BINARY, ws:{}, id:{},len:{}", session.getId(),id, len);

                        try {
                            AppSender appSender = AppSenderPool.get(id);
                            if(appSender != null){
                                // warp byte and send
                                byte[] bytes = new byte[len];
                                byteBuffer.get(bytes);
                                appSender .sendBinary(bytes);
                                log.info("device send to app BINARY, ws:{}, id:{},len:{}", session.getId(),id, len);
                            } else {
                                log.info("device send to app BINARY, app offline, ws:{}, id:{},len:{}", session.getId(),id, len);
                            }
                        }catch (Exception e){
                            log.info("device send to app BINARY, exception, ws:{}, id:{},len:{}", session.getId(),id, len, e);
                        }
                    }  else if(WebSocketMessage.Type.PING.equals(msg.getType())){
                        // 这个需要单独方reactor处理ping消息
                        log.info("device onMessage PING, ws:{}, id:{}", session.getId(),id);
                        session.pongMessage(dataBufferFactory -> dataBufferFactory.allocateBuffer(0));
                    } else if(WebSocketMessage.Type.PING.equals(msg.getType())){
                        log.info("device onMessage PONG, ws:{}, id:{}", session.getId(),id);
                    }
                })
                // disconnect
                .doOnTerminate(()->{
                    log.warn("device onClose(disconnect), ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress());
                })
                // error
                .doOnError(throwable -> {
                    log.warn("device onError(disconnect), ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress(), throwable);
                })
                .doOnCancel(() -> {
                    log.warn("device onCancel(disconnect), ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress());
                })
                .doOnComplete(() ->{
                    log.warn("device onComplete(disconnect), ws:{}, id:{}, remote:{}", session.getId(),id, session.getHandshakeInfo().getRemoteAddress());
                })
//                .doFinally( sig -> {
//                    log.info("device, Terminating WebSocket Session (client side) sig:{}", sig.name());
//                    session.close();
//                })
                // here is useful, can handle idle timeout
                .timeout(Duration.ofSeconds(30),f -> {
                    f.onComplete();
                    log.info("device timeout(idle), time:{}",30);
                })
                .then();


        Mono<Void> output = session.send(Flux.create(sink ->
                DeviceSenderPool.put(id, new DeviceSender(session, sink))))
                .doOnError(throwable -> {
                    log.error("device output onError",throwable);
                });
        /**
         * Mono.zip()
         * zip input and output
         */
        return Mono.zip(input, output).then();
    }

}
