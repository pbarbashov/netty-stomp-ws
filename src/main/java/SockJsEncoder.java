import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.util.BufferRecyclers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class SockJsEncoder extends ChannelOutboundHandlerAdapter {
    private final JsonStringEncoder jsonStringEncoder = BufferRecyclers.getJsonStringEncoder();
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof DefaultHttpResponse)
            super.write(ctx, msg, promise);
        else if (msg instanceof TextWebSocketFrame)
            super.write(ctx, msg, promise);
        else {
            ByteBuf buf = (ByteBuf) msg;
            String s = buf.toString(StandardCharsets.UTF_8);
            System.out.println("s2c " + s);
            String sb = "a[" +
                    '"' +
                    escapeSockJsSpecialChars(jsonStringEncoder.quoteAsString(s)) +
                    '"' +
                    ']';
            ReferenceCountUtil.release(buf);
            ctx.writeAndFlush(new TextWebSocketFrame(sb));
            promise.setSuccess();
        }
    }

    private String escapeSockJsSpecialChars(char[] characters) {
        StringBuilder result = new StringBuilder();
        for (char c : characters) {
            if (isSockJsSpecialChar(c)) {
                result.append('\\').append('u');
                String hex = Integer.toHexString(c).toLowerCase();
                result.append("0".repeat(Math.max(0, (4 - hex.length()))));
                result.append(hex);
            }
            else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private boolean isSockJsSpecialChar(char ch) {
        return (ch <= '\u001F') || (ch >= '\u200C' && ch <= '\u200F') ||
                (ch >= '\u2028' && ch <= '\u202F') || (ch >= '\u2060' && ch <= '\u206F') ||
                (ch >= '\uFFF0') || (ch >= '\uD800' && ch <= '\uDFFF');
    }
}
