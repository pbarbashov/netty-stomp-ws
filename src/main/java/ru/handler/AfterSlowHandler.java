package ru.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import ru.server.ServerRuntime;

import java.nio.charset.StandardCharsets;

@Slf4j
public class AfterSlowHandler extends SimpleChannelInboundHandler<StompFrame> {
    private final ServerRuntime serverRuntime;

    public AfterSlowHandler(ServerRuntime serverRuntime) {
        this.serverRuntime = serverRuntime;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        log.trace("After slow");
        Attribute<String> sessionId = ctx.channel().attr(ru.server.ServerRuntime.sessionAttribute);
        StompFrame stompFrame = new DefaultStompFrame(StompCommand.MESSAGE);
        String destination = "/user/proxy/kafka";
        stompFrame.headers().add(StompHeaders.DESTINATION, destination);
        stompFrame.headers().add(StompHeaders.CONTENT_TYPE,"application/json;charset=UTF-8");
        stompFrame.headers().add(StompHeaders.SUBSCRIPTION, serverRuntime.searchSubscriptionId(sessionId.get(),destination));
        stompFrame.content().writeCharSequence("{\"service\":1}", StandardCharsets.UTF_8);
        ctx.channel().writeAndFlush(stompFrame);
    }
}
