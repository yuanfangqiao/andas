package com.yuanfangqiao.juli.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;



/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public class AppSender {

    private WebSocketSession session;

    private FluxSink<WebSocketMessage> sink;

    public AppSender(WebSocketSession session, FluxSink<WebSocketMessage> sink) {
        this.session = session;
        this.sink = sink;
    }

    public void sendString(String data) {
        sink.next(session.textMessage(data));
    }

    public void sendBinary(byte[] bytes) {
        sink.next(session.binaryMessage(dataBufferFactory -> dataBufferFactory.wrap(bytes)));
    }

}
