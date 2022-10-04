package com.yuanfangqiao.juli.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.netty.http.server.WebsocketServerSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@Configuration
public class WsConfig {

    /**
     * bind url websocket handler
     * @return
     */
    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/camera/client/ws", new AppWebSocketHandler());
        map.put("/camera/uploader/ws", new DeviceWebSocketHandler());
        int order = -1; // before annotated controllers
        return new SimpleUrlHandlerMapping(map, order);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
        return new WebSocketHandlerAdapter(webSocketService);
    }

    /**
     * ！important
     * Configure whether to let ping frames through to be handled by the WebSocketHandler given to the upgrade method. By default, Reactor Netty automatically replies with pong frames in response to pings. This is useful in a proxy for allowing ping and pong frames through.
     * By default, this is set to false in which case ping frames are handled automatically by Reactor Netty. If set to true, ping frames will be passed through to the WebSocketHandler.
     * as of 5.2.6 in favor of providing a supplier of WebsocketServerSpec.Builder with a constructor argument
     *
     * ！重要
     * 这里设置设置开关ping的消息，需要自行响应ping消息
     * 默认情况下reactor为了简化，默认斯reactor自行ping ping消息处理
     *
     * @return
     */
    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(
                new ReactorNettyRequestUpgradeStrategy(
                        WebsocketServerSpec.builder().compress(true)
                                .handlePing(true)));
    }

}
