

package com.yuanfangqiao.juli.ws.netty;

import com.yuanfangqiao.juli.ws.netty.JuliIdleTimeOutHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

import reactor.netty.http.HttpProtocol;
import reactor.netty.resources.LoopResources;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
@Configuration
@Slf4j
public class NettyServerFactoryHttpSupport implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

	@Value("${server.port:8080}")
	private int serverPort;

	/**
	 * 这里制定Reactor Netty启动自定义配置
	 * 1. 允许绑定自定义空闲超时
	 * @param factory
	 */
	@Override
	public void customize(NettyReactiveWebServerFactory factory) {
		LoopResources resource = LoopResources.create("ingress-juli");
		ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
		reactorResourceFactory.setLoopResources(resource);
		reactorResourceFactory.setUseGlobalResources(false);
		factory.setResourceFactory(reactorResourceFactory);
		factory.setPort(serverPort);

		if(Epoll.isAvailable()) {
			factory.addServerCustomizers(builder -> builder.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(EpollChannelOption.TCP_KEEPIDLE,10)
					.childOption(EpollChannelOption.TCP_KEEPCNT,1)
					.childOption(EpollChannelOption.TCP_KEEPINTVL,10));
		} else {
			factory.addServerCustomizers(builder -> builder.childOption(ChannelOption.SO_KEEPALIVE, true));
		}

		// just http timeout, no websocket
		//factory.addServerCustomizers(httpServer -> httpServer.idleTimeout(Duration.ofMillis(10000)));
		// useful, websocket idle timeout detect
		//factory.addServerCustomizers(httpServer -> httpServer.doOnConnection(connection -> connection.addHandlerFirst(new JuliIdleTimeOutHandler(10))));
	}

	@Bean
	NettyServerCustomizer h2cCustomizer() {
	    return (httpServer) -> httpServer.protocol(HttpProtocol.HTTP11, HttpProtocol.H2C);
	}



}


