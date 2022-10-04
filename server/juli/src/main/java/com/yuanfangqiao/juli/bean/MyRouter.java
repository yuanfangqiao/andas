package com.yuanfangqiao.juli.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public class MyRouter {


    @Bean
    public RouterFunction<ServerResponse> indexRouter() {
        return RouterFunctions.route(RequestPredicates.GET("/b"),
                request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue("b.html"));
    }


}
