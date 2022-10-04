package com.yuanfangqiao.juli.ws.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * 自定义netty的空闲超时，检测出空闲超时后断开连接
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public final class JuliIdleTimeOutHandler extends IdleStateHandler  {

    private boolean closed;

    public JuliIdleTimeOutHandler(int allIdleTimeSeconds) {
        this((long)allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public JuliIdleTimeOutHandler(long allIdleTime, TimeUnit unit) {
        super(0L, 0L, allIdleTime, unit);
    }

    protected final void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        assert evt.state() == IdleState.READER_IDLE;

        this.readTimedOut(ctx);
    }

    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!this.closed) {
            // idle time exception
            ctx.fireExceptionCaught(JuliIdleTimeoutException.INSTANCE);
            // close
            ctx.close();
            this.closed = true;
        }

    }
}
