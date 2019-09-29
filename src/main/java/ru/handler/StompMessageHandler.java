package ru.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.stomp.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import ru.server.ServerRuntime;
import ru.server.SessionInfo;

import java.util.concurrent.TimeUnit;

@Slf4j
public class StompMessageHandler extends SimpleChannelInboundHandler<StompFrame> {

    private final ServerRuntime serverRuntime;
    private final int serverHeartbeat;
    private final int clientDesiredHeartBeat;

    public StompMessageHandler(ServerRuntime serverRuntime, int serverHeartbeat, int clientDesiredHeartBeat) {
        this.serverRuntime = serverRuntime;
        this.serverHeartbeat = serverHeartbeat;
        this.clientDesiredHeartBeat = clientDesiredHeartBeat;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if (StompCommand.CONNECT == msg.command()) {
            log.debug("CONNECT received");
            DefaultStompFrame connectedFrame = new DefaultStompFrame(StompCommand.CONNECTED);
            DefaultStompHeaders headers = new DefaultStompHeaders();
            headers.add(StompHeaders.VERSION,"1.2");
            headers.add(StompHeaders.HEART_BEAT,serverHeartbeat + "," + clientDesiredHeartBeat);
            connectedFrame.headers().add(headers);
            ctx.writeAndFlush(connectedFrame);
            String sessionId = ctx.channel().attr(ServerRuntime.sessionAttribute).get();
            SessionInfo sessionInfo = serverRuntime.getSessionInfo(sessionId);
            String clientHeartBeats = msg.headers().getAsString(StompHeaders.HEART_BEAT);
            if (clientHeartBeats != null) {
                String[] heartbeatArray = clientHeartBeats.split(",", 2);
                int clientHeartBeat = Integer.parseInt(heartbeatArray[0]);
                int serverDesiredHeartBeat = Integer.parseInt(heartbeatArray[1]);
                sessionInfo.setClientHeartBeatMs(clientHeartBeat == 0 || clientDesiredHeartBeat == 0 ? 0 : Math.max(clientHeartBeat,clientDesiredHeartBeat));
                sessionInfo.setServerHeartBeatMs(serverHeartbeat == 0 || serverDesiredHeartBeat == 0 ? 0 : Math.max(serverHeartbeat, serverDesiredHeartBeat));
                if (sessionInfo.getClientHeartBeatMs() != 0 || sessionInfo.getServerHeartBeatMs() != 0) {
                    log.debug("Registring idle state listener");
                    ctx.pipeline().addBefore("sockjsDecoder", "idleStateHandler",
                            new IdleStateHandler(sessionInfo.getClientHeartBeatMs()*2, sessionInfo.getServerHeartBeatMs(),0, TimeUnit.MILLISECONDS));
                    ctx.pipeline().addAfter("idleStateHandler","heartbeatHandler",new StompHeartBeatHandler());
                }
            }
            else {
                log.debug("No heartbeats received!");
            }
        }
        else if (StompCommand.SUBSCRIBE == msg.command()) {
            String sessionId = ctx.channel().attr(ServerRuntime.sessionAttribute).get();
            SessionInfo sessionInfo = serverRuntime.getSessionInfo(sessionId);
            sessionInfo.subscribe(msg.headers().getAsString(StompHeaders.DESTINATION), msg.headers().getAsString(StompHeaders.ID));
        }
        else if (StompCommand.UNSUBSCRIBE == msg.command()) {
            String sessionId = ctx.channel().attr(ServerRuntime.sessionAttribute).get();
            SessionInfo sessionInfo = serverRuntime.getSessionInfo(sessionId);
            sessionInfo.unsubscribe(msg.headers().getAsString(StompHeaders.ID));
        }
        else if (StompCommand.SEND == msg.command()) {
            log.debug("SEND received " + ctx.channel().attr(ServerRuntime.sessionAttribute).get());
            ctx.fireChannelRead(msg.retain());
        }

    }
}
