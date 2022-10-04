package com.yuanfangqiao.juli.ws.netty;


import io.netty.channel.ChannelException;
import io.netty.util.internal.PlatformDependent;

/**
 * @author Vitcou
 * @version 0.0
 * @date 2022/10/4 21:14
 */
public class JuliIdleTimeoutException extends ChannelException {

    public static final JuliIdleTimeoutException INSTANCE = PlatformDependent.javaVersion() >= 7 ? new JuliIdleTimeoutException(true) : new JuliIdleTimeoutException();

    public JuliIdleTimeoutException() {
    }

    public JuliIdleTimeoutException(String message) {
        super(message, (Throwable)null, false);
    }

    private JuliIdleTimeoutException(boolean shared) {
        super((String)null, null,shared);
    }
}
