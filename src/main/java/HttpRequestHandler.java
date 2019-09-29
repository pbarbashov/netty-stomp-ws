import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String baseUri;
    private final ServerRuntime serverRuntime;
    private final String responseBody = "{\"entropy\":%d,\"origins\":[\"*:*\"],\"cookie_needed\":true,\"websocket\":true}";
    private final Random random = new Random(System.currentTimeMillis());


    public HttpRequestHandler(String baseUri, ServerRuntime serverRuntime) {
        this.baseUri = baseUri;
        this.serverRuntime = serverRuntime;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println("Req rcv " + request);
        if (HttpUtil.is100ContinueExpected((request))) {
            send100Continue(ctx);
        }
        if (request.uri().contains(baseUri+"/info")) {
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            ByteBuf buf = Unpooled.copiedBuffer(String.format(responseBody,random.nextInt(Integer.MAX_VALUE)).getBytes(StandardCharsets.UTF_8));
            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK, buf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ChannelFuture f = ctx.writeAndFlush(response);
            if (!keepAlive)
                f.addListener(ChannelFutureListener.CLOSE);
        }
        else if (request.uri().matches(baseUri+"/\\d{1,3}/\\w*/websocket")){
            String[] params = request.uri().split("/", 5);
            System.out.println("Protocol switch " + Arrays.toString(params));
            serverRuntime.addSessionInfo(params[3],new SessionInfo(ctx.channel()));
            ctx.channel().attr(ServerRuntime.sessionAttribute).set(params[3]);
            ChannelFuture closeFuture = ctx.channel().closeFuture();
            closeFuture.addListener(future -> serverRuntime.removeSession(params[3]));
            ctx.fireChannelRead(request.retain());
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
