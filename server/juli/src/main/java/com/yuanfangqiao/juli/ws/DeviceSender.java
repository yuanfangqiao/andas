package com.yuanfangqiao.juli.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public class DeviceSender {

    private WebSocketSession session;

    private FluxSink<WebSocketMessage> sink;

    public DeviceSender(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        this.session = session;
        this.sink = sink;
    }

    public void sendData(String data) {
        sink.next(session.textMessage(data));
    }
}
