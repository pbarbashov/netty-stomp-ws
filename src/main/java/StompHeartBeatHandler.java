import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StompHeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            String sessionId = ctx.channel().attr(ServerRuntime.sessionAttribute).get();
            if (e.state() == IdleState.READER_IDLE) {
                log.debug("Reader idle in session " + sessionId + ". Closing channel!");
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                log.debug("Writer idle in session " + sessionId + ". Sending ping to channel!");
                ctx.writeAndFlush(new TextWebSocketFrame("h[\"\\n]\""));
            }
        }
    }
}
